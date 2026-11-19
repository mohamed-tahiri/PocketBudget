package com.example.pocketbudget.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pocketbudget.data.local.dao.BudgetDao
import com.example.pocketbudget.data.local.dao.CategoryDao
import com.example.pocketbudget.data.local.dao.TransactionDao
import com.example.pocketbudget.data.local.entity.BudgetEntity
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.data.local.entity.TransactionEntity

@Database(
    entities = [CategoryEntity::class, BudgetEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PocketBudgetDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: PocketBudgetDatabase? = null

        fun getDatabase(context: Context): PocketBudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PocketBudgetDatabase::class.java,
                    "pocket_budget_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
