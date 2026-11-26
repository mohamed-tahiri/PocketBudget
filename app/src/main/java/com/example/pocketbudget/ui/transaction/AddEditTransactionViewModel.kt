package com.example.pocketbudget.ui.transaction

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketbudget.data.local.PocketBudgetDatabase
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.data.local.entity.TransactionEntity
import com.example.pocketbudget.data.repository.CategoryRepository
import com.example.pocketbudget.data.repository.TransactionRepository
import com.example.pocketbudget.util.InputValidator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddEditTransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PocketBudgetDatabase.getDatabase(application)
    private val transactionRepository = TransactionRepository(database.transactionDao())
    private val categoryRepository = CategoryRepository(database.categoryDao())

    var loadedTransaction: TransactionEntity? = null
        private set

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getTransactionById(id: Long): TransactionEntity? {
        if (loadedTransaction?.id == id) return loadedTransaction
        return transactionRepository.getTransactionById(id)?.also {
            loadedTransaction = it
        }
    }

    fun saveTransaction(
        id: Long,
        title: String,
        amount: Double?,
        type: String,
        categoryId: Long,
        date: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val check = InputValidator.validateTransaction(title, amount, type, categoryId)
        if (!check.isValid) {
            onError(check.errorMessage ?: "Données invalides")
            return
        }

        val validAmount = amount
            ?: return onError(check.errorMessage ?: "Montant invalide")
        viewModelScope.launch {
            if (id == 0L) {
                val newTx = TransactionEntity(
                    title = title.trim(),
                    amount = validAmount,
                    type = type,
                    categoryId = categoryId,
                    date = date
                )
                val insertedId = transactionRepository.insertTransaction(newTx)
                Log.d(TAG, "Inserted transaction id=$insertedId, amount=$validAmount, categoryId=$categoryId")
            } else {
                val existing = transactionRepository.getTransactionById(id)
                if (existing != null) {
                    val updated = existing.copy(
                        title = title.trim(),
                        amount = validAmount,
                        type = type,
                        categoryId = categoryId,
                        date = existing.date
                    )
                    transactionRepository.updateTransaction(updated)
                    Log.d(TAG, "Updated transaction id=$id, amount=$validAmount, categoryId=$categoryId")
                } else {
                    onError("Transaction introuvable")
                    return@launch
                }
            }
            onSuccess()
        }
    }

    companion object {
        private const val TAG = "AddEditTransactionVM"
    }
}
