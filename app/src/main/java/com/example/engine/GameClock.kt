package com.example.engine

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object GameClock {
    private val locale = Locale.forLanguageTag("tr-TR")
    fun start(year: Int = 2026): LocalDate = LocalDate.of(year, 8, 10)
    fun date(dayIndex: Int, year: Int = 2026): LocalDate = start(year).plusDays((dayIndex - 1).coerceAtLeast(0).toLong())
    fun dayIndex(date: LocalDate, year: Int = 2026): Int = ChronoUnit.DAYS.between(start(year), date).toInt() + 1
    fun full(dayIndex: Int, year: Int = 2026): String = date(dayIndex, year).format(DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", locale))
    fun short(dayIndex: Int, year: Int = 2026): String = date(dayIndex, year).format(DateTimeFormatter.ofPattern("d MMM", locale))
    fun month(offset: Int, year: Int): YearMonth = YearMonth.of(year, 8).plusMonths(offset.toLong())
    fun monthTitle(offset: Int, year: Int): String = month(offset, year).format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
}
