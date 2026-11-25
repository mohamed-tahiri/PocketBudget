package com.example.pocketbudget.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val CURRENCY = stringPreferencesKey("currency")
        val BUDGET_ALERT_THRESHOLD = intPreferencesKey("budget_alert_threshold")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val INITIAL_DATA_IMPORTED = booleanPreferencesKey("initial_data_imported")
    }

    val initialDataImportedFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.INITIAL_DATA_IMPORTED] ?: false
        }

    suspend fun isInitialDataImported(): Boolean {
        return initialDataImportedFlow.first()
    }

    suspend fun setInitialDataImported(imported: Boolean) {
        Log.d(TAG, "setInitialDataImported: $imported")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INITIAL_DATA_IMPORTED] = imported
        }
    }

    val currencyFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CURRENCY]?.takeIf { it in SUPPORTED_CURRENCIES } ?: "€"
        }

    val budgetAlertThresholdFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            (preferences[PreferencesKeys.BUDGET_ALERT_THRESHOLD] ?: 80)
                .coerceIn(MIN_ALERT_THRESHOLD, MAX_ALERT_THRESHOLD)
                .let { threshold -> threshold.takeIf { it in SUPPORTED_ALERT_THRESHOLDS } ?: 80 }
        }

    val themeModeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE]?.takeIf { it in SUPPORTED_THEME_MODES } ?: "SYSTEM"
        }

    val userNameFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_NAME] ?: "Étudiant / Développeur Senior"
        }

    val userEmailFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_EMAIL] ?: "pocketbudget.pro@univ.fr"
        }

    suspend fun updateCurrency(currency: String) {
        val normalizedCurrency = currency.takeIf { it in SUPPORTED_CURRENCIES } ?: "€"
        Log.d(TAG, "updateCurrency: $normalizedCurrency")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY] = normalizedCurrency
        }
    }

    suspend fun updateBudgetAlertThreshold(threshold: Int) {
        val normalizedThreshold = threshold.takeIf { it in SUPPORTED_ALERT_THRESHOLDS } ?: 80
        Log.d(TAG, "updateBudgetAlertThreshold: $normalizedThreshold")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUDGET_ALERT_THRESHOLD] = normalizedThreshold
        }
    }

    suspend fun updateThemeMode(themeMode: String) {
        val normalizedThemeMode = themeMode.takeIf { it in SUPPORTED_THEME_MODES } ?: "SYSTEM"
        Log.d(TAG, "updateThemeMode: $normalizedThemeMode")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = normalizedThemeMode
        }
    }

    suspend fun updateUserName(name: String) {
        Log.d(TAG, "updateUserName: $name")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun updateUserEmail(email: String) {
        Log.d(TAG, "updateUserEmail: $email")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_EMAIL] = email
        }
    }

    companion object {
        private const val TAG = "UserPreferencesManager"
        private val SUPPORTED_CURRENCIES = setOf("€", "$", "£", "CHF")
        private val SUPPORTED_ALERT_THRESHOLDS = setOf(80, 90, 100)
        private val SUPPORTED_THEME_MODES = setOf("SYSTEM", "LIGHT", "DARK")
        private const val MIN_ALERT_THRESHOLD = 0
        private const val MAX_ALERT_THRESHOLD = 100
    }
}
