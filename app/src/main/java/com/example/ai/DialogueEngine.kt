package com.example.ai

import com.example.engine.CareerEngine
import com.example.model.CareerSaveState
import com.example.model.ChatMessage
import com.example.model.FinancialTransaction
import com.example.model.NewsEvent
import com.example.model.NpcCharacter
import com.example.model.SquadRole
import com.example.model.StartingStatus
import com.example.model.TransferOffer
import com.example.model.XPostItem
import com.example.ui.components.FLFormatters
import kotlin.math.max
import kotlin.random.Random

/**
 * Outcome of any AI or offline-fallback dialogue turn (WhatsApp, Instagram DM, Coach Meeting, Phone Call).
 */
data class DialogueExecutionResult(
    val updatedState: CareerSaveState,
    val replyMessage: ChatMessage,
    val structuredResponse: GeminiDialogueStructuredResponse,
    val toastSummary: String? = null,
    val usedGeminiAi: Boolean = false
)

/**
 * Central Dialogue Engine for Football Life.
 *
 * Capabilities:
 * 1. Appends the user's message immediately (optimistic UI update) so the chat bubble appears right away.
 * 2. Calls Gemini AI (`GeminiClient`) with rich persona, live career context, and conversation memory
 *    to produce an authentic Turkish character reply + structured JSON side-channel deltas.
 * 3. Applies structured deltas (`affinityDelta`, `trustDelta`, `moraleDelta`, `teamChemistryDelta`,
 *    `fanSupportDelta`, `managerTrustDelta`, `startingStatusHint`, `triggerTransferOffer`,
 *    `triggerWageRaise`, `triggerPressHeadline`, `newMemoryNote`, `suggestedQuickReplies`) to `CareerSaveState`.
 * 4. Seamlessly falls back to contextual Turkish scripted dialogue (`CareerEngine`) when no API key is configured,
 *    AI dialogues are toggled off, or the device is offline.
 */
object DialogueEngine {

    /**
     * Ensures the Club President (`NPC_PRESIDENT`) exists in the career's NPC list so the player can
     * message or call the President as well as Coach, Agent, Family, Teammates, and Journalists.
     */
    fun ensureExtendedNpcs(state: CareerSaveState): CareerSaveState {
        val club = state.currentClub
        if (state.npcs.any { it.id == "NPC_PRESIDENT" }) return state

        val presidentName = when (club.id) {
            "TR_GS" -> "Dursun Özbek"
            "TR_FB" -> "Ali Koç"
            "TR_BJK" -> "Hasan Arat"
            "TR_TS" -> "Ertuğrul Doğan"
            else -> "Başkan Ahmet Demir (${club.shortName})"
        }
        val presidentNpc = NpcCharacter(
            id = "NPC_PRESIDENT",
            name = presidentName,
            roleTitle = "Kulüp Başkanı",
            avatarColorHex = club.primaryColorHex,
            personality = "Vizyoner & Camia Lideri",
            affinity = 58,
            trust = 60,
            currentMood = "Gururlu",
            shortTermGoal = "${club.name} kulübünü ligde hedefe taşımak",
            longTermGoal = "Kulüp ekonomisini ve sportif başarısını büyütmek",
            importantMemories = listOf("${club.name} sözleşmeni onayladı."),
            quickReplyOptions = listOf(
                "Başkanım, ${club.name} arması için sahada her şeyimi vereceğim.",
                "Takım arkadaşlarımla şampiyonluk sözü veriyoruz başkanım.",
                "Tesislerimiz ve yönetimimizin desteği bize büyük güç veriyor."
            ),
            messages = listOf(
                ChatMessage(
                    id = "MSG_PRES_INIT",
                    senderName = presidentName,
                    isFromUser = false,
                    text = "Evladım, ${club.name} ailesine hoş geldin. Yönetim kurulu olarak senin yeteneğine çok güveniyoruz. Bu formanın ağırlığını bilerek sahada terinin son damlasına kadar savaşmanı bekliyorum.",
                    dayIndex = state.currentDayIndex,
                    timeLabel = state.formattedClockTime,
                    emotionTag = "Ciddi"
                )
            ),
            unreadCount = 0
        )

        return state.copy(npcs = state.npcs + presidentNpc)
    }

