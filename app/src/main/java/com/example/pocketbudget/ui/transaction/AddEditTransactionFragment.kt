package com.example.pocketbudget.ui.transaction

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.pocketbudget.R
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.databinding.FragmentAddEditTransactionBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddEditTransactionFragment : Fragment(R.layout.fragment_add_edit_transaction) {

    private val viewModel: AddEditTransactionViewModel by viewModels()
    private var _binding: FragmentAddEditTransactionBinding? = null
    private val binding get() = _binding!!

    private var categoriesList: List<CategoryEntity> = emptyList()
    private var currentTransactionId: Long = 0L
    private var pendingCategoryId: Long? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditTransactionBinding.bind(view)

        currentTransactionId = arguments?.getLong("transactionId", 0L) ?: 0L
        pendingCategoryId = savedInstanceState?.getLong(KEY_SELECTED_CATEGORY_ID, -1L)?.takeIf { it > 0L }

        binding.tvTitleHeader.text = if (currentTransactionId == 0L) {
            "Ajouter une Transaction"
        } else {
            "Modifier la Transaction"
        }

        if (currentTransactionId != 0L && savedInstanceState == null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val tx = viewModel.getTransactionById(currentTransactionId)
                if (tx != null && _binding != null) {
                    binding.etTitle.setText(tx.title)
                    binding.etAmount.setText(tx.amount.toString())
                    if (tx.type == "INCOME") {
                        binding.rbIncome.isChecked = true
                    } else {
                        binding.rbExpense.isChecked = true
                    }
                    pendingCategoryId = tx.categoryId
                    selectCategoryIfPresent(tx.categoryId)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collectLatest { categories ->
                    categoriesList = categories
                    val names = categories.map { it.name }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerCategory.adapter = adapter
                    pendingCategoryId?.let { selectCategoryIfPresent(it) }
                }
            }
        }

        binding.btnSave.setOnClickListener {
            binding.tilTitle.error = null
            binding.tilAmount.error = null

            val title = binding.etTitle.text.toString()
            val amountStr = binding.etAmount.text.toString()
            val amount = amountStr.toDoubleOrNull()
            val type = if (binding.rbIncome.isChecked) "INCOME" else "EXPENSE"

            val selectedPosition = binding.spinnerCategory.selectedItemPosition
            val categoryId = if (selectedPosition in categoriesList.indices) {
                categoriesList[selectedPosition].id
            } else -1L

            viewModel.saveTransaction(
                id = currentTransactionId,
                title = title,
                amount = amount,
                type = type,
                categoryId = categoryId,
                date = System.currentTimeMillis(),
                onSuccess = {
                    Toast.makeText(requireContext(), "Transaction enregistrée", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                },
                onError = { errorMsg ->
                    when {
                        title.isBlank() -> binding.tilTitle.error = errorMsg
                        amount == null || !amount.isFinite() || amount <= 0.0 -> binding.tilAmount.error = errorMsg
                        else -> Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    private fun selectCategoryIfPresent(categoryId: Long) {
        val index = categoriesList.indexOfFirst { it.id == categoryId }
        if (index >= 0) {
            binding.spinnerCategory.setSelection(index)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val selectedPosition = _binding?.spinnerCategory?.selectedItemPosition ?: -1
        if (selectedPosition in categoriesList.indices) {
            outState.putLong(KEY_SELECTED_CATEGORY_ID, categoriesList[selectedPosition].id)
        } else {
            pendingCategoryId?.let { outState.putLong(KEY_SELECTED_CATEGORY_ID, it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_SELECTED_CATEGORY_ID = "selected_category_id"
    }
}
