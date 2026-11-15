package com.example.engine

import com.example.model.*

object TransferEngine {
    fun process(state: CareerSaveState, offer: TransferOffer): TransferProcess = state.livingWorld.transfers.find { it.offerId == offer.id }
        ?: TransferProcess(offer.id, terms = ContractTerms(offer.offeredWeeklyWageEur, offer.contractYears,
            offer.offeredWeeklyWageEur * 2, offer.offeredAppearanceBonusEur, offer.offeredGoalBonusEur, offer.promisedRole),
            feeEur = if (offer.transferType.contains("Kiralık")) state.player.marketValueEur / 10 else state.player.marketValueEur)

    fun club(state: CareerSaveState, offer: TransferOffer): Club? =
        state.clubs.find { it.id == offer.clubId } ?: WorldDataCatalog.clubsForLeague(offer.leagueId).find { it.id == offer.clubId }

    fun affordable(budget: Long, fee: Long, terms: ContractTerms): Boolean {
        if (terms.weeklyWageEur !in 1..5_000_000L || terms.years !in 1..5 || terms.signingBonusEur !in 0..500_000_000L ||
            terms.appearanceBonusEur !in 0..5_000_000L || terms.goalBonusEur !in 0..5_000_000L ||
            terms.preferredNumber?.let { it !in 1..99 } == true || terms.releaseClauseEur?.let { it < fee || it > 5_000_000_000L } == true) return false
        val yearlyCost = terms.weeklyWageEur * 52 + terms.signingBonusEur + 45 * (terms.appearanceBonusEur + terms.goalBonusEur)
        return fee >= 0 && fee + yearlyCost <= budget
    }

    private fun update(state: CareerSaveState, process: TransferProcess): CareerSaveState = state.copy(
        transferOffers = state.transferOffers.map { offer ->
            if (offer.id != process.offerId) offer else offer.copy(
                offeredWeeklyWageEur = process.terms.weeklyWageEur,
                offeredAppearanceBonusEur = process.terms.appearanceBonusEur,
                offeredGoalBonusEur = process.terms.goalBonusEur,
                contractYears = process.terms.years, promisedRole = process.terms.role,
                negotiationCount = process.rounds.size)
        },
        livingWorld = state.livingWorld.copy(transfers = state.livingWorld.transfers.filterNot { it.offerId == process.offerId } + process)
    )

    fun negotiate(state: CareerSaveState, offerId: String, requested: ContractTerms): Pair<CareerSaveState, String> {
        val offer = state.transferOffers.find { it.id == offerId } ?: return state to "Teklif bulunamadı."
        if (state.currentDayIndex > offer.expiresDayIndex) return state to "Teklifin süresi doldu."
        val current = process(state, offer)
        val initial = current.initialTerms ?: current.terms
        if (current.stage !in setOf(TransferStage.FORMAL_OFFER, TransferStage.NEGOTIATION)) return state to "Bu aşamada pazarlık yapılamaz."
        if (current.rounds.size >= 3) return state to "Kulüp son teklifini sundu. Kabul edebilir veya reddedebilirsin."
        val target = club(state, offer) ?: return state to "Hedef kulüp bulunamadı."
        if (!affordable(Long.MAX_VALUE, 0, requested)) return state to "Sözleşme alanları geçerli aralıkta değil."
        val leverage = ((state.player.currentSeasonStats.averageRating - 6.5) * .04 + state.player.potential * .001).coerceIn(0.02, .15)
        val wageCeiling = (initial.weeklyWageEur * (1.0 + leverage)).toLong()
        val tooHigh = requested.weeklyWageEur > wageCeiling * 2 || requested.signingBonusEur > initial.signingBonusEur * 4 + 1
        val decision: String
        val counter: ContractTerms?
        val stage: TransferStage
        when {
            tooHigh || (!affordable(target.budgetEur, current.feeEur, current.terms)) -> {
                decision = "Kulüp finansal sınırları nedeniyle görüşmeden çekildi."; counter = null; stage = TransferStage.NEGOTIATION_FAILED
            }
            affordable(target.budgetEur, current.feeEur, requested) && requested.weeklyWageEur <= wageCeiling && requested.role.minTrustRequired <= current.terms.role.minTrustRequired -> {
                decision = "İstenen şartlar kabul edildi. Anlaşmayı onaylaman bekleniyor."; counter = requested; stage = TransferStage.NEGOTIATION
            }
            else -> {
                counter = current.terms.copy(weeklyWageEur = minOf(requested.weeklyWageEur, wageCeiling), years = requested.years.coerceIn(2, 4))
                    .takeIf { affordable(target.budgetEur, current.feeEur, it) } ?: current.terms
                decision = "Kulübün karşı teklifi: haftalık ${counter.weeklyWageEur} €, ${counter.years} yıl. Rol: ${counter.role.labelTr}."; stage = TransferStage.NEGOTIATION
            }
        }
        val next = current.copy(stage = stage, terms = counter ?: current.terms, initialTerms = initial,
            rounds = current.rounds + NegotiationRound(state.currentDayIndex, requested, counter, decision), outcome = decision)
        // Keep the public offer in sync; the immutable first offer still sets negotiation limits.
        return update(state, next) to decision
    }

