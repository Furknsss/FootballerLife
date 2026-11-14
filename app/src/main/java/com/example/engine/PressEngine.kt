package com.example.engine

import com.example.model.*
import java.util.Locale

object PressEngine {
    fun publish(state: CareerSaveState, message: String): Pair<CareerSaveState, String> {
        val text = message.trim().take(2000)
        if (text.isEmpty()) return state to "Önce açıklamanı yaz."
        val id = "PRESS:${state.currentDayIndex}:${text.hashCode()}"
        if (id in state.livingWorld.appliedEventIds) return state to "Bu açıklama zaten yayımlandı."
        val lower = text.lowercase(Locale.forLanguageTag("tr-TR"))
        // Ambiguous language stays neutral; no invented sentiment or unrelated punishment.
        val supportive = listOf("takım arkadaşlarıma teşekkür", "taraftarımıza teşekkür", "birlikte kazandık").any(lower::contains)
        val fanDelta = if (supportive) 1 else 0
        val news = NewsEvent(id = "NEWS:$id", sourceGameEventId = id, dayIndex = state.currentDayIndex,
            dateLabel = GameClock.full(state.currentDayIndex, state.currentSeasonYear), category = "RÖPORTAJ",
            headline = "${state.player.fullName}: “${text.take(100)}”", body = "Kurgusal kariyer açıklaması: $text",
            relatedClubName = state.currentClub.name, fanReaction = if (supportive) "Destekleyici" else "Tarafsız")
        val post = SocialMediaPost("SOC:$id", state.currentDayIndex, GameClock.short(state.currentDayIndex, state.currentSeasonYear),
            state.player.fullName, "@${state.player.kitName.lowercase()}", true, text, 0, 0,
            if (supportive) "Destekleyici" else "Tarafsız", emptyList())
        val next = state.copy(pressCompletedForLastMatch = true,
            player = state.player.copy(fanSupport = (state.player.fanSupport + fanDelta).coerceAtMost(100)),
            teamChemistry = (state.teamChemistry + fanDelta).coerceAtMost(100),
            socialPosts = listOf(post) + state.socialPosts, newsEvents = listOf(news) + state.newsEvents,
            eventHistory = listOf(GameEvent(id, state.currentDayIndex, 0, "PRESS_STATEMENT", text, "Taraftar desteği +$fanDelta")) + state.eventHistory,
            livingWorld = state.livingWorld.copy(appliedEventIds = state.livingWorld.appliedEventIds + id))
        return LivingWorldEngine.reconcile(next) to "Açıklaman aynen yayımlandı. ${if (supportive) "Takım uyumu ve taraftar desteği +1." else "Tepki henüz tarafsız."}"
    }
}
