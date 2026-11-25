package com.example.pocketbudget.util

data class BudgetAlertStatus(
    val percentage: Int,
    val isWarning: Boolean,
    val isExceeded: Boolean
)

object BudgetAlertCalculator {
    fun calculate(spent: Double, limit: Double, thresholdPercent: Int): BudgetAlertStatus {
        if (!spent.isFinite() || !limit.isFinite() || limit <= 0.0 || spent <= 0.0) {
            return BudgetAlertStatus(0, false, false)
        }

        val rawPercentage = (spent / limit) * 100
        val percentage = if (rawPercentage.isFinite()) rawPercentage.toInt() else Int.MAX_VALUE
        val normalizedThreshold = thresholdPercent.coerceIn(0, 100)
        val isExceeded = spent > limit
        val isWarning = percentage >= normalizedThreshold
        return BudgetAlertStatus(percentage, isWarning, isExceeded)
    }
}
