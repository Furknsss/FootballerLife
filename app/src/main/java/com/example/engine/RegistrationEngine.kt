package com.example.engine

import com.example.model.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object RegistrationEngine {
    fun rules(state: CareerSaveState, leagueId: String): LeagueRegistrationRules {
        val date = GameClock.date(state.currentDayIndex, state.currentSeasonYear)
        val year = if (date.monthValue < 7) date.year - 1 else date.year
        return state.livingWorld.registrationRules.firstOrNull { it.leagueId == leagueId && it.seasonYear == year }
            ?: configuredRules(leagueId, year)
    }

    fun configuredRules(leagueId: String, year: Int): LeagueRegistrationRules {
        // Explicit game settings, independently overridable for every league and season.
        val lateSummer = leagueId.startsWith("TR_")
        val windows = listOf(
            RegistrationWindow("$year-07-01", if (lateSummer) "$year-09-12" else "$year-08-31"),
            RegistrationWindow("${year + 1}-01-01", if (lateSummer) "${year + 1}-02-06" else "${year + 1}-01-31")
        )
        return LeagueRegistrationRules(leagueId, year, windows)
    }

    fun isOpen(state: CareerSaveState, leagueId: String): Boolean {
        val today = GameClock.date(state.currentDayIndex, state.currentSeasonYear)
        return rules(state, leagueId).windows.any {
            val start = LocalDate.parse(it.start)
            val end = LocalDate.parse(it.end)
            !today.isBefore(start) && !today.isAfter(end)
        }
    }

    fun daysToBoundary(state: CareerSaveState, leagueId: String): Int {
        val today = GameClock.date(state.currentDayIndex, state.currentSeasonYear)
        val current = rules(state, leagueId)
        val dates = if (isOpen(state, leagueId)) current.windows.map { LocalDate.parse(it.end).plusDays(1) }
            else (current.windows + configuredRules(leagueId, current.seasonYear + 1).windows).map { LocalDate.parse(it.start) }
        return dates.filter { it.isAfter(today) }.minOrNull()?.let { ChronoUnit.DAYS.between(today, it).toInt() } ?: 0
    }
}
