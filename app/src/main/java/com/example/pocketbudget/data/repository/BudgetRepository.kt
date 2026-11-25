package com.example.pocketbudget.data.repository

import android.util.Log
import com.example.pocketbudget.data.local.dao.BudgetDao
import com.example.pocketbudget.data.local.dao.CategoryDao
import com.example.pocketbudget.data.local.dao.TransactionDao
import com.example.pocketbudget.data.local.entity.BudgetEntity
import com.example.pocketbudget.data.local.relation.BudgetWithCategoryAndSpent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class BudgetRepository(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {

    fun getBudgetsWithSpentForMonth(monthYear: String): Flow<List<BudgetWithCategoryAndSpent>> {
        val budgetsFlow = budgetDao.getBudgetsForMonth(monthYear)
        val categoriesFlow = categoryDao.getAllCategories()
        val spentTotalsFlow = transactionDao.getCategoryExpenseTotalsFlow()

        return combine(budgetsFlow, categoriesFlow, spentTotalsFlow) { budgets, categories, spentTotals ->
            val categoryMap = categories.associateBy { it.id }
            val spentMap = spentTotals.associateBy({ it.categoryId }, { it.totalSpent })

            val result = budgets.mapNotNull { budget ->
                val category = categoryMap[budget.categoryId]
                if (category != null) {
                    val spent = spentMap[category.id] ?: 0.0
                    BudgetWithCategoryAndSpent(
                        budget = budget,
                        category = category,
                        spentAmount = spent
                    )
                } else null
            }
            Log.d(TAG, "Recalculated ${result.size} budgets for $monthYear")
            result
        }
    }

    suspend fun getBudgetForCategoryAndMonth(categoryId: Long, monthYear: String): BudgetEntity? {
        return budgetDao.getBudgetForCategoryAndMonth(categoryId, monthYear)
    }

    suspend fun insertBudget(budget: BudgetEntity): Long {
        return budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: BudgetEntity) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun getBudgetById(id: Long): BudgetEntity? {
        return budgetDao.getBudgetById(id)
    }

    companion object {
        private const val TAG = "BudgetRepository"
    }
}
