package com.example.pocketbudget.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pocketbudget.R
import com.example.pocketbudget.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModels()
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var isInitialSetup = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupSpinners()

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etUserName.text.toString()
            val email = binding.etUserEmail.text.toString()
            viewModel.updateProfile(name, email)
            Toast.makeText(requireContext(), "Profil enregistré avec succès", Toast.LENGTH_SHORT).show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    isInitialSetup = true
                    if (binding.etUserName.text.toString().isBlank() && state.userName.isNotBlank()) {
                        binding.etUserName.setText(state.userName)
                    }
                    if (binding.etUserEmail.text.toString().isBlank() && state.userEmail.isNotBlank()) {
                        binding.etUserEmail.setText(state.userEmail)
                    }

                    when (state.currency) {
                        "€" -> binding.spinnerCurrency.setSelection(0)
                        "$" -> binding.spinnerCurrency.setSelection(1)
                        "£" -> binding.spinnerCurrency.setSelection(2)
                        "CHF" -> binding.spinnerCurrency.setSelection(3)
                    }

                    when (state.budgetAlertThreshold) {
                        80 -> binding.spinnerThreshold.setSelection(0)
                        90 -> binding.spinnerThreshold.setSelection(1)
                        100 -> binding.spinnerThreshold.setSelection(2)
                    }

                    when (state.themeMode) {
                        "SYSTEM" -> {
                            binding.spinnerTheme.setSelection(0)
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        }
                        "LIGHT" -> {
                            binding.spinnerTheme.setSelection(1)
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        }
                        "DARK" -> {
                            binding.spinnerTheme.setSelection(2)
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        }
                    }
                    isInitialSetup = false
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
                if (isInitialSetup) return
                val curr = when (position) {
                    1 -> "$"
                    2 -> "£"
                    3 -> "CHF"
                    else -> "€"
                }
                viewModel.updateCurrency(curr)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val thresholds = arrayOf("80%", "90%", "100%")
        val thresholdAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, thresholds)
        thresholdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerThreshold.adapter = thresholdAdapter
        binding.spinnerThreshold.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isInitialSetup) return
                val thresh = when (position) {
                    1 -> 90
                    2 -> 100
                    else -> 80
                }
                viewModel.updateBudgetAlertThreshold(thresh)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val themes = arrayOf("Système", "Clair", "Sombre")
        val themeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, themes)
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTheme.adapter = themeAdapter
        binding.spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isInitialSetup) return
                val theme = when (position) {
                    1 -> "LIGHT"
                    2 -> "DARK"
                    else -> "SYSTEM"
                }
                viewModel.updateThemeMode(theme)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
