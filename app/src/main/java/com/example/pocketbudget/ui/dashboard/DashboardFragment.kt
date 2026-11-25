package com.example.pocketbudget.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketbudget.R
import com.example.pocketbudget.databinding.FragmentDashboardBinding
import com.example.pocketbudget.ui.budgets.BudgetAdapter
import com.example.pocketbudget.util.CurrencyFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val viewModel: DashboardViewModel by viewModels()
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var alertAdapter: BudgetAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)

        alertAdapter = BudgetAdapter(
            currency = "€",
            alertThreshold = 80,
            showActions = false,
            onEditClick = { _ -> },
            onDeleteClick = { _ -> }
        )
        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlerts.adapter = alertAdapter

        binding.btnAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addEditTransactionFragment)
        }

        binding.btnUserSettings.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNav.selectedItemId = R.id.settingsFragment
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    binding.tvUserName.text = state.userName
                    binding.tvUserEmail.text = state.userEmail
                    binding.tvBalance.text = CurrencyFormatter.format(state.balance, state.currency)
                    binding.tvTotalIncome.text = CurrencyFormatter.format(state.totalIncome, state.currency)
                    binding.tvTotalExpense.text = CurrencyFormatter.format(state.totalExpense, state.currency)

                    alertAdapter.updatePreferences(state.currency, state.alertThreshold)
                    if (state.warningBudgets.isNotEmpty()) {
                        binding.tvNoAlerts.visibility = View.GONE
                        binding.rvAlerts.visibility = View.VISIBLE
                        alertAdapter.submitList(state.warningBudgets)
                    } else {
                        binding.tvNoAlerts.visibility = View.VISIBLE
                        binding.rvAlerts.visibility = View.GONE
                        alertAdapter.submitList(emptyList())
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
