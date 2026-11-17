package com.example.engine.progression

import com.example.engine.WorldDataCatalog
import com.example.model.CareerSaveState
import com.example.model.Club
import com.example.model.PlayerProfile
import com.example.model.SquadRole
import com.example.model.TransferOffer
import com.example.model.XPostItem
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Gerçekçi transfer piyasası motoru.
 * - Katı transfer pencereleri (Yaz: Ağustos, Kış: Ocak-Şubat)
 * - Alt liglerden çıkışın zorluğu ve kademeli lig sıçrama kuralları
 * - Performans serisi (Son maçlar ortalaması, gol krallığı) gereksinimi
 * - Genç yetenek / yaş scouting filtreleri
 * - Kulüp bonservis inadı, pazarlık ve kiralık seçenekleri
 */
object TransferMarketEngine {

    enum class TransferWindowStatus(val titleTr: String, val isOpen: Boolean) {
        OPEN_SUMMER("Yaz Transfer Dönemi (Açık)", true),
        OPEN_WINTER("Ara Transfer Dönemi (Açık)", true),
        CLOSED("Transfer Penceresi Kapalı", false)
    }

    /**
     * Gün indeksine göre transfer penceresinin durumunu belirler.
     * Gün 1-21: Ağustos (Yaz transfer penceresi)
     * Gün 141-185: Ocak & Şubat başı (Kış ara transfer penceresi)
     */
    fun getTransferWindowStatus(dayIndex: Int): TransferWindowStatus {
        val zeroBased = (dayIndex - 1).coerceAtLeast(0)
        val monthIdx = ((10 + zeroBased) / 30) % 10
        val dayOfMonth = (10 + zeroBased) % 30 + 1

        return when {
            // Ağustos (monthIdx 0) ve Eylül'ün ilk 5 günü
            monthIdx == 0 || (monthIdx == 1 && dayOfMonth <= 5) -> TransferWindowStatus.OPEN_SUMMER
            // Ocak (monthIdx 5) ve Şubat'ın ilk 8 günü (monthIdx 6)
            monthIdx == 5 || (monthIdx == 6 && dayOfMonth <= 8) -> TransferWindowStatus.OPEN_WINTER
            else -> TransferWindowStatus.CLOSED
        }
    }

    data class TransferEligibility(
        val isEligible: Boolean,
        val windowStatus: TransferWindowStatus,
        val requiredRatingAverage: Double,
        val currentRatingAverage: Double,
        val reasonMessage: String
    )

    /**
     * Oyuncunun transfer teklifi almaya uygun olup olmadığını denetler.
     */
    fun evaluateTransferEligibility(
        state: CareerSaveState,
        player: PlayerProfile,
        club: Club
    ): TransferEligibility {
        val window = getTransferWindowStatus(state.currentDayIndex)
        val stats = player.currentSeasonStats
        val requiredThreshold = LeagueDifficultyCalculator.getScoutAttentionRatingThreshold(club.tier, player.age)

        val avgRating = if (stats.appearances > 0) stats.averageRating else 6.0
        val isTopScorerCandidate = stats.goals >= 4

        if (!window.isOpen) {
            return TransferEligibility(
                isEligible = false,
                windowStatus = window,
                requiredRatingAverage = requiredThreshold,
                currentRatingAverage = avgRating,
                reasonMessage = "Transfer penceresi kapalı. Yalnızca basın söylentileri ve scout gözlemleri takip edilebilir."
            )
        }

        // En az 2 maça çıkmış olması gerekir
        if (stats.appearances < 2) {
            return TransferEligibility(
                isEligible = false,
                windowStatus = window,
                requiredRatingAverage = requiredThreshold,
                currentRatingAverage = avgRating,
                reasonMessage = "Kulüplerin resmi teklif sunması için en az 2 resmi maç performansı görülmelidir."
            )
        }

        // Alt liglerden üst liglere sıçramak için performans serisi şartı
        val hasStellarPerformance = avgRating >= requiredThreshold || isTopScorerCandidate

        if (!hasStellarPerformance) {
            return TransferEligibility(
                isEligible = false,
                windowStatus = window,
                requiredRatingAverage = requiredThreshold,
                currentRatingAverage = avgRating,
                reasonMessage = "Scoutların dikkatini çekmek için son maçlar ortalaman en az ${"%.2f".format(requiredThreshold)} olmalı (Mevcut: ${"%.2f".format(avgRating)})."
            )
        }

        // Yaş kısıtı: Alt ligde (BAL veya Amatör) 26+ yaşındaki bir oyuncuya doğrudan üst liglerden teklif yağmaz
        if (club.tier >= 5 && player.age >= 26 && stats.goals < 8) {
            return TransferEligibility(
                isEligible = false,
                windowStatus = window,
                requiredRatingAverage = requiredThreshold,
                currentRatingAverage = avgRating,
                reasonMessage = "Alt liglerde 26 yaş üstü oyuncuların üst liglere transferi için olağanüstü gol krallığı (8+ gol) gerekir."
            )
        }

        return TransferEligibility(
            isEligible = true,
            windowStatus = window,
            requiredRatingAverage = requiredThreshold,
            currentRatingAverage = avgRating,
            reasonMessage = "Yüksek performans ve scout onayı! Resmi teklif değerlendirilebilir."
        )
    }

