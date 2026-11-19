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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BudgetListFragment : Fragment(R.layout.fragment_budget_list) {

    private val viewModel: BudgetListViewModel by viewModels()
    private var _binding: FragmentBudgetListBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBudgetListBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    val adapter = BudgetAdapter(
                        currency = state.currency,
                        alertThreshold = state.alertThreshold,
                        onEditClick = { budgetWithSpent ->
                            showAddEditBudgetDialog(state.categories, budgetWithSpent.category.id, budgetWithSpent.budget.monthlyLimit)
                        },
                        onDeleteClick = { budgetWithSpent ->
                            viewModel.deleteBudget(budgetWithSpent.budget)
                        }
                    )
                    binding.rvBudgets.layoutManager = LinearLayoutManager(requireContext())
                    binding.rvBudgets.adapter = adapter
                    adapter.submitList(state.budgets)
                }
            }
        }

        binding.fabAddBudget.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.uiState.collectLatest { state ->
                    showAddEditBudgetDialog(state.categories, null, 0.0)
                }
            }
        }
    }

    private fun showAddEditBudgetDialog(categories: List<CategoryEntity>, preSelectedCatId: Long?, initialLimit: Double) {
        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), "Aucune catégorie de dépense disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_budget, null)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
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

        AlertDialog.Builder(requireContext())
            .setTitle("Définir un Budget")
            .setView(dialogView)
            .setPositiveButton("Enregistrer") { _, _ ->
                val limit = etLimit.text.toString().toDoubleOrNull() ?: 0.0
                val selectedPos = spinnerCategory.selectedItemPosition
                if (selectedPos >= 0 && selectedPos < categories.size) {
                    val catId = categories[selectedPos].id
                    viewModel.saveBudget(catId, limit)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
