package com.example.pocketbudget.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketbudget.data.datastore.UserPreferencesManager
import com.example.pocketbudget.util.InputValidator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoaded: Boolean = false,
    val currency: String = "€",
    val budgetAlertThreshold: Int = 80,
    val themeMode: String = "SYSTEM",
    val userName: String = "",
    val userEmail: String = ""
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = UserPreferencesManager(application)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesManager.currencyFlow,
        preferencesManager.budgetAlertThresholdFlow,
        preferencesManager.themeModeFlow,
        preferencesManager.userNameFlow,
        preferencesManager.userEmailFlow
    ) { currency, threshold, theme, name, email ->
        SettingsUiState(
            isLoaded = true,
            currency = currency,
            budgetAlertThreshold = threshold,
            themeMode = theme,
            userName = name,
            userEmail = email
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            preferencesManager.updateCurrency(currency)
        }
    }

    fun updateBudgetAlertThreshold(threshold: Int) {
        viewModelScope.launch {
            preferencesManager.updateBudgetAlertThreshold(threshold)
        }
    }

    fun updateThemeMode(themeMode: String) {
        viewModelScope.launch {
            preferencesManager.updateThemeMode(themeMode)
        }
    }

    fun updateProfile(name: String, email: String) {
        val check = InputValidator.validateProfile(name, email)
        if (!check.isValid) return
        viewModelScope.launch {
            preferencesManager.updateUserName(name.trim())
            preferencesManager.updateUserEmail(email.trim())
        }
    }
}
