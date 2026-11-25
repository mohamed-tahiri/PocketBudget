package com.example.pocketbudget.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pocketbudget.R
import com.example.pocketbudget.databinding.FragmentSettingsBinding
import com.example.pocketbudget.util.InputValidator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModels()
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var hasLoadedState = false
    private var hasPopulatedProfile = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        hasLoadedState = false
        hasPopulatedProfile = savedInstanceState != null

        setupSpinners()

        binding.btnSaveProfile.setOnClickListener {
            binding.tilUserName.error = null
            binding.tilUserEmail.error = null

            val name = binding.etUserName.text.toString()
            val email = binding.etUserEmail.text.toString()
            val check = InputValidator.validateProfile(name, email)
            if (!check.isValid) {
                if (name.isBlank()) {
                    binding.tilUserName.error = check.errorMessage
                } else {
                    binding.tilUserEmail.error = check.errorMessage
                }
            } else {
                viewModel.updateProfile(name, email)
                Toast.makeText(requireContext(), "Profil enregistré avec succès", Toast.LENGTH_SHORT).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    if (!state.isLoaded) return@collectLatest

                    if (!hasPopulatedProfile) {
                        binding.etUserName.setText(state.userName)
                        binding.etUserEmail.setText(state.userEmail)
                        hasPopulatedProfile = true
                    }

                    val currencyPos = when (state.currency) {
                        "$" -> 1
                        "£" -> 2
                        "CHF" -> 3
                        else -> 0
                    }
                    if (binding.spinnerCurrency.selectedItemPosition != currencyPos) {
                        binding.spinnerCurrency.setSelection(currencyPos, false)
                    }

                    val thresholdPos = when (state.budgetAlertThreshold) {
                        90 -> 1
                        100 -> 2
                        else -> 0
                    }
                    if (binding.spinnerThreshold.selectedItemPosition != thresholdPos) {
                        binding.spinnerThreshold.setSelection(thresholdPos, false)
                    }

                    val themePos = when (state.themeMode) {
                        "LIGHT" -> 1
                        "DARK" -> 2
                        else -> 0
                    }
                    if (binding.spinnerTheme.selectedItemPosition != themePos) {
                        binding.spinnerTheme.setSelection(themePos, false)
                    }

                    hasLoadedState = true
                }
            }
        }
    }

    private fun setupSpinners() {
        val currencies = arrayOf("€ (Euro)", "$ (Dollar)", "£ (Livre)", "CHF (Franc Suisse)")
        val currencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = currencyAdapter
        binding.spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (!hasLoadedState) return
                val curr = when (position) {
                    1 -> "$"
                    2 -> "£"
                    3 -> "CHF"
                    else -> "€"
                }
                if (curr != viewModel.uiState.value.currency) {
                    viewModel.updateCurrency(curr)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val thresholds = arrayOf("80%", "90%", "100%")
        val thresholdAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, thresholds)
        thresholdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerThreshold.adapter = thresholdAdapter
        binding.spinnerThreshold.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (!hasLoadedState) return
                val thresh = when (position) {
                    1 -> 90
                    2 -> 100
                    else -> 80
                }
                if (thresh != viewModel.uiState.value.budgetAlertThreshold) {
                    viewModel.updateBudgetAlertThreshold(thresh)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val themes = arrayOf("Système", "Clair", "Sombre")
        val themeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, themes)
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTheme.adapter = themeAdapter
        binding.spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (!hasLoadedState) return
                val theme = when (position) {
                    1 -> "LIGHT"
                    2 -> "DARK"
                    else -> "SYSTEM"
                }
                if (theme != viewModel.uiState.value.themeMode) {
                    viewModel.updateThemeMode(theme)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
