package com.example.ui.components

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

object FLFormatters {
    private val trLocale = Locale("tr", "TR")
    private val symbols = DecimalFormatSymbols(trLocale).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val integerFormatter = DecimalFormat("#,###", symbols)
    private val oneDecimalFormatter = DecimalFormat("0.0", symbols)
    private val twoDecimalFormatter = DecimalFormat("0.00", symbols)

    /**
     * Never outputs floating point noise like 6.619999999999.
     * Always outputs clean Turkish 1-decimal format like "6,6".
     */
    fun formatRating(rating: Double): String {
        val clamped = ((rating.coerceIn(1.0, 10.0) * 10.0).roundToInt()) / 10.0
        return oneDecimalFormatter.format(clamped)
    }

    fun formatPercent(value: Int): String = "%${value.coerceIn(0, 100)}"

    /**
     * Formats market value as "€1,25 Mn", "€35,0 Mn", or "€125 Bin".
     */
    fun formatMarketValue(eur: Long): String {
        val absVal = abs(eur)
        return when {
            absVal >= 10_000_000L -> "€${oneDecimalFormatter.format(eur / 1_000_000.0)} Mn"
            absVal >= 1_000_000L -> "€${twoDecimalFormatter.format(eur / 1_000_000.0)} Mn"
            absVal >= 1_000L -> "€${integerFormatter.format(eur / 1_000L)} Bin"
            else -> "€${integerFormatter.format(eur)}"
        }
    }

    /**
     * Formats weekly salary as "€64.615 / hafta".
     */
    fun formatWage(weeklyEur: Long): String {
        return "€${integerFormatter.format(weeklyEur)} / hafta"
    }

    /**
     * Formats exact Euro amount as "€142.500".
     */
    fun formatCurrency(eur: Long): String {
        return if (eur < 0) {
            "-€${integerFormatter.format(abs(eur))}"
        } else {
            "€${integerFormatter.format(eur)}"
        }
    }

    fun formatEuroFull(eur: Long): String = formatCurrency(eur)

    fun formatCompactNumber(number: Long): String {
        val absVal = abs(number)
        return when {
            absVal >= 1_000_000L -> "${oneDecimalFormatter.format(number / 1_000_000.0)} Mn"
            absVal >= 1_000L -> "${oneDecimalFormatter.format(number / 1_000.0)} B"
            else -> integerFormatter.format(number)
        }
    }

    fun formatSignedCurrency(eur: Long): String {
        return when {
            eur > 0 -> "+€${integerFormatter.format(eur)}"
            eur < 0 -> "-€${integerFormatter.format(abs(eur))}"
            else -> "€0"
        }
    }
}
