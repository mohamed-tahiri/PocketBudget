package com.example.pocketbudget.util

data class BudgetAlertStatus(
    val percentage: Int,
    val isWarning: Boolean,
    val isExceeded: Boolean
)

object BudgetAlertCalculator {
    fun calculate(spent: Double, limit: Double, thresholdPercent: Int): BudgetAlertStatus {
        if (limit <= 0.0) return BudgetAlertStatus(0, false, false)
        val percentage = ((spent / limit) * 100).toInt()
        val isExceeded = spent > limit
        val isWarning = percentage >= thresholdPercent
        return BudgetAlertStatus(percentage, isWarning, isExceeded)
    }
}
