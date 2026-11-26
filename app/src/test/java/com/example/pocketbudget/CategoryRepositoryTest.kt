
package com.example.pocketbudget

import com.example.pocketbudget.data.local.dao.CategoryDao
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.data.repository.CategoryRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CategoryRepositoryTest {

    private lateinit var dao: CategoryDao
    private lateinit var repository: CategoryRepository

    private val categoriesFlow =
        MutableStateFlow<List<CategoryEntity>>(emptyList())

    private val category = CategoryEntity(
        id = 1L,
        name = "Food",
        type = "EXPENSE",
        iconName = "ic_food",
        colorHex = "#E53935"
    )

    @Before
    fun setup() {
        dao = mock<CategoryDao>()

        whenever(dao.getAllCategories())
            .thenReturn(categoriesFlow)

        repository = CategoryRepository(dao)
    }

    @Test
    fun insertCategoryReturnsGeneratedId() = runTest {
        val newCategory = category.copy(id = 0L)

        whenever(dao.insertCategory(newCategory))
            .thenReturn(1L)

        val result = repository.insertCategory(newCategory)

        assertEquals(1L, result)
        verify(dao).insertCategory(newCategory)
    }

    @Test
    fun getCategoryByIdReturnsCorrectCategory() = runTest {
        whenever(dao.getCategoryById(1L))
            .thenReturn(category)

        val result = repository.getCategoryById(1L)

        assertEquals(category, result)
        verify(dao).getCategoryById(1L)
    }

    @Test
    fun getCategoryByIdReturnsNullWhenNotFound() = runTest {
        whenever(dao.getCategoryById(999L))
            .thenReturn(null)

        val result = repository.getCategoryById(999L)

        assertNull(result)
        verify(dao).getCategoryById(999L)
    }

    @Test
    fun updateCategoryDelegatesToDao() = runTest {
        val updatedCategory = category.copy(
            name = "Groceries"
        )

        repository.updateCategory(updatedCategory)

        verify(dao).updateCategory(updatedCategory)
    }

    @Test
    fun deleteCategoryDelegatesToDao() = runTest {
        repository.deleteCategory(category)

        verify(dao).deleteCategory(category)
    }

    @Test
    fun getCategoriesByTypeReturnsExpenseCategories() = runTest {
        whenever(dao.getCategoriesByType("EXPENSE"))
            .thenReturn(flowOf(listOf(category)))

        val result = repository
            .getCategoriesByType("EXPENSE")
            .first()

        assertEquals(listOf(category), result)
        verify(dao).getCategoriesByType("EXPENSE")
    }

    @Test
    fun allCategoriesEmitsReactiveUpdates() = runTest {
        val emissions = async(
            start = CoroutineStart.UNDISPATCHED
        ) {
            repository.allCategories.take(2).toList()
        }

        categoriesFlow.value = listOf(category)

        assertEquals(
            listOf(
                emptyList<CategoryEntity>(),
                listOf(category)
            ),
            emissions.await()
        )
    }
}
