package com.example.engine.social

import com.example.model.CareerSaveState
import com.example.model.PlayerProfile
import kotlin.random.Random

/**
 * Kız Arkadaş, Şöhret/Cazibe ve Magazin/Sevgililik Hayatı Motoru.
 * Şöhret kademelerine (PlayerFameTier) göre ünlü modeller, oyuncular ve influencer kızlardan
 * Instagram DM'leri, ilişki aşamaları (Tanışma -> Flört -> Sevgili -> Nişanlı), hediyeler,
 * Bodrum/Maldivler tatilleri, VIP maç daveti (tribün desteği bonusu) ve paparazzi/magazin dramaları yönetilir.
 */
object RomanceAndDatingEngine {

    val INITIAL_CANDIDATES: List<GirlfriendProfile> = listOf(
        GirlfriendProfile(
            id = "partner_yasemin_kaya",
            fullName = "Yasemin Kaya",
            handle = "@yaseminkaya",
            archetype = PartnerArchetype.FITNESS_CREATOR,
            professionTr = "Fitness & Pilates Koçu",
            age = 22,
            city = "İstanbul",
            avatarHex = "#10B981",
            secondaryHex = "#064E3B",
            bio = "Sağlıklı yaşam, pilates & pozitif enerji ✨ Bebek, İstanbul",
            followersCount = 65_000L,
            minFameTier = PlayerFameTier.AMATEUR,
            minCharisma = 30,
            statusQuote = "Disiplin ve tutku her şeyin anahtarıdır 💪"
        ),
        GirlfriendProfile(
            id = "partner_selin_aksoy",
            fullName = "Selin Aksoy",
            handle = "@selinaksoy",
            archetype = PartnerArchetype.SPORTS_PRESENTER,
            professionTr = "Spor Spikeri & Medya Sunucusu",
            age = 24,
            city = "İstanbul",
            avatarHex = "#2563EB",
            secondaryHex = "#1E3A8A",
            bio = "Futbolun kalbinden canlı yayınlar 🎙️ Trendyol Süper Lig & Avrupa",
            followersCount = 280_000L,
            minFameTier = PlayerFameTier.SEMI_PRO,
            minCharisma = 45,
            statusQuote = "Saha içi performans kadar tutku da konuşulmalı ⚽"
        ),
        GirlfriendProfile(
            id = "partner_defne_sezgin",
            fullName = "Defne Sezgin",
            handle = "@defnesezgin",
            archetype = PartnerArchetype.MUSICIAN,
            professionTr = "Pop Sanatçısı & DJ",
            age = 23,
            city = "Bodrum / İstanbul",
            avatarHex = "#F59E0B",
            secondaryHex = "#78350F",
            bio = "Müzik, gece ritmi & akustik sahneler 🎵 Yeni teklim dijital platformlarda!",
            followersCount = 680_000L,
            minFameTier = PlayerFameTier.RISING_STAR,
            minCharisma = 55,
            statusQuote = "Hayat bir sahne, en yüksek sesle yaşa ✨"
        ),
        GirlfriendProfile(
            id = "partner_derin_karahan",
            fullName = "Derin Karahan",
            handle = "@derinkarahan",
            archetype = PartnerArchetype.FASHION_INFLUENCER,
            professionTr = "Lüks Moda & Stil İkonu",
            age = 23,
            city = "Nişantaşı, İstanbul",
            avatarHex = "#EC4899",
            secondaryHex = "#831843",
            bio = "Moda, haute couture & lüks yaşam 👠 Paris - Milano - İstanbul",
            followersCount = 1_850_000L,
            minFameTier = PlayerFameTier.SUPER_LIG_STAR,
            minCharisma = 68,
            statusQuote = "Stil sadece ne giydiğin değil, kim olduğundur 💎"
        ),
        GirlfriendProfile(
            id = "partner_melis_vural",
            fullName = "Melis Vural",
            handle = "@melisvural",
            archetype = PartnerArchetype.ACTRESS,
            professionTr = "Dizi & Sinema Başrol Oyuncusu",
            age = 25,
            city = "Bebek, İstanbul",
            avatarHex = "#9333EA",
            secondaryHex = "#3B0764",
            bio = "Prime-time reyting rekortmeni dizi başrolü 🎬 Altın Kelebek En İyi Kadın Oyuncu",
            followersCount = 4_200_000L,
            minFameTier = PlayerFameTier.SUPER_LIG_STAR,
            minCharisma = 75,
            statusQuote = "Kendi hikayenin başrolü olmaktan asla vazgeçme 🌟"
        ),
        GirlfriendProfile(
            id = "partner_chloe_dubois",
            fullName = "Chloé Dubois",
            handle = "@chloedubois",
            archetype = PartnerArchetype.SUPERMODEL,
            professionTr = "Uluslararası Podyum Süpermodeli",
            age = 24,
            city = "Paris / İstanbul",
            avatarHex = "#E11D48",
            secondaryHex = "#4C0519",
            bio = "Vogue & Harper's Bazaar cover girl 💃 Paris Fashion Week runway model",
            followersCount = 8_900_000L,
            minFameTier = PlayerFameTier.NATIONAL_HERO,
            minCharisma = 82,
            statusQuote = "From runway to real life with pure elegance 👑"
        )
    )

