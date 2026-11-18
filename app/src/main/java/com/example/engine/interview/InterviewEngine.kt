package com.example.engine.interview

import com.example.engine.CareerEngine
import com.example.model.CareerSaveState
import com.example.model.ChatMessage
import com.example.model.CompletedMatchReport
import com.example.model.GameEvent
import com.example.model.InstaCommentItem
import com.example.model.NewsEvent
import com.example.model.SocialMediaPost
import com.example.model.StoryItem
import com.example.model.XPostItem
import com.example.ui.components.FLFormatters
import kotlin.math.abs
import kotlin.random.Random

enum class InterviewTone(
    val code: String,
    val labelTr: String,
    val shortBadgeTr: String,
    val taglineTr: String
) {
    HUMBLE(
        code = "MÜTEVAZI_TAKIMCI",
        labelTr = "Mütevazı & Takımcı",
        shortBadgeTr = "MÜTEVAZI",
        taglineTr = "Takımı, hocayı ve taraftarı öne çıkarır"
    ),
    CONFIDENT(
        code = "İDDİALI_ÖZGÜVENLİ",
        labelTr = "İddialı & Özgüvenli",
        shortBadgeTr = "ÖZ GÜVENLİ",
        taglineTr = "Karizma, liderlik ve yıldız hedefi vurgusu"
    ),
    PROVOCATIVE(
        code = "TARTIŞMALI_SERT",
        labelTr = "Tartışmalı & Sert",
        shortBadgeTr = "PROVOKATİF",
        taglineTr = "Manşetlik, keskin ve gündem sarsıcı çıkış"
    );

    companion object {
        fun fromCode(code: String): InterviewTone {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: CONFIDENT
        }
    }
}

enum class InterviewTopic(
    val labelTr: String,
    val badgeTr: String
) {
    MATCH_RESULT("Maç Sonucu & Skor Analizi", "MAÇ SONUCU"),
    PLAYER_PERFORMANCE("Bireysel Performans & İstatistik", "PERFORMANS"),
    COACH_TACTICS("Teknik Direktör & Taktik Disiplin", "TEKNİK HEYET"),
    CONTROVERSY_MOMENT("Kritik Dakikalar & Tansiyon", "KRİTİK POZİSYON"),
    RIVALRY_ATMOSPHERE("Derbi & Tribün Atmosferi", "REKABET & TRİBÜN"),
    TRANSFER_RUMORS("Transfer Kulisi & Gelecek Planı", "TRANSFER GÜNDEMİ")
}

data class TvReporterPersona(
    val id: String,
    val channelName: String,
    val micFlagLabel: String,
    val reporterName: String,
    val reporterTitle: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val micFoamColorHex: String
)

data class InterviewAnswerOption(
    val id: String,
    val tone: InterviewTone,
    val shortTitle: String,
    val spokenText: String,
    val reporterReactionText: String,
    val headlinePreview: String,
    val moraleDelta: Int,
    val fanSupportDelta: Int,
    val managerTrustDelta: Int,
    val teamChemistryDelta: Int,
    val followersDelta: Int
)

data class InterviewQuestion(
    val id: String,
    val questionIndex: Int,
    val totalQuestions: Int,
    val topic: InterviewTopic,
    val reporter: TvReporterPersona,
    val contextSubtitle: String,
    val questionText: String,
    val options: List<InterviewAnswerOption>
)

data class AnsweredInterviewItem(
    val question: InterviewQuestion,
    val selectedOption: InterviewAnswerOption,
    val customSpokenOverride: String? = null
) {
    val effectiveQuote: String
        get() = customSpokenOverride?.takeIf { it.isNotBlank() } ?: selectedOption.spokenText
}

data class InterviewSessionResult(
    val fixtureId: String,
    val answeredItems: List<AnsweredInterviewItem>,
    val dominantTone: InterviewTone,
    val mainHeadline: String,
    val subHeadline: String,
    val bestQuoteText: String,
    val totalMoraleDelta: Int,
    val totalFanSupportDelta: Int,
    val totalManagerTrustDelta: Int,
    val totalTeamChemistryDelta: Int,
    val totalFollowersDelta: Int,
    val usedDynamicAiQuestions: Boolean = false
)

object InterviewEngine {

    /**
     * Optional hook that @diyalog's Gemini engine can register to generate dynamic context-aware questions.
     * When null, offline, or returning null/empty, [buildOfflineQuestions] provides a deep Turkish question bank.
     */
    @Volatile
    var dynamicQuestionProvider: (suspend (CareerSaveState, CompletedMatchReport) -> List<InterviewQuestion>?)? = null

    /**
     * Stores the most recently completed interview session so social feed / UI components can inspect it.
     */
    @Volatile
    var lastCompletedSession: InterviewSessionResult? = null

    val reportersCatalog: List<TvReporterPersona> = listOf(
        TvReporterPersona(
            id = "REP_BEIN",
            channelName = "beIN SPORTS HD 1",
            micFlagLabel = "beIN",
            reporterName = "Nazlı Canyurt",
            reporterTitle = "Yayıncı Kuruluş Saha Muhabiri",
            primaryColorHex = "#5C2D91",
            secondaryColorHex = "#00E5FF",
            micFoamColorHex = "#3B1963"
        ),
        TvReporterPersona(
            id = "REP_TRT",
            channelName = "TRT SPOR YILDIZ",
            micFlagLabel = "TRT",
            reporterName = "Serkan Yetkin",
            reporterTitle = "Flash Röportaj Muhabiri",
            primaryColorHex = "#E11D48",
            secondaryColorHex = "#FFFFFF",
            micFoamColorHex = "#9F1239"
        ),
        TvReporterPersona(
            id = "REP_SD",
            channelName = "SPORTS DIGITALE",
            micFlagLabel = "SD",
            reporterName = "Yağız Sabuncuoğlu",
            reporterTitle = "Transfer & Kulis Muhabiri",
            primaryColorHex = "#FF6B00",
            secondaryColorHex = "#111827",
            micFoamColorHex = "#C2410C"
        ),
        TvReporterPersona(
            id = "REP_ASPOR",
            channelName = "A SPOR CANLI",
            micFlagLabel = "A SPOR",
            reporterName = "Emre Kaplan",
            reporterTitle = "Mixed Zone Muhabiri",
            primaryColorHex = "#DC2626",
            secondaryColorHex = "#FBBF24",
            micFoamColorHex = "#991B1B"
        ),
        TvReporterPersona(
            id = "REP_SSPORT",
            channelName = "S SPORT PLUS",
            micFlagLabel = "S+",
            reporterName = "Caner Eler",
            reporterTitle = "Taktik & Maç Sonu Analisti",
            primaryColorHex = "#0284C7",
            secondaryColorHex = "#38BDF8",
            micFoamColorHex = "#075985"
        )
    )

    suspend fun generateQuestionsWithOptionalAi(
        career: CareerSaveState,
        report: CompletedMatchReport,
        userApiKey: String? = null,
        preferredModelId: String = "gemini-4-hydrogen"
    ): Pair<List<InterviewQuestion>, Boolean> {
        val baseOffline = buildOfflineQuestions(career, report)
        val provider = dynamicQuestionProvider
        if (provider != null) {
            val aiQuestions = runCatching { provider.invoke(career, report) }.getOrNull()
            if (!aiQuestions.isNullOrEmpty() && aiQuestions.size >= 3) {
                return aiQuestions.take(5) to true
            }
        }

        // Also integrate directly with @diyalog's GeminiClient if an API key is configured
        if (runCatching { com.example.ai.GeminiClient.hasConfiguredApiKey(userApiKey) }.getOrDefault(false)) {
            val aiDynamicQ = runCatching {
                generateSingleGeminiFlashQuestion(
                    career = career,
                    report = report,
                    questionIndex = baseOffline.size,
                    totalQuestions = baseOffline.size,
                    userApiKey = userApiKey,
                    preferredModelId = preferredModelId
                )
            }.getOrNull()
            if (aiDynamicQ != null) {
                val merged = baseOffline.dropLast(1) + aiDynamicQ.copy(
                    questionIndex = baseOffline.size,
                    totalQuestions = baseOffline.size
                )
                return merged to true
            }
        }

        return baseOffline to false
    }

    private suspend fun generateSingleGeminiFlashQuestion(
        career: CareerSaveState,
        report: CompletedMatchReport,
        questionIndex: Int,
        totalQuestions: Int,
        userApiKey: String?,
        preferredModelId: String
    ): InterviewQuestion? {
        val club = career.currentClub
        val ratingStr = FLFormatters.formatRating(report.userRating)
        val reporter = reportersCatalog[2] // Sports Digitale - Yağız Sabuncuoğlu
        val systemPrompt = """
            Sen Türkiye'de maç sonu Mixed Zone (Flash Röportaj) alanında görev yapan ünlü spor muhabiri Yağız Sabuncuoğlu'sun.
            Futbolcuya maç sonu canlı yayında tek bir keskin, gerçekçi Türkçe soru soracaksın ve oyuncunun verebileceği 3 farklı üslupta (1: Mütevazı, 2: Öz Güvenli, 3: Provokatif) yanıt önerisini JSON içindeki suggestedQuickReplies dizisinde (tam 3 elemanlı) döndüreceksin.
        """.trimIndent()
        val userPrompt = """
            Maç: ${report.homeClubName} ${report.homeScore} - ${report.awayScore} ${report.awayClubName} (Hafta ${report.week}).
            Oyuncu: ${career.player.fullName} (${club.name}, Pozisyon: ${career.player.primaryPosition.titleTr}).
            Bu maçtaki performansı: $ratingStr reyting, ${report.userGoals} gol, ${report.userAssists} asist, ${report.userPlayedMinutes} dakika.
            Teknik Direktör: ${club.managerName} (Hoca Güveni: ${career.managerTrust}/100, Taraftar Desteği: ${career.player.fanSupport}/100).
            Lütfen replyText alanına muhabirin canlı yayındaki sorusunu, suggestedQuickReplies dizisine ise sırasıyla [Mütevazı yanıt, İddialı yanıt, Provokatif yanıt] olacak şekilde 3 Türkçe futbolcu cevabı yaz.
        """.trimIndent()

        val res = com.example.ai.GeminiClient.generateCharacterDialogue(
            apiKeyInput = userApiKey,
            preferredModelId = preferredModelId,
            systemInstruction = systemPrompt,
            conversationHistory = emptyList(),
            userMessageWithDirective = userPrompt,
            temperature = 0.8
        ).getOrNull() ?: return null

        val qText = res.replyText.trim().removeSurrounding("\"")
        if (qText.length < 15) return null
        val replies = res.suggestedQuickReplies
        val humbleReply = replies.getOrNull(0)?.takeIf { it.isNotBlank() }
            ?: "Takım arkadaşlarımla birlikte formamızın hakkını vermek için son düdüğe kadar mücadele ettik."
        val confidentReply = replies.getOrNull(1)?.takeIf { it.isNotBlank() }
            ?: "Sahada kalitemi ve liderliğimi göstermeye devam edeceğim, bu sezon hedeflerim çok büyük!"
        val provocativeReply = replies.getOrNull(2)?.takeIf { it.isNotBlank() }
            ?: "Bizi eleştirenlere en güzel cevabı çimlerin üzerinde verdik, herkes işine baksın!"

        return InterviewQuestion(
            id = "Q_GEMINI_${report.fixtureId}",
            questionIndex = questionIndex,
            totalQuestions = totalQuestions,
            topic = InterviewTopic.PLAYER_PERFORMANCE,
            reporter = reporter,
            contextSubtitle = "Canlı AI Kulis Sorusu • $ratingStr Reyting",
            questionText = qText,
            options = listOf(
                InterviewAnswerOption(
                    id = "Q_AI_H",
                    tone = InterviewTone.HUMBLE,
                    shortTitle = "Takım ve Camia Odaklı Yanıtla",
                    spokenText = humbleReply,
                    reporterReactionText = "Son derece olgun ve camiayı birleştiren bir açıklama.",
                    headlinePreview = "${career.player.fullName}: \"${humbleReply.take(50)}...\"",
                    moraleDelta = 4,
                    fanSupportDelta = 6,
                    managerTrustDelta = 5,
                    teamChemistryDelta = 4,
                    followersDelta = 450
                ),
                InterviewAnswerOption(
                    id = "Q_AI_C",
                    tone = InterviewTone.CONFIDENT,
                    shortTitle = "Yıldız Mentalitesiyle İddialı Konuş",
                    spokenText = confidentReply,
                    reporterReactionText = "Mikrofon başında tam bir yıldız öz güveni!",
                    headlinePreview = "${career.player.fullName}: \"${confidentReply.take(50)}...\"",
                    moraleDelta = 6,
                    fanSupportDelta = 7,
                    managerTrustDelta = 3,
                    teamChemistryDelta = 2,
                    followersDelta = 720
                ),
                InterviewAnswerOption(
                    id = "Q_AI_P",
                    tone = InterviewTone.PROVOCATIVE,
                    shortTitle = "Keskin ve Manşetlik Çıkış Yap",
                    spokenText = provocativeReply,
                    reporterReactionText = "Bu sözler sosyal medyada ve spor programlarında gündemi sarsacak!",
                    headlinePreview = "Olay Sözler! ${career.player.fullName}: \"${provocativeReply.take(46)}...\"",
                    moraleDelta = 4,
                    fanSupportDelta = 2,
                    managerTrustDelta = -3,
                    teamChemistryDelta = -1,
                    followersDelta = 1180
                )
            )
        )
    }