    fun agree(state: CareerSaveState, offerId: String): Pair<CareerSaveState, String> {
        val offer = state.transferOffers.find { it.id == offerId } ?: return state to "Teklif artık mevcut değil."
        if (state.currentDayIndex > offer.expiresDayIndex) return state to "Teklif süresi doldu."
        val process = process(state, offer)
        if (process.stage !in setOf(TransferStage.FORMAL_OFFER, TransferStage.NEGOTIATION, TransferStage.REGISTRATION_BLOCKED)) return state to "Anlaşma bu aşamada onaylanamaz."
        val club = club(state, offer) ?: return state to "Kulüp bulunamadı."
        if (!affordable(club.budgetEur, process.feeEur, process.terms)) return state to "Kulüp bütçesi bu anlaşmayı karşılamıyor."
        val next = update(state, process.copy(stage = TransferStage.MEDICAL, outcome = "Şartlarda anlaşma sağlandı; sağlık kontrolü bekleniyor."))
        return addStageTask(next, offer, "MEDICAL", SceneType.MEDICAL, "Transfer sağlık kontrolüne katıl", "NPC_DOCTOR") to "Anlaşma sağlandı. Kulüp değişikliği için sağlık kontrolü ve tescil gerekiyor."
    }

    private fun addStageTask(state: CareerSaveState, offer: TransferOffer, suffix: String, type: SceneType, title: String, npc: String): CareerSaveState {
        val id = "TASK:TRANSFER:$suffix:${offer.id}"
        if (state.livingWorld.tasks.any { it.id == id }) return state
        val task = WorldTask(id, "TRANSFER:${offer.id}:$suffix", title, state.currentDayIndex, "TRANSFER_$suffix", sceneType = type, npcId = npc, entityId = offer.id)
        return state.copy(
            calendarEvents = state.calendarEvents + CalendarEvent("CAL:$id", state.currentDayIndex, GameClock.full(state.currentDayIndex, state.currentSeasonYear), state.formattedClockTime, type.environment, "TRANSFER", title, offer.clubName),
            livingWorld = state.livingWorld.copy(tasks = state.livingWorld.tasks + task,
                notifications = state.livingWorld.notifications + WorldNotification("NOTICE:$id", task.sourceEventId, state.currentDayIndex, title, id))
        )
    }

    fun medical(state: CareerSaveState, offerId: String): Pair<CareerSaveState, String> {
        val offer = state.transferOffers.find { it.id == offerId } ?: return state to "Teklif bulunamadı."
        val process = process(state, offer)
        if (process.stage != TransferStage.MEDICAL) return state to "Sağlık kontrolü aşamasında değilsin."
        if (state.player.injuryDaysRemaining > 0) return state to "Aktif sakatlık nedeniyle sağlık onayı beklemede. Rehabilitasyondan sonra tekrar gel."
        val next = update(state, process.copy(stage = TransferStage.CONTRACT_SIGNING, medicalPassed = true))
        return addStageTask(next, offer, "SIGNING", SceneType.SIGNING, "Sözleşme ve tescil onayı", "NPC_AGENT") to "Transfer sağlık kontrolü tamamlandı. Sözleşme imzasına geçebilirsin."
    }

    fun register(state: CareerSaveState, offerId: String): Pair<CareerSaveState, String> {
        val offer = state.transferOffers.find { it.id == offerId } ?: return state to "Teklif artık geçerli değil."
        val process = process(state, offer)
        if (!process.medicalPassed || process.stage !in setOf(TransferStage.CONTRACT_SIGNING, TransferStage.REGISTRATION_BLOCKED)) return state to "Sağlık ve sözleşme aşamaları tamamlanmadı."
        if (state.player.injuryDaysRemaining > 0) return state to "Yeni sakatlık nedeniyle sağlık onayı yeniden gerekli."
        if (!RegistrationEngine.isOpen(state, offer.leagueId)) return update(state, process.copy(stage = TransferStage.REGISTRATION_BLOCKED)) to "Hedef ligin tescil dönemi kapalı. Anlaşma korundu; dönem açılınca tekrar onayla."
        val registered = update(state, process.copy(stage = TransferStage.REGISTRATION))
        return CareerEngine.acceptTransferOffer(registered, offer)
    }

    fun reject(state: CareerSaveState, offerId: String): CareerSaveState {
        val offer = state.transferOffers.find { it.id == offerId } ?: return state
        val next = update(state, process(state, offer).copy(stage = TransferStage.REJECTED, outcome = "Oyuncu teklifi reddetti."))
        return next.copy(transferOffers = next.transferOffers.filterNot { it.id == offerId })
    }
}
