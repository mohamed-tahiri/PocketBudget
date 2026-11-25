package com.example.pocketbudget.util

import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double, currency: String): String {
        val formattedAmount = if (amount.isFinite()) amount else 0.0
        return String.format(Locale.US, "%.2f %s", formattedAmount, currency)
    }
}