    val DATING_ACTIONS: List<DatingActionItem> = listOf(
        DatingActionItem(
            id = "act_dinner_bosphorus",
            category = DatingInteractionCategory.DINNER,
            titleTr = "Boğaz Manzaralı Romantik Akşam Yemeği",
            descriptionTr = "Boğaz kenarında şık bir balık restoranında mum ışığında baş başa yemek.",
            iconEmoji = "🍽️",
            costEur = 1_200L,
            energyCost = 8,
            minStage = RelationshipStage.MEETING,
            affectionGain = 12,
            dramaChange = 2,
            paparazziRiskPct = 20
        ),
        DatingActionItem(
            id = "act_dinner_vip_rooftop",
            category = DatingInteractionCategory.DINNER,
            titleTr = "Özel Kapatılan VIP Çatı Restoranı",
            descriptionTr = "Şehrin en gözde gökdeleninde sadece ikiniz için özel rezerve edilmiş lüks restoran.",
            iconEmoji = "🥂",
            costEur = 5_800L,
            energyCost = 12,
            minStage = RelationshipStage.FLIRTING,
            affectionGain = 18,
            dramaChange = 5,
            paparazziRiskPct = 35
        ),
        DatingActionItem(
            id = "act_gift_roses",
            category = DatingInteractionCategory.GIFT,
            titleTr = "101 Kırmızı Gül & Özel Çikolata",
            descriptionTr = "Evine veya setine gönderilen devasa bir kırmızı gül buketi ve zarif bir not.",
            iconEmoji = "🌹",
            costEur = 450L,
            energyCost = 3,
            minStage = RelationshipStage.MEETING,
            affectionGain = 8,
            dramaChange = -3,
            paparazziRiskPct = 8
        ),
        DatingActionItem(
            id = "act_gift_designer_bag",
            category = DatingInteractionCategory.GIFT,
            titleTr = "Fransız Tasarım Lüks Çanta",
            descriptionTr = "Paris'ten özel getirtilmiş sezonun en çok aranan deri tasarım çantası.",
            iconEmoji = "👜",
            costEur = 4_500L,
            energyCost = 4,
            minStage = RelationshipStage.FLIRTING,
            affectionGain = 16,
            dramaChange = 4,
            paparazziRiskPct = 25
        ),
        DatingActionItem(
            id = "act_gift_diamond_necklace",
            category = DatingInteractionCategory.GIFT,
            titleTr = "Pırlanta Gerdanlık & Küpe Seti",
            descriptionTr = "Özel sertifikalı, ışıl ışıl parlayan lüks pırlanta mücevher takımı.",
            iconEmoji = "💎",
            costEur = 19_500L,
            energyCost = 5,
            minStage = RelationshipStage.IN_RELATIONSHIP,
            affectionGain = 24,
            dramaChange = 8,
            paparazziRiskPct = 40
        ),
        DatingActionItem(
            id = "act_gift_engagement_ring",
            category = DatingInteractionCategory.GIFT,
            titleTr = "Özel Kesim Tektaş Pırlanta Yüzük (Evlilik Teklifi)",
            descriptionTr = "Unutulmaz bir atmosferde diz çöküp evlilik teklifinde bulunmak için 3 karat pırlanta yüzük.",
            iconEmoji = "💍",
            costEur = 65_000L,
            energyCost = 12,
            minStage = RelationshipStage.IN_RELATIONSHIP,
            affectionGain = 35,
            dramaChange = -15,
            paparazziRiskPct = 70
        ),
        DatingActionItem(
            id = "act_vac_bodrum",
            category = DatingInteractionCategory.VACATION,
            titleTr = "Hafta Sonu Bodrum Özel Villa & Yat Kaçamağı",
            descriptionTr = "Bodrum Cennet Koyu'nda helikopterle ulaşılan lüks villa ve özel motoryat gezisi.",
            iconEmoji = "⛵",
            costEur = 14_000L,
            energyCost = 20,
            minStage = RelationshipStage.FLIRTING,
            affectionGain = 22,
            dramaChange = 12,
            paparazziRiskPct = 55
        ),
        DatingActionItem(
            id = "act_vac_maldives",
            category = DatingInteractionCategory.VACATION,
            titleTr = "Maldivler Su Üstü Villası Rüya Tatili",
            descriptionTr = "Turkuaz deniz üzerinde şeffaf tabanlı lüks bungalovda 4 günlük unutulmaz tatil.",
            iconEmoji = "🏖️",
            costEur = 42_000L,
            energyCost = 25,
            minStage = RelationshipStage.IN_RELATIONSHIP,
            affectionGain = 30,
            dramaChange = 6,
            paparazziRiskPct = 45
        ),
        DatingActionItem(
            id = "act_match_vip",
            category = DatingInteractionCategory.MATCH_INVITE,
            titleTr = "VIP Locada Maç Daveti (Tribün Desteği)",
            descriptionTr = "Stadyumda sana özel ayrılan VIP locada maçını izlemesi. Tribünde sevgilini görmek sana sahada ekstra motivasyon ve gol sevinci coşkusu katar!",
            iconEmoji = "🎟️",
            costEur = 2_500L,
            energyCost = 4,
            minStage = RelationshipStage.MEETING,
            affectionGain = 14,
            dramaChange = 5,
            paparazziRiskPct = 35,
            matchBonusMorale = 6,
            matchBonusFinishing = 3
        ),
        DatingActionItem(
            id = "act_night_club",
            category = DatingInteractionCategory.NIGHT_OUT,
            titleTr = "Kuruçeşme VIP Gece Kulübü & Eğlence",
            descriptionTr = "Boğaz hattının en popüler gece kulübünde arkadaş grubuyla sabaha kadar eğlence.",
            iconEmoji = "🍾",
            costEur = 8_500L,
            energyCost = 26,
            minStage = RelationshipStage.FLIRTING,
            affectionGain = 10,
            dramaChange = 25,
            paparazziRiskPct = 75
        )
    )