    /**
     * Gerçekçi transfer teklifi üretir.
     * Alt liglerden sıçrama kademelidir:
     * BAL (Tier 5) -> En fazla 3. Lig (Tier 4) veya 2. Lig (Tier 3). Süper Lig'e doğrudan sıçrayamaz.
     * Süper Amatör (Tier 6) -> BAL (Tier 5) veya 3. Lig (Tier 4).
     */
    fun maybeGenerateRealisticOffer(
        state: CareerSaveState,
        player: PlayerProfile,
        currentClub: Club,
        dayIndex: Int,
        randomSeed: Long = System.currentTimeMillis()
    ): List<TransferOffer> {
        val currentOffers = state.transferOffers
        if (currentOffers.size >= 2) return currentOffers

        val eligibility = evaluateTransferEligibility(state, player, currentClub)
        if (!eligibility.isEligible) {
            return currentOffers
        }

        val rng = Random(randomSeed + dayIndex * 47L + player.marketValueEur)

        // Hedef lig kademesi belirleme
        val currentTier = currentClub.tier
        val targetTier = when {
            // U20 ve 8.0+ ortalama ile sansasyonel BAL performansı -> 2. Lig'e sıçrayabilir
            currentTier >= 5 && player.age <= 20 && eligibility.currentRatingAverage >= 7.8 -> currentTier - 2
            // Normal başarılı gelişim -> 1 üst lige sıçrama
            currentTier > 1 -> currentTier - 1
            // Süper Lig'de ise aynı lig veya Avrupa / Büyük kulüp
            else -> 1
        }.coerceIn(1, 6)

        val candidateClubs = WorldDataCatalog.getClubsByTier(targetTier)
            .filter { it.id != currentClub.id }

        if (candidateClubs.isEmpty()) return currentOffers

        val interestedClub = candidateClubs[rng.nextInt(candidateClubs.size)]
        if (currentOffers.any { it.clubId == interestedClub.id }) return currentOffers

        // 1. Kulübün Bonservis Talebi ve Anlaşma Olasılığı
        // Alt lig kulüpleri sözleşmesi süren oyuncularını ucuza bırakmaz
        val contractBuyoutDemand = (player.marketValueEur * LeagueDifficultyCalculator.getProfileForTier(currentTier).contractBuyoutMultiplier).toLong()
        val clubCanAfford = interestedClub.budgetEur >= contractBuyoutDemand / 2

        if (!clubCanAfford && interestedClub.tier < currentTier) {
            // Talip kulübün bütçesi yetersiz, teklif masaya gelmeden iptal olur
            return currentOffers
        }

        // 2. Transfer türü belirleme (Genç oyuncular için kiralık seçeneği yaygındır)
        val transferType = when {
            player.age <= 20 && targetTier <= currentTier - 2 -> "Kiralık (Geri Çağırma Opsiyonlu)"
            player.age <= 22 && rng.nextInt(100) < 30 -> "Kiralık (Satın Alma Opsiyonlu)"
            else -> "Bonservisli Transfer"
        }

        // 3. Maaş ve Prim Skalası
        val targetTierProfile = LeagueDifficultyCalculator.getProfileForTier(targetTier)
        val baseWage = LeagueDifficultyCalculator.computeRealisticStartingWage(targetTier, player.overallRating)
        val offeredWage = (baseWage * (if (eligibility.currentRatingAverage >= 7.8) 1.25 else 1.05)).toLong()
            .coerceIn(targetTierProfile.weeklyWageMinEur, targetTierProfile.weeklyWageMaxEur)

        val promisedRole = when {
            player.overallRating >= interestedClub.teamStrength - 1 -> SquadRole.STARTER
            player.overallRating >= interestedClub.teamStrength - 5 -> SquadRole.ROTATION
            else -> SquadRole.RESERVE
        }

        val contractYears = when {
            transferType.startsWith("Kiralık") -> 1
            player.age <= 21 -> 4
            else -> 2
        }

        val offer = TransferOffer(
            id = "OFFER_${interestedClub.id}_D${dayIndex}_${rng.nextInt(100, 999)}",
            clubId = interestedClub.id,
            clubName = interestedClub.name,
            leagueId = interestedClub.leagueId,
            leagueName = interestedClub.leagueName,
            clubTier = interestedClub.tier,
            clubStrength = interestedClub.teamStrength,
            primaryColorHex = interestedClub.primaryColorHex,
            offeredWeeklyWageEur = offeredWage,
            offeredAppearanceBonusEur = (offeredWage * 0.25).toLong(),
            offeredGoalBonusEur = (offeredWage * 0.35).toLong(),
            contractYears = contractYears,
            promisedRole = promisedRole,
            transferType = transferType,
            expiresDayIndex = dayIndex + 10,
            negotiationCount = 0
        )

        return listOf(offer) + currentOffers
    }

