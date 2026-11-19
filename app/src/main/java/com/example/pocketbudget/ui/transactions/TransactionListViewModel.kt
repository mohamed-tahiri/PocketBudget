package com.example.pocketbudget.ui.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.pocketbudget.data.datastore.UserPreferencesManager
import com.example.pocketbudget.data.local.PocketBudgetDatabase
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.data.local.entity.TransactionEntity
import com.example.pocketbudget.data.local.relation.TransactionWithCategory
import com.example.pocketbudget.data.repository.CategoryRepository
import com.example.pocketbudget.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionListViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PocketBudgetDatabase.getDatabase(application)
    private val transactionRepository = TransactionRepository(database.transactionDao())
    private val categoryRepository = CategoryRepository(database.categoryDao())
    private val preferencesManager = UserPreferencesManager(application)

    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow("")
    val filterCategoryId = MutableStateFlow(-1L)
    val sortOrder = MutableStateFlow("DATE_DESC")

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currency: StateFlow<String> = preferencesManager.currencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "€")

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: Flow<PagingData<TransactionWithCategory>> = combine(
        searchQuery,
        filterType,
        filterCategoryId,
        sortOrder
    ) { query, type, catId, sort ->
        Quadruple(query, type, catId, sort)
    }.flatMapLatest { params ->
        transactionRepository.getFilteredTransactions(
            query = params.first,
            type = params.second,
            categoryId = params.third,
            sortOrder = params.fourth
        )
    }.cachedIn(viewModelScope)

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setFilterType(type: String) {
        filterType.value = type
    }

    fun setFilterCategory(catId: Long) {
        filterCategoryId.value = catId
    }

    fun setSortOrder(sort: String) {
        sortOrder.value = sort
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
