package com.example.pocketbudget.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.pocketbudget.data.local.entity.TransactionEntity
import com.example.pocketbudget.data.local.relation.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Transaction
    @Query("""
        SELECT t.* FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE (:query = '' OR t.title LIKE '%' || :query || '%' OR c.name LIKE '%' || :query || '%')
          AND (:type = '' OR t.type = :type)
          AND (:categoryId = -1 OR t.categoryId = :categoryId)
        ORDER BY 
            CASE WHEN :sortOrder = 'DATE_DESC' THEN t.date END DESC,
            CASE WHEN :sortOrder = 'DATE_ASC' THEN t.date END ASC,
            CASE WHEN :sortOrder = 'AMOUNT_DESC' THEN t.amount END DESC,
            CASE WHEN :sortOrder = 'AMOUNT_ASC' THEN t.amount END ASC,
            t.date DESC
    """)
    fun getFilteredTransactions(
        query: String,
        type: String,
        categoryId: Long,
        sortOrder: String
    ): PagingSource<Int, TransactionWithCategory>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionWithCategoryById(id: Long): TransactionWithCategory?

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactionsIfAbsent(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncomeFlow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpenseFlow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE categoryId = :categoryId AND type = 'EXPENSE'")
    suspend fun getTotalSpentForCategory(categoryId: Long): Double?

    @Query("SELECT categoryId, COALESCE(SUM(amount), 0.0) AS totalSpent FROM transactions WHERE type = 'EXPENSE' GROUP BY categoryId")
    fun getCategoryExpenseTotalsFlow(): Flow<List<CategorySpentTotal>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int
}

data class CategorySpentTotal(
    val categoryId: Long,
    val totalSpent: Double
)