    /**
     * Builds a rich 4-question sequential interview session tailored to:
     * 1) Match result, score margin, home/away & league standings
     * 2) Player's individual rating, goals, assists, shots, or bench/starter status
     * 3) Coach relationship, tactical system, or controversial match events (cards, late goals)
     * 4) Transfer rumors, active offers, rivalry/derby context, or contract/season goals
     */
    fun buildOfflineQuestions(
        career: CareerSaveState,
        report: CompletedMatchReport
    ): List<InterviewQuestion> {
        val seed = (report.fixtureId.hashCode().toLong() xor (career.currentDayIndex * 131L))
        val rng = Random(seed)

        val userClub = career.currentClub
        val isHome = report.homeClubId == userClub.id
        val userScore = if (isHome) report.homeScore else report.awayScore
        val oppScore = if (isHome) report.awayScore else report.homeScore
        val opponentName = if (isHome) report.awayClubName else report.homeClubName
        val opponentClubId = if (isHome) report.awayClubId else report.homeClubId
        val opponentClub = career.clubs.find { it.id == opponentClubId }
        val isCityDerby = opponentClub != null && opponentClub.city.equals(userClub.city, ignoreCase = true)

        val scoreDiff = userScore - oppScore
        val userWon = scoreDiff > 0
        val isDraw = scoreDiff == 0
        val ratingStr = FLFormatters.formatRating(report.userRating)

        val userStandingIndex = career.standings.indexOfFirst { it.clubId == userClub.id }
        val userRank = if (userStandingIndex >= 0) userStandingIndex + 1 else 5
        val oppStandingIndex = career.standings.indexOfFirst { it.clubId == opponentClubId }
        val oppRank = if (oppStandingIndex >= 0) oppStandingIndex + 1 else 8

        val includeFifthSpotlightQ = isCityDerby || report.userGoals >= 1 || report.userRating >= 7.6 || career.transferOffers.isNotEmpty()
        val totalQ = if (includeFifthSpotlightQ) 5 else 4

        val q1 = buildResultOrAtmosphereQuestion(
            questionIndex = 1,
            totalQuestions = totalQ,
            career = career,
            report = report,
            isHome = isHome,
            userScore = userScore,
            oppScore = oppScore,
            opponentName = opponentName,
            isCityDerby = isCityDerby,
            userRank = userRank,
            oppRank = oppRank,
            rng = rng
        )

        val q2 = buildPerformanceQuestion(
            questionIndex = 2,
            totalQuestions = totalQ,
            career = career,
            report = report,
            opponentName = opponentName,
            userWon = userWon,
            isDraw = isDraw,
            ratingStr = ratingStr,
            rng = rng
        )

        val q3 = buildTacticsCoachOrControversyQuestion(
            questionIndex = 3,
            totalQuestions = totalQ,
            career = career,
            report = report,
            opponentName = opponentName,
            userWon = userWon,
            isDraw = isDraw,
            ratingStr = ratingStr,
            rng = rng
        )

        val q4 = buildTransferRivalryOrFutureQuestion(
            questionIndex = 4,
            totalQuestions = totalQ,
            career = career,
            report = report,
            opponentName = opponentName,
            isCityDerby = isCityDerby,
            userWon = userWon,
            userRank = userRank,
            ratingStr = ratingStr,
            rng = rng
        )

        if (!includeFifthSpotlightQ) {
            return listOf(q1, q2, q3, q4)
        }

        val q5 = buildClosingMediaSpotlightQuestion(
            questionIndex = 5,
            totalQuestions = totalQ,
            career = career,
            report = report,
            opponentName = opponentName,
            userWon = userWon,
            ratingStr = ratingStr
        )
        return listOf(q1, q2, q3, q4, q5)
    }

    private fun buildResultOrAtmosphereQuestion(
        questionIndex: Int,
        totalQuestions: Int,
        career: CareerSaveState,
        report: CompletedMatchReport,
        isHome: Boolean,
        userScore: Int,
        oppScore: Int,
        opponentName: String,
        isCityDerby: Boolean,
        userRank: Int,
        oppRank: Int,
        rng: Random
    ): InterviewQuestion {
        val clubName = career.currentClub.name
        val scoreDiff = userScore - oppScore
        val reporter = if (isCityDerby) reportersCatalog[0] else reportersCatalog[rng.nextInt(2)]
        val venueLabel = if (isHome) "${career.currentClub.stadiumName} atmosferinde" else "$opponentName deplasmanında"

        return when {
            isCityDerby && scoreDiff > 0 -> {
                InterviewQuestion(
                    id = "Q1_DERBY_WIN_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.RIVALRY_ATMOSPHERE,
                    reporter = reporter,
                    contextSubtitle = "Şehir Derbisi • ${report.homeClubName} ${report.homeScore}-${report.awayScore} ${report.awayClubName}",
                    questionText = "${career.player.firstName}, nefes kesen ${career.currentClub.city} derbisinde $opponentName karşısında sahadan $userScore-$oppScore galip ayrıldınız! Tribünler hala yıkılıyor, bu derbi zaferi hakkında ilk sözlerin neler?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q1_OPT_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Zaferi Taraftara ve Takıma Armağan Et",
                            spokenText = "Derbiler camia için her zaman çok özeldir. Takım arkadaşlarımla son düdüğe kadar omuz omuza savaştık, bu galibiyet armasını kalbinde taşıyan büyük $clubName taraftarına armağan olsun!",
                            reporterReactionText = "Tribünlerden senin adına büyük bir alkış tufanı kopuyor, takım ruhuna vurgun dikkat çekiciydi.",
                            headlinePreview = "${career.player.fullName}: \"Bu Derbi Zaferi Büyük $clubName Taraftarına Armağan Olsun!\"",
                            moraleDelta = 4,
                            fanSupportDelta = 6,
                            managerTrustDelta = 4,
                            teamChemistryDelta = 5,
                            followersDelta = 420
                        ),
                        InterviewAnswerOption(
                            id = "Q1_OPT_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Şehrin Patronu Olduğunuzu Söyle",
                            spokenText = "Büyük maçlar büyük oyuncuların sahnesidir. Bugün sahaya çıktığımız ilk andan itibaren derbinin hakimi bizdik ve $userScore-$oppScore ile gücümüzü herkese kanıtladık!",
                            reporterReactionText = "Oldukça iddialı sözler! Derbi gecesine damga vuran bir öz güven.",
                            headlinePreview = "${career.player.fullName} Derbi Sonrası Gürledi: \"Sahaya Çıktığımız An Hakimi Bizdik!\"",
                            moraleDelta = 6,
                            fanSupportDelta = 8,
                            managerTrustDelta = 2,
                            teamChemistryDelta = 2,
                            followersDelta = 690
                        ),
                        InterviewAnswerOption(
                            id = "Q1_OPT_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Rakip Camiaya Sert Göndermede Bulun",
                            spokenText = "Maçtan önce çok konuşanlar vardı ama futbol lafla değil çimlerin üzerinde oynanır! Bu şehrin tek gerçek büyüğü $clubName, bunu bu gece bir kez daha öğrendiler!",
                            reporterReactionText = "Vay canına! Bu sözler $opponentName cephesinde ve sosyal medyada uzun süre konuşulacak!",
                            headlinePreview = "Derbi Sonrası Olay Sözler! ${career.player.fullName}: \"Bu Şehrin Tek Büyüğü Biziz!\"",
                            moraleDelta = 5,
                            fanSupportDelta = 9,
                            managerTrustDelta = -2,
                            teamChemistryDelta = 1,
                            followersDelta = 1150
                        )
                    )
                )
            }