    /**
     * Oyuncunun anlık şöhret kademesini hesaplar.
     */
    fun resolvePlayerFameTier(player: PlayerProfile): PlayerFameTier {
        val overall = player.overallRating
        val followers = player.followersCount.toLong()
        return when {
            overall >= 86 || followers >= 4_000_000L -> PlayerFameTier.GLOBAL_SUPERSTAR
            overall >= 80 || followers >= 800_000L -> PlayerFameTier.NATIONAL_HERO
            overall >= 74 || followers >= 150_000L -> PlayerFameTier.SUPER_LIG_STAR
            overall >= 66 || followers >= 25_000L -> PlayerFameTier.RISING_STAR
            overall >= 55 || followers >= 2_500L -> PlayerFameTier.SEMI_PRO
            else -> PlayerFameTier.AMATEUR
        }
    }

    /**
     * Oyuncunun şöhret ve karizmasına göre yeni Instagram DM tanışma isteklerini kontrol eder.
     */
    fun checkAndGenerateInboundDms(
        state: CareerSaveState,
        existingCandidates: List<GirlfriendProfile>,
        currentInboundDms: List<RomanceDmInvitation>
    ): List<RomanceDmInvitation> {
        val fameTier = resolvePlayerFameTier(state.player)
        val candidatePool = if (existingCandidates.isNotEmpty()) existingCandidates else INITIAL_CANDIDATES
        val result = currentInboundDms.toMutableList()

        val activePartnerId = candidatePool.firstOrNull { it.isActivePartner }?.id

        for (candidate in candidatePool) {
            // Eğer aktif sevgiliyse veya zaten DM listesindeyse atla
            if (candidate.id == activePartnerId) continue
            if (result.any { it.candidate.id == candidate.id }) continue

            // Şöhret kademesi yeterli mi?
            val isTierSufficient = fameTier.ordinal >= candidate.minFameTier.ordinal
            if (!isTierSufficient) continue

            // Şöhret kademesine göre olasılık zarı
            val baseChance = when (fameTier) {
                PlayerFameTier.AMATEUR -> 10
                PlayerFameTier.SEMI_PRO -> 25
                PlayerFameTier.RISING_STAR -> 45
                PlayerFameTier.SUPER_LIG_STAR -> 65
                PlayerFameTier.NATIONAL_HERO -> 80
                PlayerFameTier.GLOBAL_SUPERSTAR -> 95
            }

            if (Random.nextInt(100) < baseChance) {
                val openingMessage = generateDmOpeningMessage(candidate, state.player)
                result.add(
                    RomanceDmInvitation(
                        id = "dm_req_${candidate.id}_${state.currentDayIndex}",
                        candidate = candidate,
                        openingMessage = openingMessage,
                        receivedDayIndex = state.currentDayIndex
                    )
                )
            }
        }

        return result
    }

