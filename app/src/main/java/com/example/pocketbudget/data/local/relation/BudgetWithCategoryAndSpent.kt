package com.example.pocketbudget.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.pocketbudget.data.local.entity.BudgetEntity
import com.example.pocketbudget.data.local.entity.CategoryEntity

data class BudgetWithCategoryAndSpent(
    @Embedded
    val budget: BudgetEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity,
    var spentAmount: Double = 0.0
)
