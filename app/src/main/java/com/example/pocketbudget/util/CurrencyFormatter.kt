package com.example.pocketbudget.util

import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double, currency: String): String {
        return String.format(Locale.US, "%.2f %s", amount, currency)
    }
}