    private fun generateDmOpeningMessage(candidate: GirlfriendProfile, player: PlayerProfile): String {
        return when (candidate.archetype) {
            PartnerArchetype.SPORTS_PRESENTER ->
                "Selam ${player.firstName}! Dünkü maçtaki performansın harikaydı, programda da bahsettik. Bir kahve içip futbol sohbeti yapalım mı? ☕🎙️"
            PartnerArchetype.FASHION_INFLUENCER ->
                "Merhaba ${player.firstName}! Maç sonu basın toplantısındaki ceketin ve tarzın inanılmaz iyiydi. Ortak bir stil çekimi yapsak mı? ✨👠"
            PartnerArchetype.ACTRESS ->
                "Tebrikler ${player.firstName}! Maçtaki hırsına ve attığın gole bayıldım. Yakında derbiye geleceğim, görüşmek üzere 🎬🍿"
            PartnerArchetype.SUPERMODEL ->
                "Hi ${player.firstName}! Istanbul Fashion Week için buradayım ve adını çok duydum. Boğaz'da bir akşam yemeğine ne dersin? 💃🌟"
            PartnerArchetype.MUSICIAN ->
                "Hey ${player.firstName}! Gol müziğin olarak şarkımı çalmışlar stadyumda, çok gururlandım! Bir sonraki konserimde VIP davetlimsin 🎵🎶"
            PartnerArchetype.FITNESS_CREATOR ->
                "Selam ${player.firstName}! Sahadaki fiziksel kondisyonun ve hızın harika. Yeni sezonda ortak bir antrenman videosu çekelim mi? 💪🥗"
        }
    }

    /**
     * Yapılabilecek etkileşimleri döndürür.
     */
    fun getAvailableActions(partner: GirlfriendProfile): List<DatingActionItem> {
        return DATING_ACTIONS.filter { action ->
            action.minStage.levelOrder <= partner.stage.levelOrder
        }
    }

    /**
     * Seçilen etkileşimi gerçekleştirir.
     */
    fun executeInteraction(
        state: CareerSaveState,
        partner: GirlfriendProfile,
        action: DatingActionItem
    ): RomanceInteractionOutcome {
        val prevStage = partner.stage
        var newAffection = (partner.affection + action.affectionGain).coerceIn(0, 100)
        var newDrama = (partner.dramaLevel + action.dramaChange).coerceIn(0, 100)

        // Aşama ilerleme kontrolü
        var stageAdvanced = false
        var currentStage = prevStage

        if (action.id == "act_gift_engagement_ring" && prevStage == RelationshipStage.IN_RELATIONSHIP && newAffection >= 85) {
            currentStage = RelationshipStage.ENGAGED
            stageAdvanced = true
        } else if (prevStage == RelationshipStage.MEETING && newAffection >= RelationshipStage.MEETING.minAffectionForNext) {
            currentStage = RelationshipStage.FLIRTING
            stageAdvanced = true
        } else if (prevStage == RelationshipStage.FLIRTING && newAffection >= RelationshipStage.FLIRTING.minAffectionForNext) {
            currentStage = RelationshipStage.IN_RELATIONSHIP
            stageAdvanced = true
        }

        // Paparazzi & Magazin Olayı Kontrolü
        val fameTier = resolvePlayerFameTier(state.player)
        val totalPaparazziOdds = (action.paparazziRiskPct + (partner.archetype.basePaparazziInterest / 3) + (fameTier.ordinal * 6)).coerceIn(5, 95)
        val isPaparazziSpotted = Random.nextInt(100) < totalPaparazziOdds

        var paparazziIncident: PaparazziDramaIncident? = null
        var moraleBonus = 0
        var formBonus = 0

        if (isPaparazziSpotted) {
            paparazziIncident = generatePaparazziScandal(state, partner, action)
            newDrama = (newDrama + 15).coerceIn(0, 100)
            moraleBonus -= paparazziIncident.moralePenalty
            formBonus -= paparazziIncident.formPenalty
        }

        // VIP Maç Daveti Bonusu
        var matchBonusActive = false
        if (action.category == DatingInteractionCategory.MATCH_INVITE) {
            matchBonusActive = true
            moraleBonus += action.matchBonusMorale
        }

        val storyText = buildOutcomeStoryText(partner, action, stageAdvanced, currentStage, isPaparazziSpotted)

        return RomanceInteractionOutcome(
            actionId = action.id,
            actionTitle = action.titleTr,
            partnerId = partner.id,
            partnerName = partner.fullName,
            success = true,
            resultStoryText = storyText,
            affectionChange = action.affectionGain,
            dramaChange = if (isPaparazziSpotted) action.dramaChange + 15 else action.dramaChange,
            costEur = action.costEur,
            energyCost = action.energyCost,
            previousStage = prevStage,
            currentStage = currentStage,
            stageAdvanced = stageAdvanced,
            paparazziIncident = paparazziIncident,
            matchBonusActivated = matchBonusActive,
            playerMoraleBonus = moraleBonus,
            playerFormBonus = formBonus
        )
    }

