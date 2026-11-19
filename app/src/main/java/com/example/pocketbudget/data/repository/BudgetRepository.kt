package com.example.pocketbudget.data.repository

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

        return combine(budgetsFlow, categoriesFlow) { budgets, categories ->
            val categoryMap = categories.associateBy { it.id }
            budgets.mapNotNull { budget ->
                val category = categoryMap[budget.categoryId]
                if (category != null) {
                    val spent = transactionDao.getTotalSpentForCategory(category.id) ?: 0.0
                    BudgetWithCategoryAndSpent(
                        budget = budget,
                        category = category,
                        spentAmount = spent
                    )
                } else null
            }
        }
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
}
