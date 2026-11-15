package com.example.engine

import com.example.model.*

enum class CameraShot { ESTABLISHING, MEDIUM, CLOSE_UP, OVER_SHOULDER, TWO_SHOT, PUSH_IN, PAN, FOCUS }
data class SceneCharacterPosition(val character: String, val x: Float, val facing: Int)
data class CameraCue(val shot: CameraShot, val zoom: Float, val panX: Float, val durationMs: Int)
data class AnimationCue(val actor: String, val state: String, val startMs: Int, val durationMs: Int)
data class SceneChoice(val id: String, val label: String, val consequence: String, val enabled: Boolean = true)
data class SceneDefinition(
    val type: SceneType,
    val environmentId: String,
    val characterPositions: List<SceneCharacterPosition>,
    val cameraSequence: List<CameraCue>,
    val animationTimeline: List<AnimationCue>,
    val lightingProfile: String,
    val ambientAudio: String,
    val dialogueNodes: List<String>,
    val availableChoices: List<SceneChoice>,
    val completionConditions: String = "VALIDATED_CHOICE",
    val skipBehavior: String = "REQUIRE_CHOICE",
    val saveCheckpoint: Boolean = true,
    val followUpEvents: List<String> = emptyList()
)

object CinematicEngine {
    fun definition(state: CareerSaveState, scene: SceneCheckpoint): SceneDefinition {
        val task = state.livingWorld.tasks.first { it.id == scene.taskId }
        val type = scene.sceneType
        val report = state.lastMatchReport
        val performance = report?.let { "Son maçta ${it.userRating} reyting, ${it.userGoals} gol ve ${it.userAssists} asist." } ?: "Yeni maç raporun henüz yok."
        val npcId = task.npcId ?: "NPC_MANAGER"
        val intro = if (task.action == "DILEMMA") state.activeDilemma?.narrative ?: "Görüşme tamamlandı." else when (type) {
            SceneType.COACH -> "${state.player.firstName}, bugünkü çalışmanı ve kadro rekabetini konuşalım. Güvenim ${state.managerTrust}/100. $performance"
            SceneType.MEDICAL -> if (task.action == "TRANSFER_MEDICAL") "Transfer sağlık değerlendirmesi. Aktif sakatlık: ${state.player.injuryDaysRemaining} gün. Sağlık onayı olmadan imzaya geçilmeyecek."
                else "${state.livingWorld.medical.diagnosis}. İlk tahmin ${state.livingWorld.medical.initialRecoveryDays} gün; kalan süre ${state.player.injuryDaysRemaining} gün. Tam yüke dönüşü birlikte planlayacağız."
            SceneType.TRANSFER -> state.transferOffers.find { it.id == task.entityId }?.let { offer ->
                val process = TransferEngine.process(state, offer)
                "${offer.clubName}, ${process.terms.years} yıl için haftalık ${process.terms.weeklyWageEur} € öneriyor. Rol: ${process.terms.role.labelTr}. Son tarih: ${GameClock.short(offer.expiresDayIndex, state.currentSeasonYear)}. ${process.outcome}"
            } ?: "Bu transfer dosyası artık geçerli değil."
            SceneType.PRE_MATCH -> "Bugünkü rolün: ${state.nextMatchStartingStatus.labelTr}. ${state.currentClub.managerTacticalStyle} planına bağlı kal. ${if (state.player.injuryDaysRemaining > 0) "Sakatlığın nedeniyle sahada yer alamayacaksın." else "Enerjin ${state.player.energy}/100. Takım arkadaşlarınla yardımlaşmanı bekliyorum."}"
            SceneType.POST_MATCH -> report?.let { "${it.homeClubName} ${it.homeScore} – ${it.awayScore} ${it.awayClubName}. ${it.managerFeedback} $performance" } ?: performance
            SceneType.PRESS -> "$performance Bugünkü performansında takım arkadaşlarının payını ve kendi oyununu nasıl değerlendiriyorsun?"
            SceneType.SOCIAL -> "Bugün maç programın uygun. Futbol dışındaki hayatına da zaman ayıralım. İstersen burada akşam yemeği yiyebiliriz; hesap 80 €."
            SceneType.SIGNING -> "Sağlık kontrolü onaylandı. Sözleşme şartlarını son kez gözden geçir. İmza, hedef ligin tescil kontrolünden sonra kulüp ve finans kayıtlarına işlenecek."
            SceneType.WELCOME -> "${state.currentClub.name} ailesine hoş geldin. Yeni takım arkadaşlarınla ve teknik ekibinle tanışma zamanı."
        }
        val choices = when {
            task.action == "DILEMMA" -> state.activeDilemma?.choices?.map { SceneChoice(it.id, it.label, it.description, it.moneyDeltaEur >= 0 || state.balanceEur >= -it.moneyDeltaEur) }.orEmpty()
            task.action == "TRANSFER_MEDICAL" -> listOf(SceneChoice("medical_transfer", "Sağlık kontrolünü tamamla", "Aktif sakatlık varsa onay bekler.", state.player.injuryDaysRemaining == 0))
            type == SceneType.COACH -> listOf(SceneChoice("work", "Çalışarak formayı kazanacağım", "Güven +2; kadro performansla belirlenir."), SceneChoice("minutes", "Daha fazla süre istiyorum", "Talep hafızaya kaydedilir; forma garantisi verilmez."), SceneChoice("rest", "Yükümü birlikte planlayalım", "Güven +1; kontrollü çalışma talebi."))
            type == SceneType.MEDICAL -> listOf(SceneChoice("physio", "Fizyoterapi programı", "Günlük rehabilitasyon; tam yükte antrenman kapalı."), SceneChoice("safe", "Daha güvenli dönüş planı", "İyileşme hedefi iki gün uzar; tam iyileşme beklenir."), SceneChoice("second", "İkinci değerlendirme iste", "Yeni kontrol yarına planlanır."))
            type == SceneType.TRANSFER -> {
                val offer = state.transferOffers.find { it.id == task.entityId }
                val process = offer?.let { TransferEngine.process(state, it) }
                listOf(SceneChoice("negotiate", "Şartları yeniden görüş", "Maaş, rol, prim ve süre için karşı teklif ver.", process?.stage in setOf(TransferStage.FORMAL_OFFER, TransferStage.NEGOTIATION)),
                    SceneChoice("agree", "Şartlarda anlaş", "Sağlık kontrolü ve imza randevusu açılır.", process?.stage in setOf(TransferStage.FORMAL_OFFER, TransferStage.NEGOTIATION)),
                    SceneChoice("wait", "Düşünmek için zaman iste", "Dosya ve son tarih korunur."), SceneChoice("reject", "Teklifi reddet", "Bu dosya kapatılır."))
            }
            type == SceneType.PRE_MATCH -> listOf(SceneChoice("ready", "Takım için hazırım", "Maç başlar; mevcut kadro kararı korunur."), SceneChoice("professional", "Rolüme profesyonelce odaklanacağım", "Güven +1; mevcut kadro kararı korunur."))
            type == SceneType.POST_MATCH -> listOf(SceneChoice("team", "Takımın katkısını vurgula", "Takım uyumu +1."), SceneChoice("improve", "Geliştireceğim noktaları konuş", "Güven +1."))
            type == SceneType.PRESS -> listOf(SceneChoice("publish", "Kendi açıklamamı yayımla", "Yazdığın cümle aynen basına verilir.", scene.transcript.any { it.isFromUser }), SceneChoice("decline", "Bugün açıklama yapmayacağım", "Görüşme ödülsüz kapanır."))
            type == SceneType.SOCIAL -> listOf(SceneChoice("dinner", "Yemeğe kal", "80 €; moral +5, yakınlık +3; üç saat.", state.balanceEur >= 80 && state.socialActionCountToday < 2), SceneChoice("chat", "Kısa bir sohbet et", "Yakınlık +1; bir saat."), SceneChoice("leave", "Başka bir zamana bırak", "Görüşme sonuçsuz kapanır."))
            type == SceneType.SIGNING -> listOf(SceneChoice("sign", "İmzala ve tescili onayla", "Bütün transfer şartları yeniden doğrulanır."))
            else -> listOf(SceneChoice("welcome", "Takımla tanış", "Tanışma kariyer geçmişine kaydedilir."))
        }
        return SceneDefinition(type, scene.environmentId,
            listOf(SceneCharacterPosition("PLAYER", .20f, 1), SceneCharacterPosition(scene.characterId, .72f, -1)),
            listOf(CameraCue(CameraShot.ESTABLISHING, 1f, 0f, 1000), CameraCue(CameraShot.PUSH_IN, 1.08f, -.025f, 1800), CameraCue(CameraShot.CLOSE_UP, 1.14f, -.05f, 1400)),
            listOf(AnimationCue(scene.characterId, "IDLE_BREATHING", 0, 2400), AnimationCue(scene.characterId, "SPEAKING", 1000, 1600)),
            if (type == SceneType.SOCIAL) "WARM_EVENING" else "DAYLIGHT", type.environment,
            listOf(intro, NpcDialogueEngine.reply(state, npcId, "görüşme").dialogue), choices)
    }

