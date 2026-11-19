package com.example.pocketbudget.data.json

import android.content.Context
import com.example.pocketbudget.data.json.model.InitialDataDto
import com.example.pocketbudget.data.local.PocketBudgetDatabase
import com.example.pocketbudget.data.local.entity.BudgetEntity
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.data.local.entity.TransactionEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

object InitialDataInitializer {

    suspend fun initializeIfNeeded(context: Context, database: PocketBudgetDatabase, force: Boolean = false) {
        withContext(Dispatchers.IO) {
            val categoryCount = database.categoryDao().getCategoryCount()
            if (categoryCount == 0 || force) {
                try {
                    val inputStream = context.assets.open("initial_data.json")
                    val reader = InputStreamReader(inputStream)
                    val initialData = Gson().fromJson(reader, InitialDataDto::class.java)
                    reader.close()

                    if (initialData != null) {
                        val categories = initialData.categories.map {
                            CategoryEntity(
                                id = it.id,
                                name = it.name,
                                type = it.type,
                                iconName = it.iconName,
                                colorHex = it.colorHex
                            )
                        }

                        val budgets = initialData.budgets.map {
                            BudgetEntity(
                                id = it.id,
                                categoryId = it.categoryId,
                                monthlyLimit = it.monthlyLimit,
                                monthYear = it.monthYear
                            )
                        }

                        val transactions = initialData.transactions.map {
                            TransactionEntity(
                                id = it.id,
                                title = it.title,
                                amount = it.amount,
                                type = it.type,
                                categoryId = it.categoryId,
                                date = it.date
                            )
                        }

                        database.categoryDao().insertCategories(categories)
                        database.budgetDao().insertBudgets(budgets)
                        database.transactionDao().insertTransactions(transactions)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