    /**
     * Step 1 (Optimistic UI): Immediately appends the user's outgoing message to the NPC's chat history
     * so the player sees their bubble right away while Gemini generates the reply.
     */
    fun appendOptimisticUserNpcMessage(
        state: CareerSaveState,
        npcId: String,
        userText: String
    ): Pair<CareerSaveState, ChatMessage?> {
        val cleanText = userText.trim()
        if (cleanText.isEmpty()) return state to null
        val enrichedState = ensureExtendedNpcs(state)
        val targetNpc = enrichedState.npcs.find { it.id == npcId } ?: return enrichedState to null

        val userMsg = ChatMessage(
            id = "MSG_USR_${System.currentTimeMillis()}",
            senderName = enrichedState.player.firstName,
            isFromUser = true,
            text = cleanText,
            dayIndex = enrichedState.currentDayIndex,
            timeLabel = enrichedState.formattedClockTime
        )

        val updatedNpcs = enrichedState.npcs.map { npc ->
            if (npc.id == npcId) {
                npc.copy(
                    messages = npc.messages + userMsg,
                    unreadCount = 0
                )
            } else npc
        }

        return enrichedState.copy(
            updatedAtMs = System.currentTimeMillis(),
            npcs = updatedNpcs
        ) to userMsg
    }

