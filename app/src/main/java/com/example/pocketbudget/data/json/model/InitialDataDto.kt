package com.example.pocketbudget.data.json.model

import com.google.gson.annotations.SerializedName

data class InitialDataDto(
    @SerializedName("categories")
    val categories: List<CategoryDto>,
    @SerializedName("budgets")
    val budgets: List<BudgetDto>,
    @SerializedName("transactions")
    val transactions: List<TransactionDto>
)

data class CategoryDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("iconName")
    val iconName: String,
    @SerializedName("colorHex")
    val colorHex: String
)

data class BudgetDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("categoryId")
    val categoryId: Long,
    @SerializedName("monthlyLimit")
    val monthlyLimit: Double,
    @SerializedName("monthYear")
    val monthYear: String
)

data class TransactionDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("type")
    val type: String,
    @SerializedName("categoryId")
    val categoryId: Long,
    @SerializedName("date")
    val date: Long
)
