package com.example.pocketbudget.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.pocketbudget.data.local.dao.TransactionDao
import com.example.pocketbudget.data.local.entity.TransactionEntity
import com.example.pocketbudget.data.local.relation.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    fun getFilteredTransactions(
        query: String,
        type: String,
        categoryId: Long,
        sortOrder: String
    ): Flow<PagingData<TransactionWithCategory>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                maxSize = 200
            ),
            pagingSourceFactory = {
                transactionDao.getFilteredTransactions(query, type, categoryId, sortOrder)
            }
        ).flow
    }

    val totalIncomeFlow: Flow<Double?> = transactionDao.getTotalIncomeFlow()
    val totalExpenseFlow: Flow<Double?> = transactionDao.getTotalExpenseFlow()

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun getTransactionWithCategoryById(id: Long): TransactionWithCategory? {
        return transactionDao.getTransactionWithCategoryById(id)
    }
}