    private fun generatePaparazziScandal(
        state: CareerSaveState,
        partner: GirlfriendProfile,
        action: DatingActionItem
    ): PaparazziDramaIncident {
        val outlets = listOf("Snob Magazin", "Paparazzi TV", "Kelebek Magazin", "Süper Kulüp Basını", "Akşam Magazin")
        val outlet = outlets.random()

        val (headline, details, managerTepkisi) = when (action.category) {
            DatingInteractionCategory.NIGHT_OUT -> Triple(
                "ŞOK! YILDIZ FUTBOLCU GECE KULÜBÜ ÇIKIŞI SABAHA KARŞI YAKALANDI!",
                "${partner.fullName} ile Bebek'teki lüks gece kulübünden sabahın ilk ışıklarında el ele çıkan yıldız futbolcu, flaşların patlamasıyla neye uğradığını şaşırdı.",
                "Teknik Direktör uyardı: 'Gece hayatı yerine bir sonraki maça odaklan. Sahada koşmazsan kulübeye çekilirsin!'"
            )
            DatingInteractionCategory.VACATION -> Triple(
                "BODRUM'DA LÜKS YAT KAÇAMAĞI! AŞK RESMİLEŞTİ!",
                "Cennet Koyu'nda demirli süper yatta ${partner.fullName} ile güneşlenen yıldız futbolcunun görüntüleri sosyal medyayı salladı. Taraftarlar antrenman gününde tatil yapılmasını eleştirdi.",
                "Kulüp Yönetimi: 'Özel hayata saygılıyız ancak sezon ortasında izin günlerinin disiplinle geçirilmesini bekliyoruz.'"
            )
            DatingInteractionCategory.GIFT -> Triple(
                "MİLYONLUK JEST! SEVGİLİSİNE SERVET DEĞERİNDE HEDİYE ALDI!",
                "Yıldız oyuncu, ${partner.fullName}'e aldığı astronomik mücevherle magazin manşetlerine oturdu. Basın 'Sahadaki performanstan çok harcamaları konuşuluyor' başlıkları attı.",
                "Hoca Tepkisi: 'Harcadığın parayla değil, sahada döktüğün terle gündeme gelmelisin.'"
            )
            DatingInteractionCategory.DINNER -> Triple(
                "BOĞAZ'DA ROMANTİK BULUŞMA: KAMERALARA YAKALANDILAR!",
                "Restoran çıkışı muhabirlerin 'Aşk başladı mı?' soruları karşısında tebessüm eden çift, korumalar eşliğinde lüks araca bindi.",
                "Sosyal Medya: Taraftarlar yeni ilişkiyi coşkuyla karşılarken magazin sayfaları fotoğraflarla çalkalanıyor."
            )
            DatingInteractionCategory.MATCH_INVITE -> Triple(
                "TRİBÜNDE SÜRPRİZ MİSAFİR! YILDIZIN GÖZÜ LOCADAYDI!",
                "Maç boyunca gözlerini VIP locadaki ${partner.fullName}'den alamayan yıldız futbolcu, tribüne kalp işareti yaparak aşkını tüm stadyuma ilan etti.",
                "Tribün Yorumu: 'Aşk yaradı, sahada iki kişilik koştu!'"
            )
        }

        return PaparazziDramaIncident(
            id = "drama_${state.currentDayIndex}_${Random.nextInt(10000)}",
            dayIndex = state.currentDayIndex,
            partnerName = partner.fullName,
            headline = headline,
            mediaOutlet = outlet,
            incidentType = action.category.name,
            detailsText = details,
            managerReaction = managerTepkisi,
            fanReaction = "Sosyal medyada binlerce paylaşım ve yorum yapıldı.",
            moralePenalty = if (action.category == DatingInteractionCategory.NIGHT_OUT) 6 else 2,
            formPenalty = if (action.category == DatingInteractionCategory.NIGHT_OUT) 4 else 1,
            fameGain = 450
        )
    }

