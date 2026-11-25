package com.example.pocketbudget.ui.budgets

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketbudget.R
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.databinding.FragmentBudgetListBinding
import com.example.pocketbudget.util.InputValidator
import com.example.pocketbudget.util.AppConstants
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BudgetListFragment : Fragment(R.layout.fragment_budget_list) {

    private val viewModel: BudgetListViewModel by viewModels()
    private var _binding: FragmentBudgetListBinding? = null
    private val binding get() = _binding!!

    private lateinit var budgetAdapter: BudgetAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBudgetListBinding.bind(view)

        budgetAdapter = BudgetAdapter(
            currency = "€",
            alertThreshold = 80,
            showActions = true,
            onEditClick = { budgetWithSpent ->
                showAddEditBudgetDialog(
                    viewModel.uiState.value.categories,
                    budgetWithSpent.category.id,
                    budgetWithSpent.budget.monthlyLimit
                )
            },
            onDeleteClick = { budgetWithSpent ->
                viewModel.deleteBudget(budgetWithSpent.budget)
            }
        )
        binding.rvBudgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBudgets.adapter = budgetAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    budgetAdapter.updatePreferences(state.currency, state.alertThreshold)
                    budgetAdapter.submitList(state.budgets)

                    if (state.budgets.isEmpty()) {
                        binding.tvEmptyBudgets.visibility = View.VISIBLE
                        binding.rvBudgets.visibility = View.GONE
                    } else {
                        binding.tvEmptyBudgets.visibility = View.GONE
                        binding.rvBudgets.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.fabAddBudget.setOnClickListener {
            showAddEditBudgetDialog(viewModel.uiState.value.categories, null, 0.0)
        }
    }

    private fun showAddEditBudgetDialog(
        categories: List<CategoryEntity>,
        preSelectedCatId: Long?,
        initialLimit: Double
    ) {
        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), "Aucune catégorie de dépense disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_budget, null)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val tilLimit = dialogView.findViewById<TextInputLayout>(R.id.tilLimit)
        val etLimit = dialogView.findViewById<EditText>(R.id.etLimit)

        if (initialLimit > 0.0) {
            etLimit.setText(initialLimit.toString())
        }

        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        if (preSelectedCatId != null) {
            val index = categories.indexOfFirst { it.id == preSelectedCatId }
            if (index >= 0) spinnerCategory.setSelection(index)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (preSelectedCatId == null) "Définir un Budget" else "Modifier le Budget")
            .setView(dialogView)
            .setPositiveButton("Enregistrer", null)
            .setNegativeButton("Annuler", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val limit = etLimit.text.toString().toDoubleOrNull()
                val selectedPos = spinnerCategory.selectedItemPosition
                val catId = if (selectedPos in categories.indices) categories[selectedPos].id else -1L
                val check = InputValidator.validateBudget(
                    catId,
                    limit,
                    AppConstants.DEFAULT_BUDGET_MONTH
                )
                if (!check.isValid) {
                    tilLimit.error = check.errorMessage
                } else {
                    tilLimit.error = null
                    limit?.let { validLimit ->
                        viewModel.saveBudget(catId, validLimit)
                    }
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
