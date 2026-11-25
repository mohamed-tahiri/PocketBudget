package com.example.pocketbudget.ui.budgets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketbudget.data.datastore.UserPreferencesManager
import com.example.pocketbudget.data.local.PocketBudgetDatabase
import com.example.pocketbudget.data.local.entity.BudgetEntity
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.data.local.relation.BudgetWithCategoryAndSpent
import com.example.pocketbudget.data.repository.BudgetRepository
import com.example.pocketbudget.data.repository.CategoryRepository
import com.example.pocketbudget.util.InputValidator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BudgetListUiState(
    val budgets: List<BudgetWithCategoryAndSpent> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val currency: String = "€",
    val alertThreshold: Int = 80
)

class BudgetListViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PocketBudgetDatabase.getDatabase(application)
    private val budgetRepository = BudgetRepository(database.budgetDao(), database.transactionDao(), database.categoryDao())
    private val categoryRepository = CategoryRepository(database.categoryDao())
    private val preferencesManager = UserPreferencesManager(application)

    val uiState: StateFlow<BudgetListUiState> = combine(
        budgetRepository.getBudgetsWithSpentForMonth("2025-05"),
        categoryRepository.getCategoriesByType("EXPENSE"),
        preferencesManager.currencyFlow,
        preferencesManager.budgetAlertThresholdFlow
    ) { budgets, categories, currency, threshold ->
        BudgetListUiState(
            budgets = budgets,
            categories = categories,
            currency = currency,
            alertThreshold = threshold
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetListUiState()
    )

    fun saveBudget(categoryId: Long, monthlyLimit: Double, monthYear: String = "2025-05") {
        val check = InputValidator.validateBudget(categoryId, monthlyLimit, monthYear)
        if (!check.isValid) return

        viewModelScope.launch {
            val existing = budgetRepository.getBudgetForCategoryAndMonth(categoryId, monthYear)
            if (existing != null) {
                budgetRepository.updateBudget(existing.copy(monthlyLimit = monthlyLimit))
            } else {
                budgetRepository.insertBudget(
                    BudgetEntity(
                        categoryId = categoryId,
                        monthlyLimit = monthlyLimit,
                        monthYear = monthYear
                    )
                )
            }
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget)
        }
    }
}
