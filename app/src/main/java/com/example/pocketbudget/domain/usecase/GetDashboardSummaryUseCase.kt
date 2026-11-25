
package com.example.pocketbudget.domain.usecase

import com.example.pocketbudget.data.local.relation.BudgetWithCategoryAndSpent
import com.example.pocketbudget.util.BudgetAlertCalculator

data class DashboardSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val warningBudgets: List<BudgetWithCategoryAndSpent>
)

class GetDashboardSummaryUseCase {

    operator fun invoke(
        income: Double?,
        expense: Double?,
        budgets: List<BudgetWithCategoryAndSpent>,
        threshold: Int
    ): DashboardSummary {

        val totalIncome = income ?: 0.0
        val totalExpense = expense ?: 0.0

        val warningBudgets = budgets.filter { budget ->
            BudgetAlertCalculator.calculate(
                budget.spentAmount,
                budget.budget.monthlyLimit,
                threshold
            ).isWarning
        }

        return DashboardSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense,
            warningBudgets = warningBudgets
        )
    }
}
