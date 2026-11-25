package com.example.pocketbudget.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketbudget.data.local.PocketBudgetDatabase
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.data.repository.CategoryRepository
import com.example.pocketbudget.util.InputValidator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryListViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PocketBudgetDatabase.getDatabase(application)
    private val categoryRepository = CategoryRepository(database.categoryDao())

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveCategory(id: Long, name: String, type: String, iconName: String, colorHex: String) {
        val check = InputValidator.validateCategory(name, type, colorHex)
        if (!check.isValid) return

        viewModelScope.launch {
            if (id == 0L) {
                categoryRepository.insertCategory(
                    CategoryEntity(
                        name = name.trim(),
                        type = type,
                        iconName = iconName,
                        colorHex = colorHex.trim()
                    )
                )
            } else {
                val existing = categoryRepository.getCategoryById(id)
                if (existing != null) {
                    categoryRepository.updateCategory(
                        existing.copy(
                            name = name.trim(),
                            type = type,
                            iconName = iconName,
                            colorHex = colorHex.trim()
                        )
                    )
                }
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }
}
