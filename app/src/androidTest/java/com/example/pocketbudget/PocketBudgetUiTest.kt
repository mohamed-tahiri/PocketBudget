package com.example.pocketbudget

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pocketbudget.ui.MainActivity
import org.hamcrest.Matchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PocketBudgetUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testDashboardDisplaysFinancialSummaryAndProfile() {
        onView(withId(R.id.tvBalance)).check(matches(isDisplayed()))
        onView(withId(R.id.tvUserName)).check(matches(isDisplayed()))
        onView(withId(R.id.tvUserEmail)).check(matches(isDisplayed()))
    }

    @Test
    fun testBottomNavigationAcrossAllMainScreens() {
        onView(withId(R.id.transactionListFragment)).perform(click())
        onView(withId(R.id.etSearch)).check(matches(isDisplayed()))

        onView(withId(R.id.budgetListFragment)).perform(click())
        onView(withId(R.id.tvBudgetsTitle)).check(matches(isDisplayed()))

        onView(withId(R.id.categoryListFragment)).perform(click())
        onView(withId(R.id.tvCategoriesTitle)).check(matches(isDisplayed()))

        onView(withId(R.id.settingsFragment)).perform(click())
        onView(withId(R.id.btnSaveProfile)).check(matches(isDisplayed()))
    }

    @Test
    fun testAddTransactionValidationAndNavigation() {
        Thread.sleep(400) // Allow one-time JSON seed initialization to populate categories
        onView(withId(R.id.btnAddTransaction)).perform(scrollTo(), click())
        onView(withId(R.id.etTitle)).check(matches(isDisplayed()))

        // Attempt to save with empty title -> shows validation error and stays on screen
        onView(withId(R.id.btnSave)).perform(scrollTo(), click())
        onView(withId(R.id.etTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.tilTitle)).check(matches(hasTilError("Le titre ne peut pas être vide")))

        // Fill valid transaction and save -> returns to Dashboard
        onView(withId(R.id.etTitle)).perform(replaceText("Test Espresso Café"), closeSoftKeyboard())
        onView(withId(R.id.etAmount)).perform(replaceText("4.50"), closeSoftKeyboard())
        onView(withId(R.id.btnSave)).perform(scrollTo(), click())

        onView(withId(R.id.tvBalance)).check(matches(isDisplayed()))
    }

    @Test
    fun testSearchTransactionsShowsEmptyStateOnNoMatch() {
        onView(withId(R.id.transactionListFragment)).perform(click())
        onView(withId(R.id.etSearch)).perform(replaceText("ZZZ_INEXISTANT_999"), closeSoftKeyboard())
        Thread.sleep(500)
        onView(withId(R.id.tvEmptyTransactions)).check(matches(isDisplayed()))
    }

    @Test
    fun testSettingsProfileUpdateSyncsToDashboard() {
        onView(withId(R.id.settingsFragment)).perform(click())
        Thread.sleep(300) // Wait for initial DataStore state load
        onView(withId(R.id.etUserName)).perform(scrollTo(), clearText(), replaceText("Camille Martin"), closeSoftKeyboard())
        onView(withId(R.id.etUserEmail)).perform(scrollTo(), clearText(), replaceText("camille@pocketbudget.fr"), closeSoftKeyboard())
        onView(withId(R.id.btnSaveProfile)).perform(scrollTo(), click())

        onView(withId(R.id.dashboardFragment)).perform(click())
        Thread.sleep(300)
        onView(withId(R.id.tvUserName)).check(matches(withText(containsString("Camille Martin"))))
        onView(withId(R.id.tvUserEmail)).check(matches(withText(containsString("camille@pocketbudget.fr"))))
    }

    private fun hasTilError(expectedError: String) =
        object : org.hamcrest.TypeSafeMatcher<android.view.View>() {
            override fun describeTo(description: org.hamcrest.Description) {
                description.appendText("TextInputLayout with error: $expectedError")
            }

            override fun matchesSafely(view: android.view.View): Boolean {
                if (view !is com.google.android.material.textfield.TextInputLayout) return false
                return view.error?.toString() == expectedError
            }
        }
}
