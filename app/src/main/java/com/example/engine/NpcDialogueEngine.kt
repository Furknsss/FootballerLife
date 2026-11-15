package com.example.engine

import com.example.model.*
import java.util.Locale

data class DialogueReply(val dialogue: String, val emotion: String = "Sakin", val tone: String = "Doğal", val intent: String = "CONVERSATION", val proposedAction: String? = null, val targetEntityId: String? = null, val memoryCandidate: String? = null, val conversationStatus: String = "OPEN")

object NpcDialogueEngine {
    fun relevantMemories(state: CareerSaveState, npcId: String, text: String): List<NpcMemory> {
        val npc = state.npcs.find { it.id == npcId }
        val identity = npc?.let { LivingWorldEngine.identityId(state, it) } ?: "NPC_DOCTOR:${state.contract.clubId}"
        val words = text.lowercase(Locale.forLanguageTag("tr-TR")).split(Regex("\\W+")).filter { it.length > 3 }
        return state.livingWorld.memories.filter { it.npcId == identity && it.dayIndex <= state.currentDayIndex }
            .sortedByDescending { it.importance * 10 + words.count { word -> it.text.contains(word, ignoreCase = true) } * 5 - (state.currentDayIndex - it.dayIndex).coerceAtMost(30) }
            .take(6)
    }

    fun reply(state: CareerSaveState, npcId: String, message: String): DialogueReply {
        val npc = state.npcs.find { it.id == npcId }
        val lower = message.lowercase(Locale.forLanguageTag("tr-TR"))
        val memories = relevantMemories(state, npcId, message)
        val recall = memories.firstOrNull()?.let { " Önceki görüşmemizden hatırlıyorum: ${it.text.take(130)}" }.orEmpty()
        val report = state.lastMatchReport
        val performance = report?.let { "Son maç: ${it.userRating} reyting, ${it.userGoals} gol, ${it.userAssists} asist." } ?: "Henüz değerlendirilecek bir maç raporun yok."
        val answer = when (npcId) {
            "NPC_MANAGER" -> when {
                listOf("süre", "forma", "ilk 11", "kadro").any(lower::contains) -> "Kadro durumun ${state.nextMatchStartingStatus.labelTr}. Güvenim ${state.managerTrust}/100, enerjin ${state.player.energy}/100. $performance Forma kararını antrenman ve maç verilerine göre vereceğim."
                listOf("ağrı", "sakat", "yorgun").any(lower::contains) -> "Sağlığını riske atmayacağız. ${state.player.injuryDaysRemaining} gün iyileşme süren var. Sağlık ekibinin uygun gördüğü programı takip et."
                lower.contains("transfer") -> "Transfer talebini menajerinle değerlendirebilirsin. Takım içindeki sorumluluğun, tescil tamamlanana kadar devam ediyor."
                else -> "${state.player.firstName}, $performance ${if (state.trainedToday) "Bugünkü antrenmanını da rapora aldım." else "Antrenman disiplinini korumanı bekliyorum."} ${if (npc != null && npc.trust < 40) "Güvenimizi yeniden kurmak için istikrar gerekli." else "Önümüzdeki maçta taktik plana bağlı kalmanı istiyorum."}"
            }
            "NPC_AGENT" -> {
                val offer = state.transferOffers.firstOrNull { it.expiresDayIndex >= state.currentDayIndex }
                if (offer == null) "Şu anda geçerli resmî teklif yok. Yaşını, performansını, piyasa değerini ve kulüplerin bütçelerini dikkate alarak uygun fırsatları takip edebiliriz. Maaşın haftalık ${state.contract.weeklyWageEur} €."
                else "${offer.clubName} dosyası açık. Haftalık teklif ${TransferEngine.process(state, offer).terms.weeklyWageEur} €. Pazarlık, sağlık ve hedef ligin tescil kontrolü tamamlanmadan kulüp değişmez."
            }
            "NPC_FAMILY" -> when {
                listOf("sakat", "ağrı", "yor", "stres").any(lower::contains) -> "Önce sağlığın ${state.player.firstName}. Kendini hazır hissetmiyorsan dinlen; yanında olmamız için bir başarı kazanman gerekmiyor."
                lower.contains("transfer") -> "${state.currentClub.city} dışında yaşamak büyük bir karar. Kendini mutlu ve güvende hissedeceğin yeri seç; bizi de planına dahil et."
                else -> "Bugün nasılsın ${state.player.firstName}? Futbol dışında kendine de zaman ayır. ${if (state.player.morale < 60) "Birlikte sakin bir akşam geçirebiliriz." else "Seni dinlemek istiyorum."}"
            }
            "NPC_DOCTOR" -> "${state.livingWorld.medical.diagnosis}. Kalan iyileşme süren ${state.player.injuryDaysRemaining} gün. ${if (state.player.injuryDaysRemaining > 0) "Tam yükte antrenman ve maça dönüş için henüz onay yok." else "Güncel raporda aktif sakatlık görünmüyor."} Bu, oyun içi sağlık simülasyonudur."
            "NPC_PRESS" -> "${state.player.fullName}, $performance ${if (lower.contains("transfer")) "Transfer söylentilerine ilişkin kendi açıklamanı nasıl yapmak istersin?" else "Bugünkü performansını ve takımın rolünü nasıl değerlendiriyorsun?"} Açıklamanı karar ekranında yayımlamadan önce onaylayabilirsin."
            else -> "${state.player.firstName}, $performance ${if (state.teamChemistry >= 60) "Birbirimize destek olduğumuzda sahada daha iyi anlaşıyoruz." else "Takım içindeki iletişimi geliştirmemiz gerek."} ${if (lower.contains("yemek")) "Maç programına uyan bir akşam buluşabiliriz." else "Önümüzdeki maçta birlikte neyi geliştirmek istersin?"}"
        }
        return DialogueReply(answer + recall, emotion = npc?.currentMood ?: "Sakin")
    }

    fun send(input: CareerSaveState, npcId: String, text: String): CareerSaveState {
        val state = LivingWorldEngine.reconcile(input)
        val clean = text.trim().take(2000)
        if (clean.isEmpty()) return state
        val npc = state.npcs.find { it.id == npcId }
        if (npc == null && npcId != "NPC_DOCTOR") return state
        val response = reply(state, npcId, clean)
        val sequence = (npc?.messages?.size ?: state.livingWorld.activeScene?.transcript?.size ?: 0)
        val id = "CHAT:$npcId:${state.currentDayIndex}:${state.currentHour}:$sequence:${clean.hashCode()}"
        val messages = listOf(
            ChatMessage("$id:USER", state.player.fullName, true, clean, state.currentDayIndex, state.formattedClockTime),
            ChatMessage("$id:NPC", npc?.name ?: "Dr. Deniz Aydın", false, response.dialogue, state.currentDayIndex, state.formattedClockTime, response.emotion)
        )
        val scene = state.livingWorld.activeScene
        var next = state.copy(npcs = state.npcs.map { if (it.id == npcId) it.copy(messages = (it.messages + messages).takeLast(200), unreadCount = 0) else it },
            livingWorld = state.livingWorld.copy(activeScene = scene?.copy(transcript = (scene.transcript + messages).takeLast(80))))
        val identity = npc?.let { LivingWorldEngine.identityId(state, it) } ?: "NPC_DOCTOR:${state.contract.clubId}"
        if (listOf("söz", "transfer", "sakat", "aile", "kaptan").any { clean.contains(it, ignoreCase = true) }) {
            next = LivingWorldEngine.remember(next, NpcMemory(id, identity, state.currentDayIndex, "Görüşme", clean, 2, id))
        }
        return next
    }
}
