package com.example.pocketbudget.data.json

import com.example.pocketbudget.data.json.model.InitialDataDto
import com.example.pocketbudget.data.local.entity.BudgetEntity
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.data.local.entity.TransactionEntity
import com.example.pocketbudget.util.InputValidator

data class ValidatedInitialData(
    val categories: List<CategoryEntity>,
    val budgets: List<BudgetEntity>,
    val transactions: List<TransactionEntity>,
    val skippedCount: Int
)

/**
 * Validates JSON seed records before database insertion to prevent corrupt records,
 * broken foreign keys, or duplicates.
 */
object InitialDataValidator {

    fun validate(dto: InitialDataDto?): ValidatedInitialData {
        if (dto == null) {
            return ValidatedInitialData(emptyList(), emptyList(), emptyList(), 0)
        }

        var skipped = 0
        val seenCategoryIds = mutableSetOf<Long>()
        val validCategories = mutableListOf<CategoryEntity>()
        val categoryTypes = mutableMapOf<Long, String>()

        for (cat in dto.categories.orEmpty()) {
            val check = InputValidator.validateCategory(cat.name, cat.type, cat.colorHex)
            if (cat.id > 0L && check.isValid && cat.iconName.isNotBlank() && seenCategoryIds.add(cat.id)) {
                validCategories.add(
                    CategoryEntity(
                        id = cat.id,
                        name = cat.name.trim(),
                        type = cat.type,
                        iconName = cat.iconName.trim(),
                        colorHex = cat.colorHex.trim()
                    )
                )
                categoryTypes[cat.id] = cat.type
            } else {
                skipped++
            }
        }

        val seenBudgetIds = mutableSetOf<Long>()
        val seenBudgetCategories = mutableSetOf<Long>()
        val validBudgets = mutableListOf<BudgetEntity>()

        for (budget in dto.budgets.orEmpty()) {
            val check = InputValidator.validateBudget(budget.categoryId, budget.monthlyLimit, budget.monthYear)
            val categoryType = categoryTypes[budget.categoryId]
            if (budget.id > 0L &&
                check.isValid &&
                categoryType == "EXPENSE" &&
                seenBudgetIds.add(budget.id) &&
                seenBudgetCategories.add(budget.categoryId)
            ) {
                validBudgets.add(
                    BudgetEntity(
                        id = budget.id,
                        categoryId = budget.categoryId,
                        monthlyLimit = budget.monthlyLimit,
                        monthYear = budget.monthYear.trim()
                    )
                )
            } else {
                skipped++
            }
        }

        val seenTransactionIds = mutableSetOf<Long>()
        val validTransactions = mutableListOf<TransactionEntity>()

        for (tx in dto.transactions.orEmpty()) {
            val check = InputValidator.validateTransaction(tx.title, tx.amount, tx.type, tx.categoryId)
            val categoryExists = tx.categoryId in seenCategoryIds
            if (tx.id > 0L &&
                check.isValid &&
                categoryExists &&
                tx.date > 0L &&
                seenTransactionIds.add(tx.id)
            ) {
                validTransactions.add(
                    TransactionEntity(
                        id = tx.id,
                        title = tx.title.trim(),
                        amount = tx.amount,
                        type = tx.type,
                        categoryId = tx.categoryId,
                        date = tx.date
                    )
                )
            } else {
                skipped++
            }
        }

        return ValidatedInitialData(
            categories = validCategories,
            budgets = validBudgets,
            transactions = validTransactions,
            skippedCount = skipped
        )
    }
}
