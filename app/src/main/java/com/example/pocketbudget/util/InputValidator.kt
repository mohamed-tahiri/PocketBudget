package com.example.pocketbudget.util

/**
 * Pure Kotlin validation helpers for forms and domain inputs.
 */
object InputValidator {

    private val HEX_COLOR_REGEX = Regex("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")
    private val MONTH_YEAR_REGEX = Regex("^\\d{4}-(0[1-9]|1[0-2])$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val VALID_TYPES = setOf("INCOME", "EXPENSE")

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateTransaction(
        title: String,
        amount: Double?,
        type: String,
        categoryId: Long
    ): ValidationResult {
        if (title.isBlank()) {
            return ValidationResult(false, "Le titre ne peut pas être vide")
        }
        if (amount == null || amount.isNaN() || amount <= 0.0) {
            return ValidationResult(false, "Le montant doit être supérieur à 0")
        }
        if (type !in VALID_TYPES) {
            return ValidationResult(false, "Type de transaction invalide")
        }
        if (categoryId <= 0L) {
            return ValidationResult(false, "Veuillez sélectionner une catégorie")
        }
        return ValidationResult(true)
    }

    fun validateCategory(
        name: String,
        type: String,
        colorHex: String
    ): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, "Le nom de la catégorie ne peut pas être vide")
        }
        if (type !in VALID_TYPES) {
            return ValidationResult(false, "Type de catégorie invalide")
        }
        if (!HEX_COLOR_REGEX.matches(colorHex.trim())) {
            return ValidationResult(false, "Couleur hexadécimale invalide (ex: #E53935)")
        }
        return ValidationResult(true)
    }

    fun validateBudget(
        categoryId: Long,
        monthlyLimit: Double?,
        monthYear: String
    ): ValidationResult {
        if (categoryId <= 0L) {
            return ValidationResult(false, "Veuillez sélectionner une catégorie")
        }
        if (monthlyLimit == null || monthlyLimit.isNaN() || monthlyLimit <= 0.0) {
            return ValidationResult(false, "La limite mensuelle doit être supérieure à 0")
        }
        if (!MONTH_YEAR_REGEX.matches(monthYear.trim())) {
            return ValidationResult(false, "Format de mois invalide (YYYY-MM)")
        }
        return ValidationResult(true)
    }

    fun validateProfile(name: String, email: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, "Le nom ne peut pas être vide")
        }
        if (email.isBlank() || !EMAIL_REGEX.matches(email.trim())) {
            return ValidationResult(false, "Adresse email invalide")
        }
        return ValidationResult(true)
    }
}