    fun choose(input: CareerSaveState, choiceId: String): Pair<CareerSaveState, String> {
        val scene = input.livingWorld.activeScene ?: return input to "Aktif sahne yok."
        if (scene.status != SceneStatus.AWAITING_CHOICE) return input to "Önce diyaloğu ilerlet."
        val task = input.livingWorld.tasks.find { it.id == scene.taskId && it.isActive } ?: return input to "Görev artık geçerli değil."
        val choice = definition(input, scene).availableChoices.find { it.id == choiceId && it.enabled } ?: return input to "Bu karar şu an uygulanamaz."
        val eventId = "DECISION:${scene.sceneId}"
        if (eventId in input.livingWorld.appliedEventIds) return input to "Karar zaten uygulandı."
        var state = input
        var outcome = choice.consequence
        var finish = true
        when {
            task.action == "DILEMMA" -> {
                val selected = state.activeDilemma?.choices?.find { it.id == choiceId } ?: return input to "Karar geçersiz."
                val result = CareerEngine.resolveDynamicDilemma(state, selected); state = result.first; outcome = result.second
            }
            task.action == "TRANSFER_MEDICAL" -> { val result = TransferEngine.medical(state, task.entityId ?: ""); state = result.first; outcome = result.second; if (state == input) return state to outcome }
            scene.sceneType == SceneType.TRANSFER -> when (choiceId) {
                "negotiate" -> return state to "Aşağıdaki sözleşme alanlarını düzenleyip karşı teklif gönder."
                "agree" -> { val result = TransferEngine.agree(state, task.entityId ?: ""); state = result.first; outcome = result.second; if (state == input) return state to outcome }
                "reject" -> { state = TransferEngine.reject(state, task.entityId ?: ""); outcome = "Teklif reddedildi." }
                "wait" -> return LivingWorldEngine.suspendScene(state) to "Dosya açık tutuldu; son tarihten önce dönebilirsin."
            }
            scene.sceneType == SceneType.SIGNING -> {
                val result = TransferEngine.register(state, task.entityId ?: ""); state = result.first; outcome = result.second
                if (state.contract.clubId == input.contract.clubId) return state to outcome
            }
            scene.sceneType == SceneType.MEDICAL -> {
                val medical = state.livingWorld.medical
                if (choiceId == "second") {
                    val followId = "${task.id}:SECOND"
                    if (state.livingWorld.tasks.none { it.id == followId }) state = state.copy(livingWorld = state.livingWorld.copy(tasks = state.livingWorld.tasks + task.copy(
                        id = followId, sourceEventId = "SOURCE:$followId", dayIndex = state.currentDayIndex + 1, status = TaskStatus.LOCKED)))
                    outcome = "İkinci değerlendirme yarına kaydedildi. Mevcut dinlenme kısıtları sürüyor."
                } else {
                    state = state.copy(player = state.player.copy(injuryDaysRemaining = state.player.injuryDaysRemaining + if (choiceId == "safe") 2 else 0),
                        livingWorld = state.livingWorld.copy(medical = medical.copy(diagnosed = true,
                            plan = if (choiceId == "safe") RecoveryPlan.CONSERVATIVE else RecoveryPlan.PHYSIOTHERAPY,
                            initialRecoveryDays = medical.initialRecoveryDays + if (choiceId == "safe") 2 else 0)))
                    outcome = "${state.livingWorld.medical.plan.name} planı kaydedildi. Kalan süre ${state.player.injuryDaysRemaining} gün."
                }
            }
            scene.sceneType == SceneType.COACH -> {
                val delta = if (choiceId == "work") 2 else if (choiceId == "rest") 1 else 0
                state = state.copy(managerTrust = (state.managerTrust + delta).coerceIn(0, 100))
                outcome = "${choice.label}. Güven ${if (delta >= 0) "+" else ""}$delta; sonraki kadro kararı performans verileriyle hesaplanacak."
            }
            scene.sceneType == SceneType.PRE_MATCH -> {
                if (choiceId == "professional") state = state.copy(managerTrust = (state.managerTrust + 1).coerceAtMost(100))
                finish = false; outcome = "Maç öncesi konuşma tamamlandı. Rolün: ${state.nextMatchStartingStatus.labelTr}."
            }
            scene.sceneType == SceneType.POST_MATCH -> state = if (choiceId == "team") state.copy(teamChemistry = (state.teamChemistry + 1).coerceAtMost(100)) else state.copy(managerTrust = (state.managerTrust + 1).coerceAtMost(100))
            scene.sceneType == SceneType.PRESS -> {
                if (choiceId == "publish") {
                    val text = scene.transcript.lastOrNull { it.isFromUser }?.text ?: return state to "Önce kendi açıklamanı yaz."
                    val result = CareerEngine.publishPressStatementOrSocialPost(state, text, "MÜTEVAZI_TAKIMCI"); state = result.first; outcome = result.second
                } else state = state.copy(pressCompletedForLastMatch = true)
            }
            scene.sceneType == SceneType.SOCIAL -> {
                val cost = if (choiceId == "dinner") 80L else 0L
                val hours = if (choiceId == "dinner") 3 else 1
                if (state.currentHour + hours > 23) return state to "Bu etkinlik için bugün yeterli zaman kalmadı."
                state = state.copy(balanceEur = state.balanceEur - cost, currentHour = state.currentHour + hours,
                    socialActionCountToday = state.socialActionCountToday + 1,
                    player = state.player.copy(morale = (state.player.morale + if (choiceId == "dinner") 5 else 0).coerceAtMost(100)),
                    npcs = state.npcs.map { if (it.id == task.npcId) it.copy(affinity = (it.affinity + if (choiceId == "dinner") 3 else if (choiceId == "chat") 1 else 0).coerceAtMost(100)) else it })
                if (cost > 0) state = state.copy(transactions = listOf(FinancialTransaction("TX:$eventId", state.currentDayIndex,
                    GameClock.full(state.currentDayIndex, state.currentSeasonYear), "YAŞAM_TARZI", -cost, "Aileyle akşam yemeği", state.balanceEur)) + state.transactions)
            }
        }
        if (finish) state = LivingWorldEngine.finishTask(state, task.id, outcome)
        state = LivingWorldEngine.remember(state, NpcMemory(eventId, scene.characterId, state.currentDayIndex, task.title, choice.label, 3, eventId))
        val completed = scene.copy(status = SceneStatus.COMPLETED, selectedChoiceId = choiceId, outcome = outcome)
        state = state.copy(livingWorld = state.livingWorld.copy(activeScene = completed,
            sceneHistory = state.livingWorld.sceneHistory.filterNot { it.sceneId == scene.sceneId } + completed,
            appliedEventIds = state.livingWorld.appliedEventIds + eventId),
            eventHistory = listOf(GameEvent(eventId, state.currentDayIndex, 0, "CINEMATIC_DECISION", task.title, outcome)) + state.eventHistory)
        return LivingWorldEngine.reconcile(state) to outcome
    }
}
