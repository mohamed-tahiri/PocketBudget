package com.example.pocketbudget.ui.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketbudget.data.local.PocketBudgetDatabase
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.data.local.entity.TransactionEntity
import com.example.pocketbudget.data.repository.CategoryRepository
import com.example.pocketbudget.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddEditTransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PocketBudgetDatabase.getDatabase(application)
    private val transactionRepository = TransactionRepository(database.transactionDao())
    private val categoryRepository = CategoryRepository(database.categoryDao())

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return transactionRepository.getTransactionById(id)
    }

    fun saveTransaction(
        id: Long,
        title: String,
        amount: Double,
        type: String,
        categoryId: Long,
        date: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (title.isBlank()) {
            onError("Le titre ne peut pas être vide")
            return
        }
        if (amount <= 0.0) {
            onError("Le montant doit être supérieur à 0")
            return
        }
        if (categoryId <= 0) {
            onError("Veuillez sélectionner une catégorie")
            return
        }

        viewModelScope.launch {
            if (id == 0L) {
                val newTx = TransactionEntity(
                    title = title.trim(),
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    date = date
                )
                transactionRepository.insertTransaction(newTx)
            } else {
                val existing = transactionRepository.getTransactionById(id)
                if (existing != null) {
                    val updated = existing.copy(
                        title = title.trim(),
                        amount = amount,
                        type = type,
                        categoryId = categoryId,
                        date = date
                    )
                    transactionRepository.updateTransaction(updated)
                }
            }
            onSuccess()
        }
    }
}
