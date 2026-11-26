package com.example.pocketbudget.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pocketbudget.data.local.dao.BudgetDao
import com.example.pocketbudget.data.local.dao.CategoryDao
import com.example.pocketbudget.data.local.dao.TransactionDao
import com.example.pocketbudget.data.local.entity.BudgetEntity
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.data.local.entity.TransactionEntity

@Database(
    entities = [CategoryEntity::class, BudgetEntity::class, TransactionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PocketBudgetDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: PocketBudgetDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP INDEX IF EXISTS `index_budgets_categoryId`")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_budgets_categoryId_monthYear` ON `budgets` (`categoryId`, `monthYear`)"
                )
            }
        }

        fun getDatabase(context: Context): PocketBudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PocketBudgetDatabase::class.java,
                    "pocket_budget_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
