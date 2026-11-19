package com.example.pocketbudget

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pocketbudget.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PocketBudgetUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testDashboardIsDisplayed() {
        onView(withId(R.id.tvBalance)).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToTransactions() {
        onView(withId(R.id.transactionListFragment)).perform(click())
        onView(withId(R.id.etSearch)).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToAddTransaction() {
        onView(withId(R.id.btnAddTransaction)).perform(click())
        onView(withId(R.id.etTitle)).check(matches(isDisplayed()))
    }
}