    private fun buildOutcomeStoryText(
        partner: GirlfriendProfile,
        action: DatingActionItem,
        stageAdvanced: Boolean,
        newStage: RelationshipStage,
        paparazziSpotted: Boolean
    ): String {
        val base = when (action.id) {
            "act_gift_engagement_ring" ->
                "Göz kamaştırıcı pırlanta yüzükle diz çöktün ve 'Benimle evlenir misin?' dedin. ${partner.fullName} gözyaşları içinde 'EVET!' diyerek boynuna sarıldı! Artık nişanlısınız! 💍🥂"
            "act_match_vip" ->
                "${partner.fullName} VIP locada senin formanı giyerek maçı izledi. Isınırken ve sahaya çıkarken locaya el salladın. Tribün desteği sana sahada muazzam bir güç ve özgüven verdi! ⚽❤️"
            "act_vac_bodrum" ->
                "Bodrum'un masmavi sularında ve lüks yatta rüya gibi iki gün geçirdiniz. Aranızdaki bağ çok daha kuvvetlendi. 🌊⛵"
            "act_vac_maldives" ->
                "Maldivler'in bembeyaz kumsalında gün batımı yürüyüşleri ve romantik akşamlar unutulmaz anılar bıraktı. 🏖️🌴"
            "act_night_club" ->
                "Müziğin ve dansın ritmiyle eğlenceli bir gece geçirdiniz ancak geç saatlere kadar kalmak fiziksel enerjini tüketti. 🍾✨"
            else ->
                "${partner.fullName} ile harika vakit geçirdiniz. Yüzündeki samimi tebessüm ve ilgisi sana moral aşıladı. 💕"
        }

        val advanceNote = if (stageAdvanced) {
            "\n\n🎉 TEBRİKLER! İlişkiniz yeni bir seviyeye taşındı: ${newStage.titleTr} (${newStage.iconEmoji})"
        } else ""

        val paparazziNote = if (paparazziSpotted) {
            "\n\n📸 DİKKAT! Gece çıkışında paparazzilere yakalandınız! Magazin basını manşet attı ve hocanın kulağına gitti."
        } else ""

        return base + advanceNote + paparazziNote
    }

    /**
     * Sevgililikte oluşan bir krizi veya paparazzi olayını telafi etme aksiyonu.
     */
    fun resolveDramaAction(
        partner: GirlfriendProfile,
        resolutionType: String // "FLOWERS", "APOLOGY_DINNER", "PUBLIC_STATEMENT"
    ): Pair<GirlfriendProfile, String> {
        return when (resolutionType) {
            "FLOWERS" -> {
                val updated = partner.copy(
                    dramaLevel = (partner.dramaLevel - 20).coerceAtLeast(0),
                    affection = (partner.affection + 5).coerceIn(0, 100)
                )
                updated to "Gönderdiğin zarif özür çiçeği ve samimi mesaj buzları eritti. 🌸"
            }
            "APOLOGY_DINNER" -> {
                val updated = partner.copy(
                    dramaLevel = (partner.dramaLevel - 35).coerceAtLeast(0),
                    affection = (partner.affection + 12).coerceIn(0, 100)
                )
                updated to "Baş başa yenen sakin akşam yemeğinde tüm yanlış anlaşılmaları konuştunuz ve tatlıya bağladınız. 🍷"
            }
            else -> {
                val updated = partner.copy(
                    dramaLevel = (partner.dramaLevel - 15).coerceAtLeast(0)
                )
                updated to "Basına yapılan ölçülü açıklama magazin fırtınasını dindirdi. 📰"
            }
        }
    }
}
