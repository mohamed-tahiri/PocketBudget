package com.example.pocketbudget.data.json

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.pocketbudget.data.datastore.UserPreferencesManager
import com.example.pocketbudget.data.json.model.InitialDataDto
import com.example.pocketbudget.data.local.PocketBudgetDatabase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

object InitialDataInitializer {

    private const val TAG = "InitialDataInitializer"

    suspend fun initializeIfNeeded(
        context: Context,
        database: PocketBudgetDatabase,
        preferencesManager: UserPreferencesManager = UserPreferencesManager(context),
        force: Boolean = false
    ) {
        withContext(Dispatchers.IO) {
            val alreadyImported = preferencesManager.isInitialDataImported()
            if (alreadyImported && !force) {
                Log.d(TAG, "Initial data already imported; skipping.")
                return@withContext
            }

            val categoryCount = database.categoryDao().getCategoryCount()
            if (categoryCount > 0 && !force) {
                Log.d(TAG, "Database already populated ($categoryCount categories); marking imported.")
                preferencesManager.setInitialDataImported(true)
                return@withContext
            }

            try {
                val initialData = context.assets.open("initial_data.json").use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        Gson().fromJson(reader, InitialDataDto::class.java)
                    }
                }

                val validated = InitialDataValidator.validate(initialData)
                if (validated.skippedCount > 0) {
                    Log.w(TAG, "Skipped ${validated.skippedCount} invalid seed records during JSON validation.")
                }

                database.withTransaction {
                    database.categoryDao().insertCategoriesIfAbsent(validated.categories)
                    database.budgetDao().insertBudgetsIfAbsent(validated.budgets)
                    database.transactionDao().insertTransactionsIfAbsent(validated.transactions)
                }

                preferencesManager.setInitialDataImported(true)
                Log.i(
                    TAG,
                    "Imported ${validated.categories.size} categories, ${validated.budgets.size} budgets, ${validated.transactions.size} transactions."
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize data from initial_data.json", e)
            }
        }
    }
}
