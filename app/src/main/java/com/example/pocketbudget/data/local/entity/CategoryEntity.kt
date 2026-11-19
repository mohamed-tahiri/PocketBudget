package com.example.pocketbudget.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val iconName: String,
    val colorHex: String
)