    /**
     * Transfer penceresi kapalıyken veya scout gözlemi yapıldığında X dedikodusu veya scout haberi üretir.
     */
    fun maybeGenerateTransferRumor(
        state: CareerSaveState,
        player: PlayerProfile,
        club: Club,
        dayIndex: Int
    ): List<XPostItem> {
        val window = getTransferWindowStatus(dayIndex)
        val stats = player.currentSeasonStats
        if (stats.appearances < 2 || stats.averageRating < 7.2) {
            return state.xPosts
        }

        val rumorId = "XP_RUMOR_D${dayIndex}_${club.id}"
        if (state.xPosts.any { it.id == rumorId }) return state.xPosts

        val targetTier = (club.tier - 1).coerceAtLeast(1)
        val prospectiveClubs = WorldDataCatalog.getClubsByTier(targetTier)
        if (prospectiveClubs.isEmpty()) return state.xPosts
        val rumorClub = prospectiveClubs[dayIndex % prospectiveClubs.size]

        val rumorPost = if (!window.isOpen) {
            XPostItem(
                id = rumorId,
                dayIndex = dayIndex,
                dateLabel = "Gün $dayIndex",
                authorName = "Transfer Haberleri & Scout",
                handle = "@turkiye_transfer",
                category = "GAZETECİ",
                isVerified = true,
                text = "👀 ÖZEL HABER | ${rumorClub.name} scout ekibi, ${club.leagueName} ekiplerinden ${club.name} forması giyen ${player.fullName}'ı yakın takibe aldı. Ocak transfer penceresinde resmi temas bekleniyor.",
                likes = 840,
                reposts = 120
            )
        } else {
            XPostItem(
                id = rumorId,
                dayIndex = dayIndex,
                dateLabel = "Gün $dayIndex",
                authorName = "Transfer Haberleri & Scout",
                handle = "@turkiye_transfer",
                category = "GAZETECİ",
                isVerified = true,
                text = "🔥 SICAK GELİŞME | ${rumorClub.name}, ${club.name}'nin genç yıldızı ${player.fullName} için nabız yokluyor. Masada bonservis pazarlığı var!",
                likes = 1450,
                reposts = 310
            )
        }

        return listOf(rumorPost) + state.xPosts
    }

    /**
     * Teklif pazarlığı: Oyuncu veya menajer masaya oturduğunda kulübün tepkisi.
     */
    fun negotiateOffer(
        state: CareerSaveState,
        offerId: String
    ): Pair<CareerSaveState, String> {
        val offer = state.transferOffers.find { it.id == offerId }
            ?: return state to "İlgili transfer teklifi bulunamadı."

        if (offer.negotiationCount >= 2) {
            return state to "${offer.clubName} yönetimi son teklifini sunduğunu belirtti, daha fazla pazarlık yapmıyor."
        }

        // %75 ihtimalle kulüp makul artışı kabul eder, %25 ihtimalle masadan kalkabilir
        val rng = Random(state.currentDayIndex * 19L + offer.clubStrength)
        val acceptsWageHike = rng.nextInt(100) < 80

        if (!acceptsWageHike && offer.negotiationCount == 1) {
            // Görüşmeler kilitlendi
            return state to "${offer.clubName} sportif direktörü: 'Talepleriniz bütçemizi aşıyor. İlk teklifimiz geçerlidir, aksi halde alternatiflere yöneleceğiz.'"
        }

        val tierProfile = LeagueDifficultyCalculator.getProfileForTier(offer.clubTier)
        val boostedWage = (offer.offeredWeeklyWageEur * 1.12).toLong().coerceAtMost(tierProfile.weeklyWageMaxEur)
        val boostedGoalBonus = (offer.offeredGoalBonusEur * 1.15).toLong()
        val updatedOffer = offer.copy(
            offeredWeeklyWageEur = boostedWage,
            offeredGoalBonusEur = boostedGoalBonus,
            negotiationCount = offer.negotiationCount + 1
        )

        val updatedOffers = state.transferOffers.map { if (it.id == offerId) updatedOffer else it }
        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            transferOffers = updatedOffers
        ) to "${offer.clubName} ile yapılan müzakereler olumlu sonuçlandı! Yeni teklif: Haftalık ${boostedWage} € + Gol Primi ${boostedGoalBonus} €."
    }
}