            scoreDiff >= 3 -> {
                InterviewQuestion(
                    id = "Q1_BIG_WIN_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.MATCH_RESULT,
                    reporter = reporter,
                    contextSubtitle = "Farklı Galibiyet • Hafta ${report.week} • Lig #$userRank",
                    questionText = "$venueLabel $opponentName karşısında $userScore-$oppScore gibi ezici bir skorla sahadan ayrıldınız. Maç boyunca rakibe hiç nefes aldırmayan bu gövde gösterisini neye bağlıyorsun?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q1_BW_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Hafta İçi Çalışmayı ve Disiplini Öv",
                            spokenText = "Skor tabelası harika görünse de arkasında hafta boyunca döktüğümüz alın teri var. Hocamızın planına sadık kaldık ve takım olarak kusursuz bir yardımlaşma sergiledik.",
                            reporterReactionText = "Farklı galibiyete rağmen ayakları yere basan, çok olgun bir değerlendirme.",
                            headlinePreview = "${career.player.fullName}: \"$userScore-$oppScore'lık Zafer Disiplinli Çalışmamızın Eseri\"",
                            moraleDelta = 4,
                            fanSupportDelta = 5,
                            managerTrustDelta = 5,
                            teamChemistryDelta = 4,
                            followersDelta = 380
                        ),
                        InterviewAnswerOption(
                            id = "Q1_BW_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Şampiyonluk Kalibresinde Olduğunuzu Vurgula",
                            spokenText = "Bizim potansiyelimiz tam olarak bu! Ritim bulduğumuzda bu ligde karşımızda durabilecek bir savunma hattı tanımıyorum. $userScore-$oppScore sadece bir başlangıç!",
                            reporterReactionText = "Rakiplere açık bir gözdağı verdin, bu hücum hattı gerçekten korkutucu.",
                            headlinePreview = "${career.player.fullName} Meydan Okudu: \"Ritim Bulduğumuzda Kimse Karşımızda Duramaz!\"",
                            moraleDelta = 6,
                            fanSupportDelta = 7,
                            managerTrustDelta = 3,
                            teamChemistryDelta = 2,
                            followersDelta = 620
                        ),
                        InterviewAnswerOption(
                            id = "Q1_BW_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Rakibin Çaresiz Kaldığını Söyle",
                            spokenText = "Açıkçası $opponentName'in bizi biraz daha zorlamasını bekliyordum ama antrenman maçı temposunda $userScore gol attık. Biraz daha vites artırsak tarihi fark olurdu!",
                            reporterReactionText = "Rakip kulübeyi kızdıracak kadar sert bir açıklama!",
                            headlinePreview = "Flaş Açıklama! ${career.player.fullName}: \"Antrenman Temposunda $userScore Gol Attık!\"",
                            moraleDelta = 4,
                            fanSupportDelta = 3,
                            managerTrustDelta = -3,
                            teamChemistryDelta = -1,
                            followersDelta = 960
                        )
                    )
                )
            }

            scoreDiff in 1..2 -> {
                InterviewQuestion(
                    id = "Q1_WIN_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.MATCH_RESULT,
                    reporter = reporter,
                    contextSubtitle = "Kritik 3 Puan • ${report.homeClubName} ${report.homeScore}-${report.awayScore} ${report.awayClubName}",
                    questionText = "Zorlu geçen 90 dakikanın sonunda $venueLabel $opponentName engelini $userScore-$oppScore aşarak altın değerinde 3 puanı hanenize yazdırdınız. Sahadaki mücadeleyi nasıl özetlersin?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q1_W_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Takım Karakterine ve Rakibe Saygı Göster",
                            spokenText = "$opponentName gerçekten dirençli bir takım ama biz de pes etmeyen bir karakter ortaya koyduk. Kritik anlarda kenetlenip 3 puanı taraftarımıza hediye ettiğimiz için mutluyuz.",
                            reporterReactionText = "Hem rakibe saygılı hem de takım arkadaşlarını onore eden bir açıklama.",
                            headlinePreview = "${career.player.fullName}: \"Kenetlenerek Altın Değerinde 3 Puan Aldık\"",
                            moraleDelta = 3,
                            fanSupportDelta = 5,
                            managerTrustDelta = 4,
                            teamChemistryDelta = 4,
                            followersDelta = 340
                        ),
                        InterviewAnswerOption(
                            id = "Q1_W_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Kazanma Alışkanlığına Dikkat Çek",
                            spokenText = "Büyük hedeflere yürüyen takımlar zor maçları da kazanmayı bilir. Bugün sahada ne istediğini bilen, soğukkanlı ve kazanmaya şartlanmış bir $clubName vardı!",
                            reporterReactionText = "Kazanma kültürüne yaptığın vurgu teknik heyetin de yüzünü güldürecektir.",
                            headlinePreview = "${career.player.fullName}: \"Zor Maçları Kazanmak Bizim Karakterimiz!\"",
                            moraleDelta = 5,
                            fanSupportDelta = 6,
                            managerTrustDelta = 3,
                            teamChemistryDelta = 2,
                            followersDelta = 490
                        ),
                        InterviewAnswerOption(
                            id = "Q1_W_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Eleştirenlere Kapak Olsun De",
                            spokenText = "Hafta boyunca puan kaybedeceğimizi bekleyen, televizyonlarda bizi eleştiren sözde uzmanlara sahada en güzel cevabı $userScore-$oppScore ile verdik. Konuşmaya devam etsinler!",
                            reporterReactionText = "Stüdyodaki yorumculara doğrudan sert bir mesaj gönderdin!",
                            headlinePreview = "${career.player.fullName}'dan Yorumculara Sert Yanıt: \"Cevabımızı Sahada Verdik!\"",
                            moraleDelta = 4,
                            fanSupportDelta = 5,
                            managerTrustDelta = -2,
                            teamChemistryDelta = 1,
                            followersDelta = 840
                        )
                    )
                )
            }

            scoreDiff == 0 -> {
                InterviewQuestion(
                    id = "Q1_DRAW_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.MATCH_RESULT,
                    reporter = reporter,
                    contextSubtitle = "Puanlar Paylaşıldı • ${report.homeClubName} ${report.homeScore}-${report.awayScore} ${report.awayClubName}",
                    questionText = "$venueLabel büyük çekişmeye sahne olan mücadele $userScore-$oppScore eşitlikle sona erdi. Soyunma odasında bu 1 puan kazanılmış bir puan olarak mı görülüyor, yoksa kaçan 2 puanın üzüntüsü mü var?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q1_D_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Eksikleri Görerek Önünüze Bakacağınızı Söyle",
                            spokenText = "Elbette sahaya her zaman 3 puan için çıkıyoruz. Bugün bazı anlarda şans yanımızda değildi ama mücadeleyi bırakmadık. Hatalarımızdan ders çıkarıp haftaya telafi edeceğiz.",
                            reporterReactionText = "Yapıcı ve özeleştiri içeren profesyonel bir yaklaşım.",
                            headlinePreview = "${career.player.fullName}: \"Ders Çıkarıp Önümüzdeki Maçta Telafi Edeceğiz\"",
                            moraleDelta = 2,
                            fanSupportDelta = 3,
                            managerTrustDelta = 4,
                            teamChemistryDelta = 3,
                            followersDelta = 240
                        ),
                        InterviewAnswerOption(
                            id = "Q1_D_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Galibiyeti Hak Ettiğinizi Vurgula",
                            spokenText = "Kesinlikle kaçan 2 puan! Sahadaki oyun üstünlüğüne ve ürettiğimiz pozisyonlara bakarsanız bugün buradan galibiyetle ayrılması gereken taraf bizdik. Gelecek hafta acısını çıkaracağız!",
                            reporterReactionText = "Beraberlikle yetinmeyen hırslı bir duruş!",
                            headlinePreview = "${career.player.fullName} Net Konuştu: \"Bugün Galibiyeti Hak Eden Taraf Bizdik!\"",
                            moraleDelta = 4,
                            fanSupportDelta = 5,
                            managerTrustDelta = 2,
                            teamChemistryDelta = 1,
                            followersDelta = 410
                        ),
                        InterviewAnswerOption(
                            id = "Q1_D_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Rakibin Katı Savunmasını ve Zaman Geçirmesini Eleştir",
                            spokenText = "Bugün sahada futbol oynamak isteyen tek bir takım vardı, o da bizdik! $opponentName 90 dakika boyunca vakit geçirip oyunu çirkinleştirerek 1 puanı kurtardı.",
                            reporterReactionText = "Rakibin oyun anlayışına yönelik çok sert sözler!",
                            headlinePreview = "Polemik Yaratan Sözler! ${career.player.fullName}: \"$opponentName Futbol Oynamaya Gelmemiş!\"",
                            moraleDelta = 1,
                            fanSupportDelta = 2,
                            managerTrustDelta = -3,
                            teamChemistryDelta = -1,
                            followersDelta = 770
                        )
                    )
                )
            }

            else -> {
                val isHeavyLoss = scoreDiff <= -3
                InterviewQuestion(
                    id = "Q1_LOSS_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.MATCH_RESULT,
                    reporter = reporter,
                    contextSubtitle = "${if (isHeavyLoss) "Ağır Yenilgi" else "Mağlubiyet"} • ${report.homeClubName} ${report.homeScore}-${report.awayScore} ${report.awayClubName}",
                    questionText = "$venueLabel işler istediğiniz gibi gitmedi ve sahadan $userScore-$oppScore mağlup ayrıldınız. Taraftarlar sonuçtan dolayı oldukça üzgün; bu yenilginin sebebi sence neydi?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q1_L_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Sorumluluğu Al ve Taraftardan Özür Dile",
                            spokenText = "Mazeret üretecek değiliz, bugün $clubName formasına yakışan oyunu sahaya yansıtamadık. Bizi destekleyen taraftarımızdan özür diliyoruz; yarından itibaren iki katı çalışıp ayağa kalkacağız.",
                            reporterReactionText = "Zor gecede sorumluluktan kaçmayan olgun bir açıklama.",
                            headlinePreview = "${career.player.fullName}'dan Özeleştiri: \"Mazeret Yok, Taraftarımıza Söz Veriyoruz!\"",
                            moraleDelta = 1,
                            fanSupportDelta = 4,
                            managerTrustDelta = 5,
                            teamChemistryDelta = 4,
                            followersDelta = 210
                        ),
                        InterviewAnswerOption(
                            id = "Q1_L_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Bu Yenilginin Sizi Kamçılayacağını Söyle",
                            spokenText = "Futbolda bazen böyle şanssız geceler yaşanır ama kimse bizi şimdiden silmeye kalkmasın! Bu mağlubiyet içimizdeki ateşi daha da büyüttü, haftaya bambaşka bir reaksiyon göreceksiniz.",
                            reporterReactionText = "Yenilgiye rağmen başı dik, meydan okuyan bir tavır.",
                            headlinePreview = "${career.player.fullName}: \"Kimse Bizi Silmesin, Çok Güçlü Döneceğiz!\"",
                            moraleDelta = 4,
                            fanSupportDelta = 2,
                            managerTrustDelta = 2,
                            teamChemistryDelta = 1,
                            followersDelta = 380
                        ),
                        InterviewAnswerOption(
                            id = "Q1_L_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Takım İçi Hatalara ve Mental Düşüşe Tepki Göster",
                            spokenText = "Açık konuşmak gerekirse sahada herkes aynı ciddiyetle mücadele etmezse böyle sonuçlar kaçınılmaz olur! Bazı şeylerin soyunma odasında acilen değişmesi gerekiyor.",
                            reporterReactionText = "Bu sözler soyunma odasında ve teknik heyette soğuk duş etkisi yaratabilir!",
                            headlinePreview = "Soyunma Odasını Karıştıracak Sözler! ${career.player.fullName}: \"Herkes Aynı Ciddiyetle Oynamıyor!\"",
                            moraleDelta = -2,
                            fanSupportDelta = -3,
                            managerTrustDelta = -6,
                            teamChemistryDelta = -7,
                            followersDelta = 890
                        )
                    )
                )
            }
        }
    }

    private fun buildPerformanceQuestion(
        questionIndex: Int,
        totalQuestions: Int,
        career: CareerSaveState,
        report: CompletedMatchReport,
        opponentName: String,
        userWon: Boolean,
        isDraw: Boolean,
        ratingStr: String,
        rng: Random
    ): InterviewQuestion {
        val player = career.player
        val reporter = reportersCatalog[1] // TRT SPOR / S SPORT+
        val passAccuracyPct = if (report.userPassesAttempted > 0) {
            ((report.userPassesCompleted * 100.0) / report.userPassesAttempted).toInt()
        } else 82

        return when {
            report.userGoals >= 2 -> {
                InterviewQuestion(
                    id = "Q2_BRACE_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.PLAYER_PERFORMANCE,
                    reporter = reporter,
                    contextSubtitle = "Yıldızlaşan Performans • ${report.userGoals} Gol • $ratingStr Reyting",
                    questionText = "Bu gece rakip ağları tam ${report.userGoals} kez sarsarak $ratingStr maç reytingiyle gecenin tartışmasız yıldızı oldun! Son vuruşlardaki bu yüksek soğukkanlılığını nasıl anlatırsın?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q2_BR_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Asist Yapan Arkadaşlarına Teşekkür Et",
                            spokenText = "Golleri ben atmış olabilirim ama asıl pay bana o harika pasları çıkaran takım arkadaşlarımın. Onlar doğru yerde buluşturdu, bana sadece dokunmak kaldı.",
                            reporterReactionText = "${report.userGoals} gole rağmen bu mütevazılık soyunma odasında çok sevilecektir.",
                            headlinePreview = "${player.fullName} ${report.userGoals} Golünü Arkadaşlarına Adadı: \"Asıl Pay Takımımın!\"",
                            moraleDelta = 5,
                            fanSupportDelta = 6,
                            managerTrustDelta = 5,
                            teamChemistryDelta = 6,
                            followersDelta = 520
                        ),
                        InterviewAnswerOption(
                            id = "Q2_BR_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Gol Krallığı ve Yıldızlık Hedefini İşaret Et",
                            spokenText = "Ceza sahasında topla buluştuğumda kalecinin ne yapacağını önceden hissediyorum. Bu sezon ${player.currentSeasonStats.goals} gole ulaştım ve durmaya hiç niyetim yok, hedefim zirve!",
                            reporterReactionText = "Tam bir keskin nişancı öz güveni! İstatistiklerin de bunu doğruluyor.",
                            headlinePreview = "${player.fullName} Durdurulamıyor: \"Hedefim Sezon Sonunda Zirvede Olmak!\"",
                            moraleDelta = 7,
                            fanSupportDelta = 8,
                            managerTrustDelta = 3,
                            teamChemistryDelta = 2,
                            followersDelta = 860
                        ),
                        InterviewAnswerOption(
                            id = "Q2_BR_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Rakip Savunmanın Seni Tutamadığını Söyle",
                            spokenText = "Maçtan önce beni özel önlemlerle durduracaklarını söylüyorlardı. Sanırım ${report.userGoals} gol yeterli bir cevap olmuştur, beni durdurmak için daha fazlasına ihtiyaçları var!",
                            reporterReactionText = "Rakip stoperlere canlı yayında meydan okudun!",
                            headlinePreview = "${player.fullName}'dan Rakip Savunmaya Gönderme: \"Beni Durdurmaya Güçleri Yetmedi!\"",
                            moraleDelta = 6,
                            fanSupportDelta = 7,
                            managerTrustDelta = 0,
                            teamChemistryDelta = -1,
                            followersDelta = 1240
                        )
                    )
                )
            }

            report.userGoals == 1 || report.userAssists >= 1 -> {
                val contributionText = when {
                    report.userGoals == 1 && report.userAssists >= 1 -> "1 gol ve ${report.userAssists} asistlik skor katkınla"
                    report.userGoals == 1 -> "attığın şık gol ve $ratingStr reytinginle"
                    else -> "yaptığın ${report.userAssists} asist ve %$passAccuracyPct pas isabetinle"
                }
                InterviewQuestion(
                    id = "Q2_CONTRIB_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.PLAYER_PERFORMANCE,
                    reporter = reporter,
                    contextSubtitle = "Skor Katkısı • ${report.userGoals} Gol, ${report.userAssists} Asist • $ratingStr Reyting",
                    questionText = "Sahada kaldığın ${report.userPlayedMinutes} dakika boyunca $contributionText hücum hattını sırtladın. Kritik pozisyonda karar verirken aklından ne geçti?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q2_C_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "İdmanlardaki Tekrarların Meyvesi Olduğunu Söyle",
                            spokenText = "Antrenmanlarda teknik ekibimizle bu hücum organizasyonlarını defalarca çalışıyoruz. Maç içinde o boşluğu görünce doğru kararı verdim, takıma katkı sağladığım için mutluyum.",
                            reporterReactionText = "Disiplinli hazırlığın sahaya yansıması gerçekten etkileyiciydi.",
                            headlinePreview = "${player.fullName}: \"İdmanda Çalıştığımız Organizasyonu Sahaya Yansıttık\"",
                            moraleDelta = 4,
                            fanSupportDelta = 5,
                            managerTrustDelta = 5,
                            teamChemistryDelta = 4,
                            followersDelta = 410
                        ),
                        InterviewAnswerOption(
                            id = "Q2_C_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Sorumluluk Almaktan Çekinmediğini Vurgula",
                            spokenText = "Taraftarımız benden kilit açan hareketler bekliyor. Top ayağıma geldiğinde tereddüt etmem; sorumluluk alıp skoru değiştirmek benim işim!",
                            reporterReactionText = "Lider karakterini ortaya koyan net ifadeler!",
                            headlinePreview = "${player.fullName}: \"Sorumluluk Alıp Skoru Değiştirmek Benim İşim!\"",
                            moraleDelta = 6,
                            fanSupportDelta = 7,
                            managerTrustDelta = 3,
                            teamChemistryDelta = 2,
                            followersDelta = 630
                        ),
                        InterviewAnswerOption(
                            id = "Q2_C_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Formunu Sorgulayanlara Mesaj Gönder",
                            spokenText = "Son günlerde formumu tartışmaya açanlar bugün tabelaya iyi baksın! Ben sahada cevabımı gol ve asistle veririm, gerisi boş gürültü.",
                            reporterReactionText = "Eleştirmenlere canlı yayında çok sert bir cevap verdin!",
                            headlinePreview = "${player.fullName} Eleştirilere Patladı: \"Tabelaya İyi Baksınlar!\"",
                            moraleDelta = 5,
                            fanSupportDelta = 4,
                            managerTrustDelta = -1,
                            teamChemistryDelta = 0,
                            followersDelta = 910
                        )
                    )
                )
            }

            !report.userStarted -> {
                InterviewQuestion(
                    id = "Q2_BENCH_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.PLAYER_PERFORMANCE,
                    reporter = reportersCatalog[4],
                    contextSubtitle = "Hamle Oyuncusu • ${report.userPlayedMinutes}' Dakika • $ratingStr Reyting",
                    questionText = "Bugünkü karşılaşmaya yedek kulübesinde başladın ve sonradan oyuna dahil olarak ${report.userPlayedMinutes} dakika süre aldın ($ratingStr reyting). İlk 11'de olmamak seni mental olarak nasıl etkiliyor?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q2_B_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Hocanın Kararına Saygı Duyduğunu Belirt",
                            spokenText = "1 dakika da oynasam 90 dakika da oynasam bu formanın hakkını vermek zorundayım. Hocamızın kararına saygım sonsuz, formayı kapmak için idmanlarda daha çok çalışacağım.",
                            reporterReactionText = "Profesyonellik dersi veren bir açıklama, hocanın bunu takdir edeceği kesin.",
                            headlinePreview = "${player.fullName}: \"Kaç Dakika Süre Alırsam Alayım Formam İçin Savaşırım\"",
                            moraleDelta = 2,
                            fanSupportDelta = 4,
                            managerTrustDelta = 6,
                            teamChemistryDelta = 4,
                            followersDelta = 260
                        ),
                        InterviewAnswerOption(
                            id = "Q2_B_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "İlk 11 İçin Hazır Olduğunu Göster",
                            spokenText = "Oyuna girdiğim andan itibaren tempoyu nasıl değiştirdiğimi herkes gördü. Ben kulübede oturacak değil, ilk 11'de maç kazandıracak kalitedeyim ve haftaya formayı alacağım!",
                            reporterReactionText = "İlk 11 için hocaya ve camiaya net bir mesaj gönderdin.",
                            headlinePreview = "${player.fullName}'dan İlk 11 Mesajı: \"Sahaya Girince Tempoyu Değiştirdim!\"",
                            moraleDelta = 5,
                            fanSupportDelta = 5,
                            managerTrustDelta = 1,
                            teamChemistryDelta = 1,
                            followersDelta = 480
                        ),
                        InterviewAnswerOption(
                            id = "Q2_B_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Yedek Kalmayı Kabul Etmediğini Söyle",
                            spokenText = "Açık söyleyeyim, yedek kulübesinde oturmak bana göre değil! Bu takımda benden daha formda kimse yokken kulübede beklemek sabrımı zorluyor.",
                            reporterReactionText = "Bomba bir çıkış! Teknik direktör ${career.currentClub.managerName} bu sözlere çok kızabilir!",
                            headlinePreview = "Kulübede Kriz! ${player.fullName}: \"Yedek Oturmak Sabrımı Zorluyor!\"",
                            moraleDelta = 2,
                            fanSupportDelta = -2,
                            managerTrustDelta = -8,
                            teamChemistryDelta = -4,
                            followersDelta = 950
                        )
                    )
                )
            }

            report.userRating >= 7.2 -> {
                InterviewQuestion(
                    id = "Q2_SOLID_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.PLAYER_PERFORMANCE,
                    reporter = reportersCatalog[4],
                    contextSubtitle = "İstikrarlı Oyun • %$passAccuracyPct Pas İsabeti • $ratingStr Reyting",
                    questionText = "Skor tabelasında ismin yazmasa bile %$passAccuracyPct pas isabeti ve $ratingStr reytingle takımın sahadaki en çalışkan isimlerinden biriydin. Kendi performansını nasıl değerlendiriyorsun?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q2_S_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Görev Adamı Olmaktan Gurur Duyduğunu Söyle",
                            spokenText = "Futbol sadece gol atmak değildir; pres yapmak, alan kapatmak ve oyunu doğru yönlendirmek de çok önemli. Takımın sistemini işletmek için elimden geleni yaptım.",
                            reporterReactionText = "Modern futbolun gerektirdiği takım oyununu çok güzel özetledin.",
                            headlinePreview = "${player.fullName}: \"Takım Sisteminin Kusursuz İşlemesi İçin Sahadayım\"",
                            moraleDelta = 3,
                            fanSupportDelta = 4,
                            managerTrustDelta = 5,
                            teamChemistryDelta = 5,
                            followersDelta = 310
                        ),
                        InterviewAnswerOption(
                            id = "Q2_S_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Oyunun Merkezinde Olduğunu Vurgula",
                            spokenText = "Bugün sahada oyunun temposunu ben belirledim. $ratingStr reyting doğru yolda olduğumu gösteriyor, önümüzdeki maçlarda buna gol ve asistleri de ekleyeceğim!",
                            reporterReactionText = "Hem oyun kurulumundan memnun hem de skor katkısı için iddialı!",
                            headlinePreview = "${player.fullName}: \"Sahada Temponun Hakimiydim, Sıra Gollerde!\"",
                            moraleDelta = 5,
                            fanSupportDelta = 5,
                            managerTrustDelta = 3,
                            teamChemistryDelta = 2,
                            followersDelta = 460
                        ),
                        InterviewAnswerOption(
                            id = "Q2_S_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "İlerideki Arkadaşlarının Paslarını Değerlendiremediğini Söyle",
                            spokenText = "%$passAccuracyPct isabetle oynayıp bu kadar kilit pas dağıtmama rağmen bitiricilikte sorun yaşadık. Yaptığım servisler gole çevrilseydi şu an bambaşka bir skor konuşuyorduk!",
                            reporterReactionText = "Hücum hattındaki takım arkadaşlarına üstü kapalı bir sitem!",
                            headlinePreview = "${player.fullName}'dan Sitem: \"Verdiğim Paslar Gole Çevrilseydi Fark Olurdu!\"",
                            moraleDelta = 2,
                            fanSupportDelta = 1,
                            managerTrustDelta = -3,
                            teamChemistryDelta = -5,
                            followersDelta = 740
                        )
                    )
                )
            }

            else -> {
                InterviewQuestion(
                    id = "Q2_TOUGH_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.PLAYER_PERFORMANCE,
                    reporter = reportersCatalog[3],
                    contextSubtitle = "Zorlu Gece • ${report.userPlayedMinutes}' Dakika • $ratingStr Reyting",
                    questionText = "Bugün rakip savunmanın sıkı markajı altında istediğimiz topları almakta zorlandın ve maçı $ratingStr reytingle tamamladın. Sosyal medyada performansına yönelik eleştiriler var, ne dersin?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q2_T_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Eleştirileri Haklı Bulup Çalışma Sözü Ver",
                            spokenText = "Taraftarımız her zaman en iyisini beklemekte haklı. Bugün kendi standartlarımın altında kaldığım anlar oldu ama pes etmek benim karakterimde yok, haftaya çok daha güçlü döneceğim.",
                            reporterReactionText = "Samimi özeleştirin taraftarın gönlünü kazanacaktır.",
                            headlinePreview = "${player.fullName}: \"Eleştirilerden Güç Alıp Haftaya Gerçek Kimliğimi Göstereceğim\"",
                            moraleDelta = 2,
                            fanSupportDelta = 4,
                            managerTrustDelta = 4,
                            teamChemistryDelta = 3,
                            followersDelta = 230
                        ),
                        InterviewAnswerOption(
                            id = "Q2_T_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Rakibin İki Kişilik Markajına Dikkat Çek",
                            spokenText = "$opponentName maç boyunca üzerime iki oyuncu gönderdi, bu da takım arkadaşlarım için boş alanlar yarattı. Kimse tek maçla kalitemi sorgulamasın, sahada cevabımı yakında veririm!",
                            reporterReactionText = "Kendine güveninden zerre ödün vermeyen bir açıklama.",
                            headlinePreview = "${player.fullName}: \"Tek Maçla Kalitemi Sorgulayanlara Sahada Cevap Vereceğim!\"",
                            moraleDelta = 4,
                            fanSupportDelta = 2,
                            managerTrustDelta = 2,
                            teamChemistryDelta = 1,
                            followersDelta = 410
                        ),
                        InterviewAnswerOption(
                            id = "Q2_T_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Klavye Başından Eleştirenlere Sert Çık",
                            spokenText = "Hayatında ayağına top değmemiş insanların klavye başından bana futbol öğretmeye çalışması sadece güldürüyor! Sahada ter döken benim, kimseye hesap vermem!",
                            reporterReactionText = "Sosyal medyayı ateşe verecek çok sert bir çıkış!",
                            headlinePreview = "Canlı Yayında Gerilim! ${player.fullName}: \"Klavye Başındakilere Hesap Vermem!\"",
                            moraleDelta = 1,
                            fanSupportDelta = -6,
                            managerTrustDelta = -5,
                            teamChemistryDelta = -2,
                            followersDelta = 1080
                        )
                    )
                )
            }
        }
    }

    private fun buildTacticsCoachOrControversyQuestion(
        questionIndex: Int,
        totalQuestions: Int,
        career: CareerSaveState,
        report: CompletedMatchReport,
        opponentName: String,
        userWon: Boolean,
        isDraw: Boolean,
        ratingStr: String,
        rng: Random
    ): InterviewQuestion {
        val managerName = career.currentClub.managerName
        val tacticalStyle = career.currentClub.managerTacticalStyle
        val cardEvent = report.events.find { it.type == "KIRMIZI_KART" || it.type == "SARI_KART" }
        val lateGoalEvent = report.events.find { it.type == "GOL" && it.minute >= 78 }
        val reporter = reportersCatalog[3] // A SPOR / S SPORT+

        return when {
            lateGoalEvent != null -> {
                InterviewQuestion(
                    id = "Q3_LATE_GOAL_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.CONTROVERSY_MOMENT,
                    reporter = reporter,
                    contextSubtitle = "Son Nefeste Heyecan • ${lateGoalEvent.minute}. Dakika Golü",
                    questionText = "Maçın ${lateGoalEvent.minute}. dakikasında gelen kritik gol stadyumda tansiyonu zirveye çıkardı ve hakemin uzatma kararları çok tartışıldı. O son dakikalarda saha içinde neler yaşandı?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q3_LG_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Son Düdüğe Kadar Konsantrasyonu Vurgula",
                            spokenText = "Futbolda hakem son düdüğü çalana kadar maç bitmez. ${lateGoalEvent.minute}. dakikada nabızlar çok yüksekti ama sakin kalıp oyun disiplinine odaklanmaya çalıştık.",
                            reporterReactionText = "Yüksek tansiyona rağmen soğukkanlılığını koruyan bir yanıt.",
                            headlinePreview = "${career.player.fullName}: \"Son Düdüğe Kadar Konsantrasyonumuzu Koruduk\"",
                            moraleDelta = 3,
                            fanSupportDelta = 4,
                            managerTrustDelta = 4,
                            teamChemistryDelta = 3,
                            followersDelta = 350
                        ),
                        InterviewAnswerOption(
                            id = "Q3_LG_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Kondisyon ve Mental Gücünüzü Öne Çıkar",
                            spokenText = "80. dakikadan sonra rakipler yorulurken biz vites artırıyoruz! Fiziksel ve mental olarak ligin en diri takımıyız, son dakikalar bizim uzmanlık alanımız.",
                            reporterReactionText = "Takımın kondisyonuna ve kazanma inancına güçlü bir vurgu!",
                            headlinePreview = "${career.player.fullName}: \"Son Dakikalar Bizim Uzmanlık Alanımız!\"",
                            moraleDelta = 5,
                            fanSupportDelta = 6,
                            managerTrustDelta = 3,
                            teamChemistryDelta = 3,
                            followersDelta = 540
                        ),
                        InterviewAnswerOption(
                            id = "Q3_LG_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Hakem Kararlarını ve VAR'ı Sert Eleştir",
                            spokenText = "Saha içinde inanılmaz bir standart sorunu vardı! Hakemin takdir haklarını sürekli tek taraflı kullanması ve VAR'ın sessizliği emeklerimize gölge düşürüyor, artık yeter!",
                            reporterReactionText = "Hakem ve VAR kararlarına yönelik PFDK sınırlarını zorlayacak çok sert bir açıklama!",
                            headlinePreview = "Hakem İsyanı! ${career.player.fullName}: \"VAR ve Hakem Kararları Emeğimize Gölge Düşürüyor!\"",
                            moraleDelta = 3,
                            fanSupportDelta = 7,
                            managerTrustDelta = -3,
                            teamChemistryDelta = 2,
                            followersDelta = 1120
                        )
                    )
                )
            }

            cardEvent != null -> {
                InterviewQuestion(
                    id = "Q3_CARD_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.CONTROVERSY_MOMENT,
                    reporter = reporter,
                    contextSubtitle = "Saha İçi Gerilim • ${cardEvent.minute}. Dakika Kart Pozisyonu",
                    questionText = "Karşılaşmanın ${cardEvent.minute}. dakikasında yaşanan sert ikili mücadele ve çıkan kart sonrası iki takım oyuncuları arasında gerginlik yaşandı. Saha içindeki sertlik hakkında ne düşünüyorsun?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q3_CD_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Futbolun Doğasında Sertlik Olduğunu Söyle",
                            spokenText = "İki takım da kazanmayı çok isteyince ikili mücadelelerin sertleşmesi doğal. Önemli olan maç bittiğinde el sıkışabilmek ve saha içinde kalabilmek.",
                            reporterReactionText = "Centilmence ve tansiyonu düşüren örnek bir futbolcu duruşu.",
                            headlinePreview = "${career.player.fullName}'dan Fair-Play Mesajı: \"Gerginlik Sahada Kalır\"",
                            moraleDelta = 3,
                            fanSupportDelta = 4,
                            managerTrustDelta = 4,
                            teamChemistryDelta = 4,
                            followersDelta = 290
                        ),
                        InterviewAnswerOption(
                            id = "Q3_CD_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Fiziksel Mücadeleden Asla Kaçmadığınızı Belirt",
                            spokenText = "Bizi sertlikle yıldırabileceklerini sanıyorlarsa yanılıyorlar! Sahada kimse geri adım atmadı, formamız için her türlü fiziksel savaşa hazırız.",
                            reporterReactionText = "Taraftarın tam da duymak istediği savaşçı ruh!",
                            headlinePreview = "${career.player.fullName}: \"Bizi Sertlikle Yıldırabileceklerini Sananlar Yanılır!\"",
                            moraleDelta = 5,
                            fanSupportDelta = 7,
                            managerTrustDelta = 3,
                            teamChemistryDelta = 3,
                            followersDelta = 580
                        ),
                        InterviewAnswerOption(
                            id = "Q3_CD_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Rakibin Kasıtlı Sert Oynadığını ve Hakemin Göz Yumduğunu Söyle",
                            spokenText = "$opponentName oyuncuları bugün topa değil doğrudan bileklerimize müdahale etti! Hakem de bu kasıtlı sertliğe göz yumdu, sağlığımızı tehlikeye atıyorlar!",
                            reporterReactionText = "Çok ağır bir suçlama! Bu açıklama spor programlarında saatlerce tartışılacak.",
                            headlinePreview = "Bomba İddia! ${career.player.fullName}: \"Topa Değil Bileklerimize Oynadılar!\"",
                            moraleDelta = 2,
                            fanSupportDelta = 5,
                            managerTrustDelta = -2,
                            teamChemistryDelta = 1,
                            followersDelta = 990
                        )
                    )
                )
            }

            else -> {
                val trustContext = if (career.managerTrust >= 65) {
                    "Hocan $managerName seni takımın kilit isimlerinden biri olarak görüyor"
                } else {
                    "Teknik direktör $managerName ile arandaki iletişim ve sistemdeki rolün sıkça konuşuluyor"
                }
                InterviewQuestion(
                    id = "Q3_COACH_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.COACH_TACTICS,
                    reporter = reportersCatalog[4],
                    contextSubtitle = "Taktik Plan • TD: $managerName ($tacticalStyle)",
                    questionText = "$trustContext. Bugün sahada uyguladığınız '$tacticalStyle' anlayışı ve $managerName'in saha kenarındaki direktifleri oyununuzu nasıl etkiledi?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q3_CH_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Hocanın Taktik Dehasına ve Güvene Teşekkür Et",
                            spokenText = "$managerName hocamız hafta boyunca rakibin her zayıf noktasını bize ezberletti. Onun '$tacticalStyle' felsefesiyle her geçen gün daha çok gelişiyorum, güvenini boşa çıkarmayacağım.",
                            reporterReactionText = "Teknik direktör $managerName ile arandaki güçlü bağı pekiştiren sözler.",
                            headlinePreview = "${career.player.fullName}'dan $managerName'e Övgü: \"Hocamızın Planı Kusursuzdu\"",
                            moraleDelta = 3,
                            fanSupportDelta = 4,
                            managerTrustDelta = 7,
                            teamChemistryDelta = 4,
                            followersDelta = 320
                        ),
                        InterviewAnswerOption(
                            id = "Q3_CH_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Taktik Sistemde Serbestlik İstediğini Vurgula",
                            spokenText = "Hocamızın taktik disiplinine saygım büyük ama saha içinde yaratıcılığımı kullandığımda çok daha tehlikeli oluyorum. Bana üçüncü bölgede özgürlük verildiğinde neler yapabildiğimi herkes görüyor.",
                            reporterReactionText = "Yeteneklerine güvenen ve saha içinde liderlik talep eden bir yaklaşım.",
                            headlinePreview = "${career.player.fullName}: \"Sahada Özgürlük Verildiğinde Fark Yaratıyorum!\"",
                            moraleDelta = 5,
                            fanSupportDelta = 5,
                            managerTrustDelta = 1,
                            teamChemistryDelta = 1,
                            followersDelta = 510
                        ),
                        InterviewAnswerOption(
                            id = "Q3_CH_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Taktik Tercihlerin Seni Kısıtladığını İma Et",
                            spokenText = "Dürüst olmak gerekirse bugünkü taktik kurgu hücum gücümüzü fazla geriye çekti. Benim gibi bir oyuncudan tam verim almak istiyorsanız topu rakip ceza sahasına daha hızlı taşımamız şart!",
                            reporterReactionText = "Teknik direktör $managerName'in taktik planına canlı yayında açık bir eleştiri!",
                            headlinePreview = "Taktik Krizi Mi? ${career.player.fullName}: \"Bu Kurgu Hücum Gücümüzü Kısıtlıyor!\"",
                            moraleDelta = 2,
                            fanSupportDelta = -1,
                            managerTrustDelta = -8,
                            teamChemistryDelta = -3,
                            followersDelta = 940
                        )
                    )
                )
            }
        }
    }

    private fun buildTransferRivalryOrFutureQuestion(
        questionIndex: Int,
        totalQuestions: Int,
        career: CareerSaveState,
        report: CompletedMatchReport,
        opponentName: String,
        isCityDerby: Boolean,
        userWon: Boolean,
        userRank: Int,
        ratingStr: String,
        rng: Random
    ): InterviewQuestion {
        val player = career.player
        val clubName = career.currentClub.name
        val activeOffer = career.transferOffers.firstOrNull()
        val marketValStr = FLFormatters.formatMarketValue(player.marketValueEur)
        val reporter = reportersCatalog[2] // SPORTS DIGITALE - Yağız Sabuncuoğlu

        return when {
            activeOffer != null -> {
                InterviewQuestion(
                    id = "Q4_OFFER_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.TRANSFER_RUMORS,
                    reporter = reporter,
                    contextSubtitle = "Transfer Kulisi • ${activeOffer.clubName} (${activeOffer.leagueName}) İlgisi",
                    questionText = "Son soru: Kulislerde ${activeOffer.leagueName} ekiplerinden ${activeOffer.clubName}'in senin için resmi temaslara başladığı ve menajerin ${career.contract.agentName} ile görüştüğü konuşuluyor. Piyasa değerin $marketValStr seviyesine ulaştı; taraftarlar ayrılık ihtimalinden endişeli, ne söyleyeceksin?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q4_OF_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Tek Odağının Kulübün Olduğunu Söyle",
                            spokenText = "Transfer haberlerini ben de basından okuyorum, bu konularla menajerim ilgileniyor. Benim şu an tek odağım $clubName forması ve taraftarımıza verdiğimiz sözler!",
                            reporterReactionText = "Taraftarın yüreğine su serpen, kulübe bağlılık mesajı!",
                            headlinePreview = "${player.fullName} Transfer İddialarına Noktayı Koydu: \"Tek Odağım $clubName!\"",
                            moraleDelta = 3,
                            fanSupportDelta = 8,
                            managerTrustDelta = 6,
                            teamChemistryDelta = 5,
                            followersDelta = 490
                        ),
                        InterviewAnswerOption(
                            id = "Q4_OF_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Büyük Kulüplerin İlgisinin Doğal Olduğunu Vurgula",
                            spokenText = "Sahada bu performansı sergilediğinizde ${activeOffer.clubName} gibi kulüplerin radarına girmeniz çok doğal. Hedefleri olan bir oyuncuyum, sezon sonunda herkes için en doğru adımı değerlendiririz.",
                            reporterReactionText = "Hem profesyonelliği korudun hem de transfere kapıyı aralık bıraktın!",
                            headlinePreview = "${player.fullName}'dan ${activeOffer.clubName} Yanıtı: \"Büyük Hedeflerim Var, Sezon Sonu Bakarız\"",
                            moraleDelta = 5,
                            fanSupportDelta = 1,
                            managerTrustDelta = 0,
                            teamChemistryDelta = 0,
                            followersDelta = 780
                        ),
                        InterviewAnswerOption(
                            id = "Q4_OF_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Avrupa ve Üst Seviye İçin Ayrılığa Göz Kırp",
                            spokenText = "${activeOffer.clubName} gibi bir projeye hiçbir futbolcu kayıtsız kalamaz! Kariyerimde bir üst basamağa çıkma vakti geldiyse kulübümün de önümü açmasını beklerim.",
                            reporterReactionText = "Flaş! Bu açıklama transfer piyasasında deprem etkisi yaratacak!",
                            headlinePreview = "Ayrılık Sinyali! ${player.fullName}: \"${activeOffer.clubName} Projesine Kayıtsız Kalınamaz!\"",
                            moraleDelta = 4,
                            fanSupportDelta = -8,
                            managerTrustDelta = -6,
                            teamChemistryDelta = -4,
                            followersDelta = 1420
                        )
                    )
                )
            }

            career.isTransferWindowOpen -> {
                InterviewQuestion(
                    id = "Q4_WINDOW_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.TRANSFER_RUMORS,
                    reporter = reporter,
                    contextSubtitle = "${career.transferWindowNameTr} • Piyasa Değeri: $marketValStr",
                    questionText = "${career.transferWindowNameTr} devam ederken adın birçok kulüple anılıyor ve piyasa değerin $marketValStr'a yükseldi. Bu süreçte geleceğin ve sözleşme hedeflerin hakkında neler söylersin?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q4_W_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Burada Mutlu Olduğunu ve Formaya Odaklandığını Söyle",
                            spokenText = "Şu an $clubName çatısı altında çok mutluyum. Transfer dönemi dedikodularına kulağımı kapattım, tek düşüncem bu takımla kupalar kazanmak.",
                            reporterReactionText = "Camiaya güven veren net bir sadakat açıklaması.",
                            headlinePreview = "${player.fullName}: \"$clubName Çatısı Altında Çok Mutluyum!\"",
                            moraleDelta = 3,
                            fanSupportDelta = 7,
                            managerTrustDelta = 5,
                            teamChemistryDelta = 4,
                            followersDelta = 390
                        ),
                        InterviewAnswerOption(
                            id = "Q4_W_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Değerinin Daha da Artacağını Vurgula",
                            spokenText = "$marketValStr sadece başlangıç! Sahadaki istatistiklerimi yukarı taşıdıkça Avrupa'nın dev sahnelerinde adımı çok daha sık duyacaksınız.",
                            reporterReactionText = "Kariyer vizyonu çok geniş ve öz güvenli bir yanıt!",
                            headlinePreview = "${player.fullName}: \"$marketValStr Sadece Başlangıç, Hedefim Zirve!\"",
                            moraleDelta = 6,
                            fanSupportDelta = 4,
                            managerTrustDelta = 2,
                            teamChemistryDelta = 1,
                            followersDelta = 680
                        ),
                        InterviewAnswerOption(
                            id = "Q4_W_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Yönetime Sözleşme ve Değer Mesajı Gönder",
                            spokenText = "Futbolda dün yoktur, bugün vardır. Sahada takımı sırtlıyorsam hem yönetimin hem de ilgilenen kulüplerin değerimi ona göre bilmesi gerekir, her teklife açığım!",
                            reporterReactionText = "Yönetime ve transfer piyasasına soğuk terler döktürecek bir mesaj!",
                            headlinePreview = "${player.fullName}'dan Yönetime Rest: \"Değerimi Bilmeyen Kaybeder, Tekliflere Açığım!\"",
                            moraleDelta = 3,
                            fanSupportDelta = -5,
                            managerTrustDelta = -5,
                            teamChemistryDelta = -2,
                            followersDelta = 1190
                        )
                    )
                )
            }

            else -> {
                val nextOpponent = career.nextUnplayedUserFixture?.let { fx ->
                    if (fx.homeClubId == career.currentClub.id) fx.awayClubName else fx.homeClubName
                } ?: "gelecek hafta rakibimiz"

                InterviewQuestion(
                    id = "Q4_FUTURE_${report.fixtureId}",
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    topic = InterviewTopic.RIVALRY_ATMOSPHERE,
                    reporter = reportersCatalog[0],
                    contextSubtitle = "Ligde #$userRank. Sıra • Sıradaki Rakip: $nextOpponent",
                    questionText = "Ligde $userRank. sırada bulunuyorsunuz ve sırada zorlu $nextOpponent karşılaşması var. Sezonun geri kalanı ve taraftarların şampiyonluk/zirve beklentisi için buradan nasıl bir mesaj vermek istersin?",
                    options = listOf(
                        InterviewAnswerOption(
                            id = "Q4_F_H",
                            tone = InterviewTone.HUMBLE,
                            shortTitle = "Maç Maç İlerleyip Taraftarla Bütünleşmeyi Vurgula",
                            spokenText = "Lig uzun bir maraton, adım adım ilerliyoruz. $nextOpponent maçına da aynı ciddiyetle hazırlanacağız; taraftarımız tribünleri doldursun, onların desteğiyle aşamayacağımız engel yok!",
                            reporterReactionText = "Taraftarı kenetleyen, birleştirici bir kapanış mesajı.",
                            headlinePreview = "${player.fullName}'dan Taraftara Çağrı: \"Birlikte Aşamayacağımız Engel Yok!\"",
                            moraleDelta = 4,
                            fanSupportDelta = 6,
                            managerTrustDelta = 5,
                            teamChemistryDelta = 4,
                            followersDelta = 430
                        ),
                        InterviewAnswerOption(
                            id = "Q4_F_C",
                            tone = InterviewTone.CONFIDENT,
                            shortTitle = "Zirve Yarışında Favori Olduğunuzu İlan Et",
                            spokenText = "Bizim hedefimiz her zaman en tepedir! $nextOpponent maçıyla birlikte galibiyet serisi başlatıp sezon sonunda o kupayı şehrimize getireceğiz, herkes buna hazır olsun!",
                            reporterReactionText = "Şampiyonluk ateşini yakan iddialı bir final!",
                            headlinePreview = "${player.fullName} İddialı Konuştu: \"Sezon Sonunda O Kupayı Getireceğiz!\"",
                            moraleDelta = 6,
                            fanSupportDelta = 8,
                            managerTrustDelta = 3,
                            teamChemistryDelta = 3,
                            followersDelta = 670
                        ),
                        InterviewAnswerOption(
                            id = "Q4_F_P",
                            tone = InterviewTone.PROVOCATIVE,
                            shortTitle = "Sıradaki Rakibe ve Zirvedekilere Gözdağı Ver",
                            spokenText = "Üst sıralardaki rakiplerimiz arkalarına bakmaya başlasın çünkü nefesimiz enselerinde! Haftaya $nextOpponent karşısında da sahayı dar etmeye geliyoruz!",
                            reporterReactionText = "Haftaya oynanacak maç öncesi tansiyonu şimdiden yükselttin!",
                            headlinePreview = "${player.fullName}'dan $nextOpponent Maçı Öncesi Gözdağı: \"Sahayı Dar Edeceğiz!\"",
                            moraleDelta = 5,
                            fanSupportDelta = 7,
                            managerTrustDelta = 0,
                            teamChemistryDelta = 1,
                            followersDelta = 980
                        )
                    )
                )
            }
        }
    }

    private fun buildClosingMediaSpotlightQuestion(
        questionIndex: Int,
        totalQuestions: Int,
        career: CareerSaveState,
        report: CompletedMatchReport,
        opponentName: String,
        userWon: Boolean,
        ratingStr: String
    ): InterviewQuestion {
        val player = career.player
        val club = career.currentClub
        val reporter = reportersCatalog[0] // beIN SPORTS Lead Reporter
        val totalSeasonGoals = player.currentSeasonStats.goals
        val totalSeasonAssists = player.currentSeasonStats.assists

        return InterviewQuestion(
            id = "Q5_SPOTLIGHT_${report.fixtureId}",
            questionIndex = questionIndex,
            totalQuestions = totalQuestions,
            topic = InterviewTopic.RIVALRY_ATMOSPHERE,
            reporter = reporter,
            contextSubtitle = "Gecenin Son Sorusu • Sezon: $totalSeasonGoals Gol, $totalSeasonAssists Asist",
            questionText = "Mixed Zone'dan ayrılmadan önce son sorumuz: Sosyal medyada şu an binlerce taraftar senin ismini konuşuyor ve bu sezon $totalSeasonGoals gol, $totalSeasonAssists asist katkısına ulaştın. Ekran başındaki futbolseverlere son sözün ne olur?",
            options = listOf(
                InterviewAnswerOption(
                    id = "Q5_SP_H",
                    tone = InterviewTone.HUMBLE,
                    shortTitle = "Ailene, Takımına ve Taraftara Teşekkür Et",
                    spokenText = "Beni her koşulda destekleyen aileme, takım arkadaşlarıma ve yağmur çamur demeden yanımızda olan büyük ${club.name} taraftarına minnettarım. Onları gururlandırmaya devam edeceğim.",
                    reporterReactionText = "Çok samimi ve duygusal bir veda cümlesi, teşekkürler ${player.firstName}!",
                    headlinePreview = "${player.fullName}: \"Taraftarımızı Gururlandırmak İçin Her Şeyimi Vereceğim\"",
                    moraleDelta = 4,
                    fanSupportDelta = 6,
                    managerTrustDelta = 4,
                    teamChemistryDelta = 4,
                    followersDelta = 460
                ),
                InterviewAnswerOption(
                    id = "Q5_SP_C",
                    tone = InterviewTone.CONFIDENT,
                    shortTitle = "En İyi Sezonunu Yaşayacağının Sözünü Ver",
                    spokenText = "Henüz gerçek şovumu görmediniz bile! Her hafta üzerine koyarak ilerleyeceğim ve sezon bittiğinde herkes ${player.fullName} adını konuşacak!",
                    reporterReactionText = "Manşetlik bir kapanış! İyi akşamlar ve tebrikler!",
                    headlinePreview = "${player.fullName}: \"Henüz Gerçek Şovumu Görmediniz Bile!\"",
                    moraleDelta = 6,
                    fanSupportDelta = 7,
                    managerTrustDelta = 2,
                    teamChemistryDelta = 2,
                    followersDelta = 790
                ),
                InterviewAnswerOption(
                    id = "Q5_SP_P",
                    tone = InterviewTone.PROVOCATIVE,
                    shortTitle = "Rakiplere ve Şüphecilere Son Bir Meydan Oku",
                    spokenText = "Benden şüphe duyanlar televizyonlarının sesini açsın ve izlemeye devam etsin! Sezon sonunda kimlerin haklı çıktığını hep birlikte göreceğiz!",
                    reporterReactionText = "Mikrofonları titreten bir meydan okumayla röportajı noktaladık!",
                    headlinePreview = "${player.fullName}'dan Meydan Okuma: \"Şüphe Duyanlar İzlemeye Devam Etsin!\"",
                    moraleDelta = 5,
                    fanSupportDelta = 4,
                    managerTrustDelta = -1,
                    teamChemistryDelta = 0,
                    followersDelta = 1120
                )
            )
        )
    }

    /**
     * Converts a user-typed free-text answer into a complete [InterviewAnswerOption] for the current question.
     */
    fun buildCustomAnswerOption(
        question: InterviewQuestion,
        customText: String,
        selectedTone: InterviewTone,
        playerFullName: String
    ): InterviewAnswerOption {
        val cleanText = customText.trim()
        val (moraleD, fanD, trustD, chemD, followD) = when (selectedTone) {
            InterviewTone.HUMBLE -> listOf(3, 5, 5, 4, 380)
            InterviewTone.CONFIDENT -> listOf(6, 7, 3, 2, 640)
            InterviewTone.PROVOCATIVE -> listOf(3, -2, -5, -2, 1050)
        }
        val reaction = when (selectedTone) {
            InterviewTone.HUMBLE -> "Kendi sözlerinle samimi ve takım odaklı bir mesaj verdin."
            InterviewTone.CONFIDENT -> "Mikrofona kendi cümlenle oldukça öz güvenli ve net bir mesaj bıraktın!"
            InterviewTone.PROVOCATIVE -> "Kendi cümlenle gündemi sarsacak çok keskin bir açıklama yaptın!"
        }
        val previewSnippet = if (cleanText.length > 52) cleanText.take(52) + "..." else cleanText
        return InterviewAnswerOption(
            id = "${question.id}_CUSTOM_${System.currentTimeMillis()}",
            tone = selectedTone,
            shortTitle = "Özel Açıklama (${selectedTone.shortBadgeTr})",
            spokenText = cleanText,
            reporterReactionText = reaction,
            headlinePreview = "$playerFullName: \"$previewSnippet\"",
            moraleDelta = moraleD,
            fanSupportDelta = fanD,
            managerTrustDelta = trustD,
            teamChemistryDelta = chemD,
            followersDelta = followD
        )
    }

    /**
     * Evaluates all answered questions and constructs a complete [InterviewSessionResult].
     */
    fun summarizeSession(
        career: CareerSaveState,
        report: CompletedMatchReport,
        answeredItems: List<AnsweredInterviewItem>,
        usedDynamicAi: Boolean = false
    ): InterviewSessionResult {
        if (answeredItems.isEmpty()) {
            return InterviewSessionResult(
                fixtureId = report.fixtureId,
                answeredItems = emptyList(),
                dominantTone = InterviewTone.HUMBLE,
                mainHeadline = "${career.player.fullName} Maç Sonu Basına Konuştu",
                subHeadline = "${report.homeClubName} ${report.homeScore}-${report.awayScore} ${report.awayClubName}",
                bestQuoteText = "Takım olarak hedefimize odaklandık.",
                totalMoraleDelta = 2,
                totalFanSupportDelta = 3,
                totalManagerTrustDelta = 2,
                totalTeamChemistryDelta = 2,
                totalFollowersDelta = 250,
                usedDynamicAiQuestions = usedDynamicAi
            )
        }

        val totalMorale = answeredItems.sumOf { it.selectedOption.moraleDelta }.coerceIn(-12, 18)
        val totalFan = answeredItems.sumOf { it.selectedOption.fanSupportDelta }.coerceIn(-15, 22)
        val totalTrust = answeredItems.sumOf { it.selectedOption.managerTrustDelta }.coerceIn(-18, 18)
        val totalChem = answeredItems.sumOf { it.selectedOption.teamChemistryDelta }.coerceIn(-14, 16)
        val totalFollowers = answeredItems.sumOf { it.selectedOption.followersDelta }.coerceAtLeast(180)

        val toneCounts = answeredItems.groupingBy { it.selectedOption.tone }.eachCount()
        val dominantTone = toneCounts.maxByOrNull { it.value }?.key ?: answeredItems.first().selectedOption.tone

        // Pick the most impactful quote (provocative or confident or first) for main headline
        val standoutItem = answeredItems.maxByOrNull {
            abs(it.selectedOption.followersDelta) + abs(it.selectedOption.fanSupportDelta) * 40
        } ?: answeredItems.first()

        val secondaryItem = answeredItems.firstOrNull { it != standoutItem } ?: standoutItem

        return InterviewSessionResult(
            fixtureId = report.fixtureId,
            answeredItems = answeredItems,
            dominantTone = dominantTone,
            mainHeadline = standoutItem.selectedOption.headlinePreview,
            subHeadline = secondaryItem.selectedOption.headlinePreview,
            bestQuoteText = standoutItem.effectiveQuote,
            totalMoraleDelta = totalMorale,
            totalFanSupportDelta = totalFan,
            totalManagerTrustDelta = totalTrust,
            totalTeamChemistryDelta = totalChem,
            totalFollowersDelta = totalFollowers,
            usedDynamicAiQuestions = usedDynamicAi
        )
    }

    /**
     * Applies the completed multi-question post-match interview result to [CareerSaveState],
     * updating morale, fan support, manager trust, team chemistry, followers, news headlines,
     * and exposing rich X tweets, Instagram posts + comments, Stories, and NPC WhatsApp reactions for @telefon.
     */
    fun applyInterviewSessionResult(
        state: CareerSaveState,
        result: InterviewSessionResult
    ): Pair<CareerSaveState, String> {
        lastCompletedSession = result
        val baseState = CareerEngine.ensureVersion3EnrichedState(state)
        val dateLabel = CareerEngine.formatGameDate(baseState.currentDayIndex, baseState.currentSeasonYear)
        val shortDate = CareerEngine.formatShortDate(baseState.currentDayIndex)
        val nowMs = System.currentTimeMillis()
        val club = baseState.currentClub
        val player = baseState.player

        val updatedPlayer = player.copy(
            morale = (player.morale + result.totalMoraleDelta).coerceIn(10, 100),
            fanSupport = (player.fanSupport + result.totalFanSupportDelta).coerceIn(5, 100),
            followersCount = (player.followersCount + result.totalFollowersDelta).coerceAtLeast(100)
        )
        val updatedTrust = (baseState.managerTrust + result.totalManagerTrustDelta).coerceIn(5, 100)
        val updatedChemistry = (baseState.teamChemistry + result.totalTeamChemistryDelta).coerceIn(10, 100)

        val sentimentLabel = when {
            result.totalFanSupportDelta >= 4 -> "Destekleyici"
            result.totalFanSupportDelta <= -2 || result.dominantTone == InterviewTone.PROVOCATIVE -> "Eleştirel"
            else -> "Tarafsız"
        }

        // 1. News Events (Main Flash Headline + Tactical/Transfer Sub-headline)
        val primaryQuestion = result.answeredItems.firstOrNull()
        val reporterLabel = primaryQuestion?.question?.reporter?.let { "${it.reporterName} (${it.channelName})" } ?: "Mixed Zone Basını"

        val mainNews = NewsEvent(
            id = "NEWS_INT_MAIN_$nowMs",
            sourceGameEventId = "EVT_INTERVIEW_${state.currentDayIndex}",
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            category = "RÖPORTAJ",
            headline = result.mainHeadline,
            body = "$reporterLabel mikrofonlarına konuşan ${player.fullName}, \"${result.bestQuoteText}\" sözleriyle maç sonu gündemine damga vurdu.",
            relatedClubName = club.name,
            fanReaction = sentimentLabel
        )

        val extraNewsList = if (result.answeredItems.size >= 2 && result.subHeadline != result.mainHeadline) {
            val secItem = result.answeredItems.last()
            listOf(
                mainNews,
                NewsEvent(
                    id = "NEWS_INT_SUB_$nowMs",
                    sourceGameEventId = "EVT_INTERVIEW_SUB_${state.currentDayIndex}",
                    dayIndex = state.currentDayIndex,
                    dateLabel = dateLabel,
                    category = "RÖPORTAJ",
                    headline = result.subHeadline,
                    body = "${secItem.question.reporter.channelName} yayınına konuşan ${player.fullName}: \"${secItem.effectiveQuote}\"",
                    relatedClubName = club.name,
                    fanReaction = sentimentLabel
                )
            )
        } else {
            listOf(mainNews)
        }

        // 2. X (Twitter) Posts for @telefon's X feed
        val broadcasterPost = XPostItem(
            id = "XP_INT_BC_$nowMs",
            dayIndex = state.currentDayIndex,
            dateLabel = shortDate,
            authorName = primaryQuestion?.question?.reporter?.channelName ?: "beIN SPORTS Türkiye",
            handle = when (primaryQuestion?.question?.reporter?.micFlagLabel) {
                "TRT" -> "@trtspor"
                "SD" -> "@SportsDigitale"
                "A SPOR" -> "@aspor"
                "S+" -> "@ssporttr"
                else -> "@beINSPORTSTR"
            },
            category = "GAZETECİ",
            isVerified = true,
            text = "🎙️ FLAŞ RÖPORTAJ | ${player.fullName} (${club.shortName}): \"${result.bestQuoteText}\"",
            likes = 640 + result.totalFollowersDelta,
            reposts = 115 + abs(result.totalFanSupportDelta) * 18
        )

        val journalistPost = XPostItem(
            id = "XP_INT_JRN_$nowMs",
            dayIndex = state.currentDayIndex,
            dateLabel = shortDate,
            authorName = "Yağız Sabuncuoğlu",
            handle = "@yagosabuncuoglu",
            category = "GAZETECİ",
            isVerified = true,
            text = when (result.dominantTone) {
                InterviewTone.HUMBLE -> "${player.fullName} maç sonu röportajında son derece olgun ve takım odaklı mesajlar verdi. Teknik heyet ve soyunma odası bu duruştan çok memnun."
                InterviewTone.CONFIDENT -> "${player.fullName} mikrofonlar karşısında öz güven patlaması yaşadı. Sahadaki liderliğini basın toplantısına da taşıyor. 🔥"
                InterviewTone.PROVOCATIVE -> "⚡ ${player.fullName}'ın Mixed Zone'daki sert açıklamaları gündemi sarstı! Kulüp koridorlarında ve sosyal medyada herkes bu sözleri konuşuyor."
            },
            likes = 920 + result.totalFollowersDelta,
            reposts = 180 + abs(result.totalFollowersDelta) / 5
        )

        val fanReactionXPost = XPostItem(
            id = "XP_INT_FAN_$nowMs",
            dayIndex = state.currentDayIndex,
            dateLabel = shortDate,
            authorName = "${club.shortName} Ruhu",
            handle = "@${club.shortName.lowercase()}_ruhu19",
            category = "TARAFTAR",
            isVerified = false,
            text = if (result.totalFanSupportDelta >= 0) {
                "Mikrofon karşısında bile formanın ağırlığını hissettiriyor! Yanındayız ${player.fullName}! 👏🔥 #${club.shortName}"
            } else {
                "${player.fullName}'ın maç sonu açıklamaları camiada tartışma yarattı. Sahada icraat bekliyoruz! 👀 #${club.shortName}"
            },
            likes = 310 + abs(result.totalFanSupportDelta) * 35,
            reposts = 44 + abs(result.totalFanSupportDelta) * 6
        )

        // 3. Instagram Post & Rich Comments for @telefon's Instagram feed
        val generatedInstaComments = buildInstaCommentsForInterview(state, result, nowMs)
        val interviewInstaPost = SocialMediaPost(
            id = "SOC_INT_$nowMs",
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            authorName = player.fullName,
            authorHandle = "@${player.firstName.lowercase()}${player.kitNumber}",
            isUserPost = true,
            content = "🎙️ Maç Sonu Flash Röportaj: \"${result.bestQuoteText}\"",
            likesCount = 480 + result.totalFollowersDelta + (player.followersCount / 30).coerceAtLeast(60),
            commentsCount = generatedInstaComments.size + 38,
            sentiment = sentimentLabel,
            topFanComments = generatedInstaComments.map { "${it.authorHandle}: ${it.text}" },
            instaComments = generatedInstaComments,
            isVerifiedAuthor = true,
            locationLabel = "${club.stadiumName} • Mixed Zone",
            imageSceneType = "STADIUM"
        )

        // 4. Story Item for @telefon's Stories
        val interviewStory = StoryItem(
            id = "STORY_INT_$nowMs",
            authorName = "${club.shortName} TV",
            authorRole = "Canlı Flash Röportaj",
            avatarHex = club.primaryColorHex,
            title = "🎙️ Flash Röportaj: ${player.firstName}",
            caption = result.mainHeadline,
            sceneType = "STADIUM",
            dayIndex = state.currentDayIndex
        )

        // 5. Update NPCs (Manager, Agent, Captain, Team Group & Press WhatsApp feedback on significant interview answers)
        val updatedNpcs = baseState.npcs.map { npc ->
            when (npc.id) {
                "NPC_MANAGER", "NPC_COACH" -> {
                    val newTrustVal = (npc.trust + result.totalManagerTrustDelta).coerceIn(5, 100)
                    val newAffinity = (npc.affinity + (result.totalManagerTrustDelta / 2)).coerceIn(5, 100)
                    if (abs(result.totalManagerTrustDelta) >= 3) {
                        val coachMsgText = if (result.totalManagerTrustDelta > 0) {
                            "${player.firstName}, maç sonu flash röportajdaki olgun ve takım odaklı açıklamalarını dinledim. Basın önünde teknik heyete ve arkadaşlarına sahip çıkman gerçek bir profesyonellik örneği. Böyle devam et!"
                        } else {
                            "${player.firstName}, televizyon mikrofonlarına yaptığın açıklamalar hiç hoşuma gitmedi! Takım içi konuları veya taktik kararlarımızı medya önünde tartışmaya açmanı istemiyorum, yarın odama uğra."
                        }
                        val msg = ChatMessage(
                            id = "MSG_COACH_INT_$nowMs",
                            senderName = npc.name,
                            isFromUser = false,
                            text = coachMsgText,
                            dayIndex = baseState.currentDayIndex,
                            timeLabel = "22:45",
                            emotionTag = if (result.totalManagerTrustDelta > 0) "Takdirkâr" else "Sert Uyarı",
                            impactSummary = "Hoca Güveni ${if (result.totalManagerTrustDelta > 0) "+${result.totalManagerTrustDelta}" else "${result.totalManagerTrustDelta}"}"
                        )
                        npc.copy(
                            trust = newTrustVal,
                            affinity = newAffinity,
                            currentMood = if (result.totalManagerTrustDelta > 0) "Memnun" else "Gergin",
                            messages = npc.messages + msg,
                            unreadCount = npc.unreadCount + 1
                        )
                    } else {
                        npc.copy(trust = newTrustVal, affinity = newAffinity)
                    }
                }

                "NPC_AGENT" -> {
                    val transferAnswered = result.answeredItems.find { it.question.topic == InterviewTopic.TRANSFER_RUMORS }
                    if (transferAnswered != null) {
                        val agentText = when (transferAnswered.selectedOption.tone) {
                            InterviewTone.HUMBLE -> "Kardeşim, canlı yayında kulübe bağlılık mesajın taraftarı mest etti. Sözleşme yenileme masasında elimiz çok güçlendi!"
                            InterviewTone.CONFIDENT -> "Flash röportajdaki öz güvenli duruşun harikaydı! Birkaç sportif direktör yayından hemen sonra beni arayıp durumunu sordu."
                            InterviewTone.PROVOCATIVE -> "Dostum mikrofona bıraktığın bomba açıklamadan sonra telefonum susmuyor! Kulüp yönetimi biraz gerildi ama transfer piyasasında herkes seni konuşuyor."
                        }
                        val msg = ChatMessage(
                            id = "MSG_AGENT_INT_$nowMs",
                            senderName = npc.name,
                            isFromUser = false,
                            text = agentText,
                            dayIndex = baseState.currentDayIndex,
                            timeLabel = "22:50",
                            emotionTag = "Kulis Bilgisi"
                        )
                        npc.copy(
                            messages = npc.messages + msg,
                            unreadCount = npc.unreadCount + 1
                        )
                    } else npc
                }

                "NPC_CAPTAIN", "NPC_TEAM_GROUP" -> {
                    if (abs(result.totalTeamChemistryDelta) >= 4) {
                        val groupMsg = ChatMessage(
                            id = "MSG_GRP_INT_${npc.id}_$nowMs",
                            senderName = npc.name,
                            isFromUser = false,
                            text = if (result.totalTeamChemistryDelta > 0) {
                                "Beyler ${player.firstName} canlı yayında hepimizin hakkını teslim etti, soyunma odası seninle gurur duyuyor kardeşim! 💪🔥"
                            } else {
                                "${player.firstName}, maç sonu televizyonda söylediklerin soyunma odasında biraz soğuk rüzgar estirdi, yarın idman öncesi konuşalım."
                            },
                            dayIndex = baseState.currentDayIndex,
                            timeLabel = "22:52",
                            emotionTag = if (result.totalTeamChemistryDelta > 0) "Kardeşlik" else "Soğuk"
                        )
                        npc.copy(
                            affinity = (npc.affinity + result.totalTeamChemistryDelta).coerceIn(10, 100),
                            messages = npc.messages + groupMsg,
                            unreadCount = npc.unreadCount + 1
                        )
                    } else {
                        npc.copy(affinity = (npc.affinity + (result.totalTeamChemistryDelta / 2)).coerceIn(10, 100))
                    }
                }

                "NPC_PRESS" -> {
                    val pressMsg = ChatMessage(
                        id = "MSG_PRESS_INT_$nowMs",
                        senderName = npc.name,
                        isFromUser = false,
                        text = "${player.firstName}, bu akşam Mixed Zone'da verdiğin \"${result.bestQuoteText.take(60)}...\" demeci yarın sabah tüm spor gazetelerinin birinci sayfasında olacak! Özel röportaj için de bekliyoruz.",
                        dayIndex = baseState.currentDayIndex,
                        timeLabel = "22:55",
                        emotionTag = "Manşet"
                    )
                    npc.copy(
                        affinity = (npc.affinity + 4).coerceIn(10, 100),
                        messages = npc.messages + pressMsg,
                        unreadCount = npc.unreadCount + 1
                    )
                }

                else -> npc
            }
        }

        val updatedReport = baseState.lastMatchReport?.copy(
            pressHeadline = result.mainHeadline
        )

        val summaryToast = buildString {
            append("Flash Röportaj tamamlandı! ")
            append("Takipçi +${result.totalFollowersDelta}, ")
            append("Taraftar ${if (result.totalFanSupportDelta >= 0) "+${result.totalFanSupportDelta}" else "${result.totalFanSupportDelta}"}, ")
            append("Hoca Güveni ${if (result.totalManagerTrustDelta >= 0) "+${result.totalManagerTrustDelta}" else "${result.totalManagerTrustDelta}"}.")
        }

        val updatedState = baseState.copy(
            updatedAtMs = nowMs,
            player = updatedPlayer,
            managerTrust = updatedTrust,
            teamChemistry = updatedChemistry,
            pressCompletedForLastMatch = true,
            lastMatchReport = updatedReport,
            newsEvents = extraNewsList + baseState.newsEvents,
            xPosts = listOf(broadcasterPost, journalistPost, fanReactionXPost) + baseState.xPosts,
            socialPosts = listOf(interviewInstaPost) + baseState.socialPosts,
            stories = listOf(interviewStory) + baseState.stories,
            npcs = updatedNpcs,
            eventHistory = listOf(
                GameEvent(
                    id = "EVT_INTERVIEW_$nowMs",
                    dayIndex = baseState.currentDayIndex,
                    timestampMs = nowMs,
                    eventType = "POST_MATCH_INTERVIEW",
                    contextDescription = result.mainHeadline,
                    appliedEffects = "Üslup: ${result.dominantTone.labelTr}, Moral: ${result.totalMoraleDelta}, Taraftar: ${result.totalFanSupportDelta}, Hoca: ${result.totalManagerTrustDelta}, Takipçi: +${result.totalFollowersDelta}"
                )
            ) + baseState.eventHistory
        )

        return updatedState to summaryToast
    }

    private fun buildInstaCommentsForInterview(
        state: CareerSaveState,
        result: InterviewSessionResult,
        nowMs: Long
    ): List<InstaCommentItem> {
        val clubShort = state.currentClub.shortName
        val clubHex = state.currentClub.primaryColorHex
        return when (result.dominantTone) {
            InterviewTone.HUMBLE -> listOf(
                InstaCommentItem(
                    id = "IC_INT_${nowMs}_1",
                    authorHandle = "${clubShort.lowercase()}_tribun",
                    authorName = "$clubShort Tribün Sesi",
                    isVerified = false,
                    avatarHex = clubHex,
                    text = "Hem sahada terinin son damlasına kadar savaşıyor hem de mikrofonda karakter dersi veriyor! 👏🔥",
                    timeLabel = "1d",
                    likesCount = 84
                ),
                InstaCommentItem(
                    id = "IC_INT_${nowMs}_2",
                    authorHandle = "futbol_analiz_tr",
                    authorName = "Futbol Analiz TR",
                    isVerified = true,
                    avatarHex = "#1D9BF0",
                    text = "Takım arkadaşlarını ve teknik heyeti öne çıkaran açıklamaları tam bir kaptan kumaşı.",
                    timeLabel = "2d",
                    likesCount = 51
                ),
                InstaCommentItem(
                    id = "IC_INT_${nowMs}_3",
                    authorHandle = "burak_1907_fan",
                    authorName = "Burak Yılmaz",
                    isVerified = false,
                    avatarHex = "#10B981",
                    text = "Helal olsun aslanım, bu forma sana çok yakışıyor! 👑⚽",
                    timeLabel = "4d",
                    likesCount = 29
                )
            )

            InterviewTone.CONFIDENT -> listOf(
                InstaCommentItem(
                    id = "IC_INT_${nowMs}_1",
                    authorHandle = "spor_gecesi_hd",
                    authorName = "Spor Gecesi Digital",
                    isVerified = true,
                    avatarHex = "#F59E0B",
                    text = "Flash röportajdaki öz güveni şaka mı?! Yıldız mentalitesi budur! 🔥🎙️",
                    timeLabel = "1d",
                    likesCount = 142
                ),
                InstaCommentItem(
                    id = "IC_INT_${nowMs}_2",
                    authorHandle = "${clubShort.lowercase()}_ultras",
                    authorName = "$clubShort Ultras",
                    isVerified = false,
                    avatarHex = clubHex,
                    text = "Rakiplere korku salmaya devam et kral! Arkandayız! ⚡🦁",
                    timeLabel = "3d",
                    likesCount = 77
                ),
                InstaCommentItem(
                    id = "IC_INT_${nowMs}_3",
                    authorHandle = "kerem_futbol",
                    authorName = "Kerem Demir",
                    isVerified = false,
                    avatarHex = "#8B5CF6",
                    text = "Hem sahada konuşuyor hem mikrofonda! Sezonun oyuncusu net.",
                    timeLabel = "5d",
                    likesCount = 34
                )
            )

            InterviewTone.PROVOCATIVE -> listOf(
                InstaCommentItem(
                    id = "IC_INT_${nowMs}_1",
                    authorHandle = "kaos_futbol_tr",
                    authorName = "Flaş Futbol Gündemi",
                    isVerified = true,
                    avatarHex = "#EF4444",
                    text = "Canlı yayında ortalığı ateşe verdi! Bu sözler bu hafta tüm spor programlarında tartışılır 👀🔥",
                    timeLabel = "1d",
                    likesCount = 215
                ),
                InstaCommentItem(
                    id = "IC_INT_${nowMs}_2",
                    authorHandle = "${clubShort.lowercase()}_sevdalisi",
                    authorName = "Taraftar Kürsüsü",
                    isVerified = false,
                    avatarHex = "#F97316",
                    text = "Sözünü hiç sakınmıyor ama soyunma odasında hocayla papaz olmasa bari 😅",
                    timeLabel = "2d",
                    likesCount = 93
                ),
                InstaCommentItem(
                    id = "IC_INT_${nowMs}_3",
                    authorHandle = "rakip_analiz",
                    authorName = "Tribün Nabzı",
                    isVerified = false,
                    avatarHex = "#64748B",
                    text = "Fazla sert konuşmuş, rövanş maçında bu sözleri hatırlatırlar!",
                    timeLabel = "4d",
                    likesCount = 61
                )
            )
        }
    }
}
