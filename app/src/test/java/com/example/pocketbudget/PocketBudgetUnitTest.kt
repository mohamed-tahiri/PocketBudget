package com.example.pocketbudget

import com.example.pocketbudget.util.BudgetAlertCalculator
import com.example.pocketbudget.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PocketBudgetUnitTest {

    @Test
    fun testCurrencyFormatter() {
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
    fun testBudgetAlertCalculatorWarning() {
        val status = BudgetAlertCalculator.calculate(85.0, 100.0, 80)
        assertEquals(85, status.percentage)
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
    fun testTransactionValidationEmptyTitle() {
        val title = ""
        val amount = 50.0
        val isValid = title.isNotBlank() && amount > 0.0
        assertFalse(isValid)
    }

    @Test
    fun testTransactionValidationZeroAmount() {
        val title = "Courses"
        val amount = 0.0
        val isValid = title.isNotBlank() && amount > 0.0
        assertFalse(isValid)
    }

    @Test
    fun testTransactionValidationValid() {
        val title = "Courses"
        val amount = 45.0
        val isValid = title.isNotBlank() && amount > 0.0
        assertTrue(isValid)
    }

    @Test
    fun testCategoryNameValidation() {
        val categoryName = ""
        val isValid = categoryName.isNotBlank()
        assertFalse(isValid)
    }
}