    /**
     * Step 1b (Optimistic UI for Instagram DM): Immediately appends the user's outgoing DM message.
     */
    fun appendOptimisticUserInstaDm(
        state: CareerSaveState,
        threadId: String,
        userText: String
    ): Pair<CareerSaveState, ChatMessage?> {
        val cleanText = userText.trim()
        if (cleanText.isEmpty()) return state to null
        val thread = state.instaDmThreads.find { it.id == threadId } ?: return state to null

        val userMsg = ChatMessage(
            id = "IDM_USR_${System.currentTimeMillis()}",
            senderName = state.player.fullName,
            isFromUser = true,
            text = cleanText,
            dayIndex = state.currentDayIndex,
            timeLabel = state.formattedClockTime
        )

        val updatedThreads = state.instaDmThreads.map { th ->
            if (th.id == threadId) {
                th.copy(
                    isPendingRequest = false,
                    unreadCount = 0,
                    messages = th.messages + userMsg
                )
            } else th
        }

        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            instaDmThreads = updatedThreads
        ) to userMsg
    }

    /**
     * Step 2: Resolves an NPC reply (WhatsApp chat, Coach office meeting, or Voice Phone Call)
     * using Gemini AI when available, or offline contextual Turkish fallback otherwise.
     *
     * Note: Assumes [stateBeforeUserMsg] is the state BEFORE the user message was appended
     * (or if the user message was already optimistically appended in [currentStateWithUserMsg],
     * replaces/appends cleanly without duplicating the user message).
     */
    suspend fun generateAndApplyNpcDialogue(
        stateBeforeUserMsg: CareerSaveState,
        currentStateWithUserMsg: CareerSaveState,
        npcId: String,
        userText: String,
        channel: DialogueChannelType = DialogueChannelType.WHATSAPP_CHAT,
        topicContext: String? = null,
        apiKeyInput: String?,
        modelId: String,
        aiEnabled: Boolean = true
    ): DialogueExecutionResult {
        val cleanText = userText.trim()
        val baseEnriched = ensureExtendedNpcs(stateBeforeUserMsg)
        val currentEnriched = ensureExtendedNpcs(currentStateWithUserMsg)
        val targetNpc = currentEnriched.npcs.find { it.id == npcId }
            ?: baseEnriched.npcs.find { it.id == npcId }

        if (targetNpc == null || cleanText.isEmpty()) {
            val fallbackMsg = ChatMessage(
                id = "MSG_EMPTY_${System.currentTimeMillis()}",
                senderName = "Sistem",
                isFromUser = false,
                text = "...",
                dayIndex = currentEnriched.currentDayIndex,
                timeLabel = currentEnriched.formattedClockTime
            )
            return DialogueExecutionResult(
                updatedState = currentEnriched,
                replyMessage = fallbackMsg,
                structuredResponse = GeminiDialogueStructuredResponse(replyText = "...", isFromAi = false)
            )
        }

        val canTryAi = aiEnabled && GeminiClient.hasConfiguredApiKey(apiKeyInput)

        if (canTryAi) {
            val persona = CharacterPersonas.getPersonaForNpc(baseEnriched, targetNpc)
            // Build conversation history from prior messages (excluding the optimistic user message just added)
            val priorMessages = baseEnriched.npcs.find { it.id == npcId }?.messages.orEmpty()
            val historyTurns = priorMessages.takeLast(10).map { msg ->
                ConversationTurn(
                    isUser = msg.isFromUser,
                    speakerName = msg.senderName,
                    text = msg.text
                )
            }

            val systemPrompt = CharacterPersonas.buildSystemPrompt(
                state = baseEnriched,
                persona = persona,
                channel = channel,
                currentAffinity = targetNpc.affinity,
                currentTrust = targetNpc.trust,
                importantMemories = targetNpc.importantMemories,
                topicContext = topicContext
            )

            val userDirective = """
                Oyuncunun (${baseEnriched.player.fullName}) yeni mesajı / sözü:
                "$cleanText"

                Lütfen ${persona.displayName} (${persona.roleTitleTr}) olarak Türkçe yanıtını ve oyun içi etkilerini belirtilen JSON formatında üret.
            """.trimIndent()

            val aiResult = GeminiClient.generateCharacterDialogue(
                apiKeyInput = apiKeyInput,
                preferredModelId = modelId,
                systemInstruction = systemPrompt,
                conversationHistory = historyTurns,
                userMessageWithDirective = userDirective
            )

            val structured = aiResult.getOrNull()
            if (structured != null && structured.replyText.isNotBlank()) {
                return applyAiStructuredResponseToNpcState(
                    stateWithUserMsg = currentEnriched,
                    npcId = npcId,
                    userText = cleanText,
                    structured = structured
                )
            }
        }

        // Offline / No-Key Fallback: handle Club President with authoritative Turkish president persona
        if (npcId == "NPC_PRESIDENT") {
            return applyOfflinePresidentDialogue(
                stateWithUserMsg = currentEnriched,
                presidentNpc = targetNpc,
                userText = cleanText
            )
        }

        // Offline / No-Key Fallback using CareerEngine's rich Turkish scripted logic
        val fallbackState = CareerEngine.sendDialogueMessageToNpc(baseEnriched, npcId, cleanText)
        val updatedNpc = fallbackState.npcs.find { it.id == npcId }
        val replyMsg = updatedNpc?.messages?.lastOrNull { !it.isFromUser } ?: ChatMessage(
            id = "MSG_FB_${System.currentTimeMillis()}",
            senderName = targetNpc.name,
            isFromUser = false,
            text = "Anlaşıldı ${baseEnriched.player.firstName}, sahada odaklanmaya devam edelim.",
            dayIndex = fallbackState.currentDayIndex,
            timeLabel = fallbackState.formattedClockTime,
            emotionTag = "Doğal"
        )

        val fallbackStructured = GeminiDialogueStructuredResponse(
            replyText = replyMsg.text,
            emotionTag = replyMsg.emotionTag,
            affinityDelta = (updatedNpc?.affinity ?: targetNpc.affinity) - targetNpc.affinity,
            trustDelta = (updatedNpc?.trust ?: targetNpc.trust) - targetNpc.trust,
            moraleDelta = fallbackState.player.morale - baseEnriched.player.morale,
            managerTrustDelta = fallbackState.managerTrust - baseEnriched.managerTrust,
            suggestedQuickReplies = updatedNpc?.quickReplyOptions.orEmpty(),
            isFromAi = false,
            modelUsed = "offline-scripted"
        )

        return DialogueExecutionResult(
            updatedState = fallbackState,
            replyMessage = replyMsg,
            structuredResponse = fallbackStructured,
            toastSummary = replyMsg.impactSummary,
            usedGeminiAi = false
        )
    }

    /**
     * Generates and applies an Instagram Direct (DM) reply using Gemini AI or offline fallback.
     */
    suspend fun generateAndApplyInstaDmDialogue(
        stateBeforeUserMsg: CareerSaveState,
        currentStateWithUserMsg: CareerSaveState,
        threadId: String,
        userText: String,
        apiKeyInput: String?,
        modelId: String,
        aiEnabled: Boolean = true
    ): DialogueExecutionResult {
        val cleanText = userText.trim()
        val thread = currentStateWithUserMsg.instaDmThreads.find { it.id == threadId }
            ?: stateBeforeUserMsg.instaDmThreads.find { it.id == threadId }

        if (thread == null || cleanText.isEmpty()) {
            val emptyMsg = ChatMessage(
                id = "IDM_EMPTY_${System.currentTimeMillis()}",
                senderName = "Instagram",
                isFromUser = false,
                text = "...",
                dayIndex = currentStateWithUserMsg.currentDayIndex,
                timeLabel = currentStateWithUserMsg.formattedClockTime
            )
            return DialogueExecutionResult(
                updatedState = currentStateWithUserMsg,
                replyMessage = emptyMsg,
                structuredResponse = GeminiDialogueStructuredResponse(replyText = "...", isFromAi = false)
            )
        }

        val canTryAi = aiEnabled && GeminiClient.hasConfiguredApiKey(apiKeyInput)

        if (canTryAi) {
            val persona = CharacterPersonas.getPersonaForInstaDm(stateBeforeUserMsg, thread)
            val priorMessages = stateBeforeUserMsg.instaDmThreads.find { it.id == threadId }?.messages.orEmpty()
            val historyTurns = priorMessages.takeLast(10).map { msg ->
                ConversationTurn(
                    isUser = msg.isFromUser,
                    speakerName = msg.senderName,
                    text = msg.text
                )
            }

            val systemPrompt = CharacterPersonas.buildSystemPrompt(
                state = stateBeforeUserMsg,
                persona = persona,
                channel = DialogueChannelType.INSTAGRAM_DM,
                currentAffinity = 70,
                currentTrust = 70,
                importantMemories = emptyList(),
                topicContext = "Instagram Direct Mesajlaşması (${thread.senderRole})"
            )

            val userDirective = """
                Oyuncunun (${stateBeforeUserMsg.player.fullName}) Instagram DM mesajı:
                "$cleanText"

                Lütfen ${thread.senderName} (${thread.senderHandle}) olarak doğal Türkçe yanıtını ve etkilerini JSON formatında döndür.
            """.trimIndent()

            val aiResult = GeminiClient.generateCharacterDialogue(
                apiKeyInput = apiKeyInput,
                preferredModelId = modelId,
                systemInstruction = systemPrompt,
                conversationHistory = historyTurns,
                userMessageWithDirective = userDirective
            )

            val structured = aiResult.getOrNull()
            if (structured != null && structured.replyText.isNotBlank()) {
                return applyAiStructuredResponseToInstaDm(
                    stateBeforeUserMsg = stateBeforeUserMsg,
                    stateWithUserMsg = currentStateWithUserMsg,
                    threadId = threadId,
                    userText = cleanText,
                    structured = structured
                )
            }
        }

        // Offline fallback via CareerEngine.sendInstaDmMessage
        val fallbackState = CareerEngine.sendInstaDmMessage(stateBeforeUserMsg, threadId, cleanText)
        val updatedThread = fallbackState.instaDmThreads.find { it.id == threadId }
        val replyMsg = updatedThread?.messages?.lastOrNull { !it.isFromUser } ?: ChatMessage(
            id = "IDM_FB_${System.currentTimeMillis()}",
            senderName = thread.senderName,
            isFromUser = false,
            text = "Harikasın ${stateBeforeUserMsg.player.firstName}! Başarılarının devamını diliyoruz 🔥⚽",
            dayIndex = fallbackState.currentDayIndex,
            timeLabel = fallbackState.formattedClockTime
        )

        return DialogueExecutionResult(
            updatedState = fallbackState,
            replyMessage = replyMsg,
            structuredResponse = GeminiDialogueStructuredResponse(
                replyText = replyMsg.text,
                emotionTag = replyMsg.emotionTag,
                moraleDelta = fallbackState.player.morale - stateBeforeUserMsg.player.morale,
                isFromAi = false,
                modelUsed = "offline-scripted"
            ),
            toastSummary = replyMsg.impactSummary,
            usedGeminiAi = false
        )
    }

    private fun applyAiStructuredResponseToNpcState(
        stateWithUserMsg: CareerSaveState,
        npcId: String,
        userText: String,
        structured: GeminiDialogueStructuredResponse
    ): DialogueExecutionResult {
        val targetNpc = stateWithUserMsg.npcs.find { it.id == npcId }!!
        val lowerUser = userText.lowercase()

        var updatedPlayer = stateWithUserMsg.player
        var updatedBalance = stateWithUserMsg.balanceEur
        var updatedContract = stateWithUserMsg.contract
        var updatedOffers = stateWithUserMsg.transferOffers
        var updatedTransactions = stateWithUserMsg.transactions
        var updatedXPosts = stateWithUserMsg.xPosts
        var updatedNews = stateWithUserMsg.newsEvents
        var updatedTeamChemistry = (stateWithUserMsg.teamChemistry + structured.teamChemistryDelta).coerceIn(0, 100)

        val effectiveTrustDelta = if (npcId == "NPC_MANAGER" && structured.managerTrustDelta != 0) {
            structured.managerTrustDelta
        } else {
            structured.trustDelta
        }
        val effectiveAffinityDelta = structured.affinityDelta

        val newManagerTrust = if (npcId == "NPC_MANAGER") {
            (stateWithUserMsg.managerTrust + effectiveTrustDelta).coerceIn(5, 100)
        } else {
            (stateWithUserMsg.managerTrust + structured.managerTrustDelta).coerceIn(5, 100)
        }

        val extraNotes = mutableListOf<String>()

        // Apply morale & fan support deltas
        if (structured.moraleDelta != 0) {
            updatedPlayer = updatedPlayer.copy(
                morale = (updatedPlayer.morale + structured.moraleDelta).coerceIn(0, 100)
            )
            val sign = if (structured.moraleDelta > 0) "+" else ""
            extraNotes.add("Moral $sign${structured.moraleDelta}")
        }
        if (structured.fanSupportDelta != 0) {
            updatedPlayer = updatedPlayer.copy(
                fanSupport = (updatedPlayer.fanSupport + structured.fanSupportDelta).coerceIn(0, 100),
                followersCount = (updatedPlayer.followersCount + structured.fanSupportDelta * 65).coerceAtLeast(0)
            )
            val sign = if (structured.fanSupportDelta > 0) "+" else ""
            extraNotes.add("Taraftar $sign${structured.fanSupportDelta}")
        }
        if (structured.teamChemistryDelta != 0) {
            val sign = if (structured.teamChemistryDelta > 0) "+" else ""
            extraNotes.add("Kimya $sign${structured.teamChemistryDelta}")
        }

        // Manager starting status decision
        var forcedStartingStatus: StartingStatus? = null
        if (npcId == "NPC_MANAGER") {
            when (structured.startingStatusHint) {
                "STARTER" -> {
                    if (newManagerTrust >= 45 && updatedPlayer.energy >= 50 && updatedPlayer.injuryDaysRemaining == 0) {
                        forcedStartingStatus = StartingStatus.STARTER
                        extraNotes.add("Kadro: İLK 11")
                    }
                }
                "BENCH" -> {
                    forcedStartingStatus = StartingStatus.BENCH
                    extraNotes.add("Kadro: Yedek Kulübesi")
                }
            }
        }

        // Agent special triggers: Transfer offer search or wage raise
        if (npcId == "NPC_AGENT") {
            val wantsTransfer = structured.triggerTransferOffer ||
                lowerUser.contains("transfer") || lowerUser.contains("teklif bul")
            if (wantsTransfer && updatedOffers.isEmpty()) {
                val candidateClubs = stateWithUserMsg.clubs.filter {
                    it.id != stateWithUserMsg.currentClub.id && it.teamStrength >= stateWithUserMsg.currentClub.teamStrength - 2
                }.shuffled(Random(System.currentTimeMillis()))
                val targetClub = candidateClubs.firstOrNull()
                if (targetClub != null) {
                    val offeredWage = max(
                        stateWithUserMsg.contract.weeklyWageEur + 1_200L,
                        (updatedPlayer.marketValueEur / 95L).coerceAtLeast(2_500L)
                    )
                    val newOffer = TransferOffer(
                        id = "OFR_AI_${System.currentTimeMillis()}",
                        clubId = targetClub.id,
                        clubName = targetClub.name,
                        leagueId = targetClub.leagueId,
                        leagueName = targetClub.leagueName,
                        clubTier = targetClub.tier,
                        clubStrength = targetClub.teamStrength,
                        primaryColorHex = targetClub.primaryColorHex,
                        offeredWeeklyWageEur = offeredWage,
                        offeredAppearanceBonusEur = offeredWage / 4,
                        offeredGoalBonusEur = offeredWage / 3,
                        contractYears = 3,
                        promisedRole = SquadRole.STARTER,
                        transferType = "Bonservisli Transfer",
                        expiresDayIndex = stateWithUserMsg.currentDayIndex + 14
                    )
                    updatedOffers = listOf(newOffer) + updatedOffers
                    extraNotes.add("Yeni Teklif: ${targetClub.name}")
                }
            }

            if (structured.triggerWageRaise && newManagerTrust >= 55) {
                val raisedWage = (updatedContract.weeklyWageEur * 1.10).toLong()
                updatedContract = updatedContract.copy(weeklyWageEur = raisedWage)
                extraNotes.add("Maaş: ${FLFormatters.formatWage(raisedWage)}")
            }
        }

        // Family gift trigger
        if (npcId == "NPC_FAMILY" && (lowerUser.contains("hediye") || lowerUser.contains("1.500") || lowerUser.contains("gönderiyorum"))) {
            if (updatedBalance >= 1_500L) {
                val giftAmount = 1_500L
                updatedBalance -= giftAmount
                updatedPlayer = updatedPlayer.copy(morale = (updatedPlayer.morale + 8).coerceAtMost(100))
                updatedTransactions = listOf(
                    FinancialTransaction(
                        id = "TX_FAM_AI_${System.currentTimeMillis()}",
                        dayIndex = stateWithUserMsg.currentDayIndex,
                        dateLabel = CareerEngine.formatGameDate(stateWithUserMsg.currentDayIndex, stateWithUserMsg.currentSeasonYear),
                        category = "YAŞAM_TARZI",
                        amountEur = -giftAmount,
                        description = "Aileye Destek & Sürpriz Hediye",
                        balanceAfterEur = updatedBalance
                    )
                ) + updatedTransactions
                extraNotes.add("-1.500 € Hediye")
            }
        }

        // Journalist / Press headline trigger
        if (npcId == "NPC_PRESS" || !structured.triggerPressHeadline.isNullOrBlank()) {
            val headlineText = structured.triggerPressHeadline
                ?: "${updatedPlayer.fullName}: \"${userText.take(48)}\""
            val shortDate = CareerEngine.formatShortDate(stateWithUserMsg.currentDayIndex)
            val pressXp = XPostItem(
                id = "XP_AI_PRESS_${System.currentTimeMillis()}",
                dayIndex = stateWithUserMsg.currentDayIndex,
                dateLabel = shortDate,
                authorName = targetNpc.name,
                handle = "@${targetNpc.name.lowercase().replace(" ", "")}",
                category = "GAZETECİ",
                isVerified = true,
                text = "🚨 ÖZEL | ${stateWithUserMsg.currentClub.name} oyuncusu ${updatedPlayer.fullName}: \"$userText\" — $headlineText 🔥⚽",
                likes = 1_600 + updatedPlayer.followersCount / 8,
                reposts = 240
            )
            val pressNews = NewsEvent(
                id = "NEWS_AI_${System.currentTimeMillis()}",
                dayIndex = stateWithUserMsg.currentDayIndex,
                dateLabel = CareerEngine.formatGameDate(stateWithUserMsg.currentDayIndex, stateWithUserMsg.currentSeasonYear),
                category = "RÖPORTAJ",
                headline = headlineText,
                body = "${stateWithUserMsg.currentClub.name} forması giyen ${updatedPlayer.fullName}, yaptığı özel açıklamayla gündeme oturdu: \"${structured.replyText.take(120)}\"",
                relatedClubName = stateWithUserMsg.currentClub.name
            )
            updatedXPosts = listOf(pressXp) + updatedXPosts
            updatedNews = listOf(pressNews) + updatedNews
            extraNotes.add("Manşet Yayınlandı")
        }

        val impactSummary = buildString {
            append("🤖 AI • ")
            val affSign = if (effectiveAffinityDelta >= 0) "+" else ""
            val trSign = if (effectiveTrustDelta >= 0) "+" else ""
            append("Yakınlık $affSign$effectiveAffinityDelta, Güven $trSign$effectiveTrustDelta")
            if (extraNotes.isNotEmpty()) {
                append(" • ${extraNotes.joinToString(" • ")}")
            }
        }

        val npcReplyMsg = ChatMessage(
            id = "MSG_AI_${System.currentTimeMillis() + 1}",
            senderName = targetNpc.name,
            isFromUser = false,
            text = structured.replyText,
            dayIndex = stateWithUserMsg.currentDayIndex,
            timeLabel = stateWithUserMsg.formattedClockTime,
            emotionTag = structured.emotionTag,
            impactSummary = impactSummary
        )

        val updatedMemories = if (!structured.newMemoryNote.isNullOrBlank()) {
            (targetNpc.importantMemories + structured.newMemoryNote).takeLast(8)
        } else {
            targetNpc.importantMemories
        }

        val updatedQuickReplies = if (structured.suggestedQuickReplies.isNotEmpty()) {
            structured.suggestedQuickReplies
        } else {
            targetNpc.quickReplyOptions
        }

        val updatedNpcs = stateWithUserMsg.npcs.map { npc ->
            if (npc.id == npcId) {
                // Ensure the user's message is present if stateWithUserMsg didn't have it yet
                val hasRecentUserMsg = npc.messages.lastOrNull()?.let { it.isFromUser && it.text == userText } == true
                val baseMessages = if (hasRecentUserMsg) {
                    npc.messages
                } else {
                    npc.messages + ChatMessage(
                        id = "MSG_USR_${System.currentTimeMillis()}",
                        senderName = stateWithUserMsg.player.firstName,
                        isFromUser = true,
                        text = userText,
                        dayIndex = stateWithUserMsg.currentDayIndex,
                        timeLabel = stateWithUserMsg.formattedClockTime
                    )
                }

                npc.copy(
                    affinity = (npc.affinity + effectiveAffinityDelta).coerceIn(0, 100),
                    trust = (npc.trust + effectiveTrustDelta).coerceIn(0, 100),
                    currentMood = structured.emotionTag,
                    importantMemories = updatedMemories,
                    quickReplyOptions = updatedQuickReplies,
                    messages = baseMessages + npcReplyMsg,
                    unreadCount = 0
                )
            } else npc
        }

        val nextStatus = forcedStartingStatus ?: stateWithUserMsg.nextMatchStartingStatus

        val finalState = stateWithUserMsg.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = updatedPlayer,
            balanceEur = updatedBalance,
            contract = updatedContract,
            transferOffers = updatedOffers,
            transactions = updatedTransactions,
            xPosts = updatedXPosts,
            newsEvents = updatedNews,
            teamChemistry = updatedTeamChemistry,
            managerTrust = newManagerTrust,
            nextMatchStartingStatus = nextStatus,
            npcs = updatedNpcs
        )

        return DialogueExecutionResult(
            updatedState = finalState,
            replyMessage = npcReplyMsg,
            structuredResponse = structured,
            toastSummary = impactSummary,
            usedGeminiAi = true
        )
    }

    private fun applyAiStructuredResponseToInstaDm(
        stateBeforeUserMsg: CareerSaveState,
        stateWithUserMsg: CareerSaveState,
        threadId: String,
        userText: String,
        structured: GeminiDialogueStructuredResponse
    ): DialogueExecutionResult {
        // Also run CareerEngine's sponsor/scout bonus triggers if applicable, then replace the reply text with Gemini's response
        val engineProcessed = CareerEngine.sendInstaDmMessage(stateBeforeUserMsg, threadId, userText)
        val thread = engineProcessed.instaDmThreads.find { it.id == threadId }
            ?: stateWithUserMsg.instaDmThreads.find { it.id == threadId }!!

        val engineLastReply = thread.messages.lastOrNull { !it.isFromUser }
        val combinedImpact = buildString {
            append("🤖 AI")
            if (structured.moraleDelta != 0) {
                val s = if (structured.moraleDelta > 0) "+" else ""
                append(" • Moral $s${structured.moraleDelta}")
            }
            if (!engineLastReply?.impactSummary.isNullOrBlank()) {
                append(" • ${engineLastReply?.impactSummary}")
            }
        }

        val aiReplyMsg = ChatMessage(
            id = "IDM_AI_${System.currentTimeMillis() + 1}",
            senderName = thread.senderName,
            isFromUser = false,
            text = structured.replyText,
            dayIndex = engineProcessed.currentDayIndex,
            timeLabel = engineProcessed.formattedClockTime,
            emotionTag = structured.emotionTag,
            impactSummary = combinedImpact
        )

        val updatedThreads = engineProcessed.instaDmThreads.map { th ->
            if (th.id == threadId) {
                val msgsWithoutEngineReply = if (th.messages.lastOrNull()?.isFromUser == false) {
                    th.messages.dropLast(1)
                } else {
                    th.messages
                }
                th.copy(
                    isPendingRequest = false,
                    unreadCount = 0,
                    messages = msgsWithoutEngineReply + aiReplyMsg
                )
            } else th
        }

        val updatedPlayer = engineProcessed.player.copy(
            morale = (engineProcessed.player.morale + structured.moraleDelta).coerceIn(0, 100),
            fanSupport = (engineProcessed.player.fanSupport + structured.fanSupportDelta).coerceIn(0, 100)
        )

        val finalState = engineProcessed.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = updatedPlayer,
            instaDmThreads = updatedThreads
        )

        return DialogueExecutionResult(
            updatedState = finalState,
            replyMessage = aiReplyMsg,
            structuredResponse = structured,
            toastSummary = combinedImpact,
            usedGeminiAi = true
        )
    }

    private fun applyOfflinePresidentDialogue(
        stateWithUserMsg: CareerSaveState,
        presidentNpc: NpcCharacter,
        userText: String
    ): DialogueExecutionResult {
        val lower = userText.lowercase()
        val club = stateWithUserMsg.currentClub
        val player = stateWithUserMsg.player

        val isDisrespectful = listOf("saçma", "bıktım", "nefret", "istifa", "para ver", "kötü").any { lower.contains(it) }
        val isAboutBonusOrWage = listOf("prim", "maaş", "zam", "kontrat", "ödül", "bütçe").any { lower.contains(it) }
        val isAboutChampionshipOrLoyalty = listOf("şampiyon", "kupa", "arma", "camia", "söz", "başkanım", "hedef").any { lower.contains(it) }

        val affinityDelta = when {
            isDisrespectful -> -6
            isAboutChampionshipOrLoyalty -> +6
            else -> +3
        }
        val trustDelta = when {
            isDisrespectful -> -5
            isAboutChampionshipOrLoyalty -> +5
            else -> +2
        }
        val moraleDelta = when {
            isDisrespectful -> -3
            isAboutChampionshipOrLoyalty -> +5
            else -> +2
        }

        val replyText = when {
            isDisrespectful ->
                "Evladım, ${club.name} yönetim kurulu başkanı olarak bu üslubu camiamıza yakıştıramadım. Biz senden saha içinde profesyonellik ve armaya saygı bekliyoruz!"
            isAboutBonusOrWage ->
                "Yönetim kurulu olarak sahadaki alın terinin karşılığını her zaman veririz ${player.firstName}. Bu hafta alacağımız galibiyetten sonra soyunma odasına özel galibiyet primi açıklayacağım."
            isAboutChampionshipOrLoyalty ->
                "İşte görmek istediğim ${club.name} ruhu bu evladım! Yönetim olarak teknik heyetimizin ve senin sonuna kadar arkandayız. O kupa bu sezon stadyumumuza gelecek!"
            else ->
                "Teşekkür ederim ${player.firstName}. Camiamızın ve taraftarımızın senden beklentisi büyük. Tesislerde bir eksiğin olursa doğrudan bana veya sportif direktörümüze iletebilirsin."
        }

        val impactSummary = "Yakınlık ${if (affinityDelta >= 0) "+$affinityDelta" else "$affinityDelta"}, Güven ${if (trustDelta >= 0) "+$trustDelta" else "$trustDelta"} • Moral ${if (moraleDelta >= 0) "+$moraleDelta" else "$moraleDelta"}"

        val replyMsg = ChatMessage(
            id = "MSG_PRES_${System.currentTimeMillis() + 1}",
            senderName = presidentNpc.name,
            isFromUser = false,
            text = replyText,
            dayIndex = stateWithUserMsg.currentDayIndex,
            timeLabel = stateWithUserMsg.formattedClockTime,
            emotionTag = if (isDisrespectful) "Ciddi" else "Gururlu",
            impactSummary = impactSummary
        )

        val quickReplies = listOf(
            "Başkanım, ${club.name} arması için sahada her şeyimi vereceğim.",
            "Bu hafta galibiyet primiyle takımın motivasyonu zirveye çıkar başkanım.",
            "Camiamıza şampiyonluk sevinci yaşatmaya kararlıyız!"
        )

        val updatedNpcs = stateWithUserMsg.npcs.map { npc ->
            if (npc.id == "NPC_PRESIDENT") {
                val hasRecentUserMsg = npc.messages.lastOrNull()?.let { it.isFromUser && it.text == userText } == true
                val baseMessages = if (hasRecentUserMsg) {
                    npc.messages
                } else {
                    npc.messages + ChatMessage(
                        id = "MSG_USR_${System.currentTimeMillis()}",
                        senderName = player.firstName,
                        isFromUser = true,
                        text = userText,
                        dayIndex = stateWithUserMsg.currentDayIndex,
                        timeLabel = stateWithUserMsg.formattedClockTime
                    )
                }
                npc.copy(
                    affinity = (npc.affinity + affinityDelta).coerceIn(0, 100),
                    trust = (npc.trust + trustDelta).coerceIn(0, 100),
                    currentMood = if (isDisrespectful) "Mesafeli" else "Gururlu",
                    quickReplyOptions = quickReplies,
                    messages = baseMessages + replyMsg,
                    unreadCount = 0
                )
            } else npc
        }

        val updatedState = stateWithUserMsg.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = player.copy(morale = (player.morale + moraleDelta).coerceIn(0, 100)),
            npcs = updatedNpcs
        )

        return DialogueExecutionResult(
            updatedState = updatedState,
            replyMessage = replyMsg,
            structuredResponse = GeminiDialogueStructuredResponse(
                replyText = replyText,
                emotionTag = replyMsg.emotionTag,
                affinityDelta = affinityDelta,
                trustDelta = trustDelta,
                moraleDelta = moraleDelta,
                suggestedQuickReplies = quickReplies,
                isFromAi = false,
                modelUsed = "offline-scripted"
            ),
            toastSummary = impactSummary,
            usedGeminiAi = false
        )
    }
}
