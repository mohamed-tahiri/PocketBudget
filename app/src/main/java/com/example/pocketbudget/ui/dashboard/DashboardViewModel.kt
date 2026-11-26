package com.example.pocketbudget.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketbudget.data.datastore.UserPreferencesManager
import com.example.pocketbudget.data.local.PocketBudgetDatabase
import com.example.pocketbudget.data.local.relation.BudgetWithCategoryAndSpent
import com.example.pocketbudget.data.repository.BudgetRepository
import com.example.pocketbudget.data.repository.TransactionRepository
import com.example.pocketbudget.domain.usecase.GetDashboardSummaryUseCase
import com.example.pocketbudget.util.AppConstants
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val currency: String = "€",
    val alertThreshold: Int = 80,
    val warningBudgets: List<BudgetWithCategoryAndSpent> = emptyList(),
    val userName: String = "Étudiant / Développeur Senior",
    val userEmail: String = "pocketbudget.pro@univ.fr"
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PocketBudgetDatabase.getDatabase(application)
    private val transactionRepository = TransactionRepository(database.transactionDao())
    private val budgetRepository = BudgetRepository(database.budgetDao(), database.transactionDao(), database.categoryDao())
    private val preferencesManager = UserPreferencesManager(application)
    private val getDashboardSummary = GetDashboardSummaryUseCase()

    val uiState: StateFlow<DashboardUiState> = combine(
        combine(
            transactionRepository.totalIncomeFlow,
            transactionRepository.totalExpenseFlow,
            preferencesManager.currencyFlow,
            preferencesManager.budgetAlertThresholdFlow
        ) { income, expense, currency, threshold ->
            Quadruple(income, expense, currency, threshold)
        },
        combine(
            preferencesManager.userNameFlow,
            preferencesManager.userEmailFlow,
            budgetRepository.getBudgetsWithSpentForMonth(AppConstants.DEFAULT_BUDGET_MONTH)
        ) { name, email, budgets ->
            Triple(name, email, budgets)
        }
    ) { p1, p2 ->
        val income = p1.first
        val expense = p1.second
        val currency = p1.third
        val threshold = p1.fourth

        val userName = p2.first
        val userEmail = p2.second
        val budgets = p2.third

        val summary = getDashboardSummary(
            income,
            expense,
            budgets,
            threshold
        )

        DashboardUiState(
            totalIncome = summary.totalIncome,
            totalExpense = summary.totalExpense,
            balance = summary.balance,
            currency = currency,
            alertThreshold = threshold,
            warningBudgets = summary.warningBudgets,
            userName = userName,
            userEmail = userEmail
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
