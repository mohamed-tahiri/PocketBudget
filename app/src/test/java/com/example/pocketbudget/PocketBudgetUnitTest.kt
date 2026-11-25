package com.example.pocketbudget

import com.example.pocketbudget.data.json.InitialDataValidator
import com.example.pocketbudget.data.json.model.BudgetDto
import com.example.pocketbudget.data.json.model.CategoryDto
import com.example.pocketbudget.data.json.model.InitialDataDto
import com.example.pocketbudget.data.json.model.TransactionDto
import com.example.pocketbudget.util.BudgetAlertCalculator
import com.example.pocketbudget.util.CurrencyFormatter
import com.example.pocketbudget.util.InputValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PocketBudgetUnitTest {

    @Test
    fun testCurrencyFormatterEuro() {
        val formatted = CurrencyFormatter.format(1234.56, "€")
        assertEquals("1234.56 €", formatted)
    }

    @Test
    fun testCurrencyFormatterDollar() {
        val formatted = CurrencyFormatter.format(100.0, "$")
        assertEquals("100.00 $", formatted)
    }

    @Test
    fun testBudgetAlertCalculatorNormal() {
        val status = BudgetAlertCalculator.calculate(40.0, 100.0, 80)
        assertEquals(40, status.percentage)
        assertFalse(status.isWarning)
        assertFalse(status.isExceeded)
    }

    @Test
    fun testBudgetAlertCalculatorWarningAtThreshold() {
        val status = BudgetAlertCalculator.calculate(80.0, 100.0, 80)
        assertEquals(80, status.percentage)
        assertTrue(status.isWarning)
        assertFalse(status.isExceeded)
    }

    @Test
    fun testBudgetAlertCalculatorExceeded() {
        val status = BudgetAlertCalculator.calculate(110.0, 100.0, 80)
        assertEquals(110, status.percentage)
        assertTrue(status.isWarning)
        assertTrue(status.isExceeded)
    }

    @Test
    fun testBudgetAlertCalculatorZeroLimit() {
        val status = BudgetAlertCalculator.calculate(50.0, 0.0, 80)
        assertEquals(0, status.percentage)
        assertFalse(status.isWarning)
        assertFalse(status.isExceeded)
    }

    @Test
    fun testTransactionValidationRejectsBlankTitle() {
        val result = InputValidator.validateTransaction("   ", 50.0, "EXPENSE", 1L)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun testTransactionValidationRejectsNonPositiveAmount() {
        val zeroResult = InputValidator.validateTransaction("Courses", 0.0, "EXPENSE", 1L)
        val negativeResult = InputValidator.validateTransaction("Courses", -12.5, "EXPENSE", 1L)
        assertFalse(zeroResult.isValid)
        assertFalse(negativeResult.isValid)
    }

    @Test
    fun testTransactionValidationAcceptsValidInput() {
        val result = InputValidator.validateTransaction("Courses Supermarché", 45.5, "EXPENSE", 1L)
        assertTrue(result.isValid)
    }

    @Test
    fun testCategoryValidationChecksNameAndHexColor() {
        assertFalse(InputValidator.validateCategory("", "EXPENSE", "#E53935").isValid)
        assertFalse(InputValidator.validateCategory("Food", "EXPENSE", "red").isValid)
        assertFalse(InputValidator.validateCategory("Food", "OTHER", "#E53935").isValid)
        assertTrue(InputValidator.validateCategory("Alimentation", "EXPENSE", "#E53935").isValid)
    }

    @Test
    fun testBudgetValidationChecksLimitAndMonthFormat() {
        assertFalse(InputValidator.validateBudget(1L, 0.0, "2025-05").isValid)
        assertFalse(InputValidator.validateBudget(1L, 300.0, "2025-13").isValid)
        assertTrue(InputValidator.validateBudget(1L, 300.0, "2025-05").isValid)
    }

    @Test
    fun testProfileValidationChecksNameAndEmail() {
        assertFalse(InputValidator.validateProfile("", "user@univ.fr").isValid)
        assertFalse(InputValidator.validateProfile("Alice", "not-an-email").isValid)
        assertTrue(InputValidator.validateProfile("Alice Dupont", "alice.dupont@univ.fr").isValid)
    }

    @Test
    fun testInitialDataValidatorFiltersInvalidAndDuplicateRecords() {
        val dto = InitialDataDto(
            categories = listOf(
                CategoryDto(1L, "Alimentation", "EXPENSE", "ic_food", "#E53935"),
                CategoryDto(1L, "Duplicate Id", "EXPENSE", "ic_food", "#E53935"),
                CategoryDto(2L, "", "EXPENSE", "ic_bad", "#E53935"),
                CategoryDto(3L, "Salaire", "INCOME", "ic_work", "#43A047")
            ),
            budgets = listOf(
                BudgetDto(1L, 1L, 500.0, "2025-05"),
                BudgetDto(2L, 3L, 500.0, "2025-05"), // Income category cannot have expense budget
                BudgetDto(3L, 99L, 200.0, "2025-05") // Unknown category
            ),
            transactions = listOf(
                TransactionDto(1L, "Courses", 42.0, "EXPENSE", 1L, 1746096000000L),
                TransactionDto(2L, "Bad Amount", -5.0, "EXPENSE", 1L, 1746096000000L),
                TransactionDto(3L, "Orphan Tx", 20.0, "EXPENSE", 99L, 1746096000000L)
            )
        )

        val validated = InitialDataValidator.validate(dto)
        assertEquals(2, validated.categories.size)
        assertEquals(1, validated.budgets.size)
        assertEquals(1, validated.transactions.size)
        assertEquals(6, validated.skippedCount)
    }
}
