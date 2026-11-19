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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditTransactionBinding.bind(view)

        currentTransactionId = arguments?.getLong("transactionId", 0L) ?: 0L

        if (currentTransactionId != 0L) {
            viewLifecycleOwner.lifecycleScope.launch {
                val tx = viewModel.getTransactionById(currentTransactionId)
                if (tx != null) {
                    binding.etTitle.setText(tx.title)
                    binding.etAmount.setText(tx.amount.toString())
                    if (tx.type == "INCOME") {
                        binding.rbIncome.isChecked = true
                    } else {
                        binding.rbExpense.isChecked = true
                    }
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
                }
            }
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val amountStr = binding.etAmount.text.toString()
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val type = if (binding.rbIncome.isChecked) "INCOME" else "EXPENSE"

            val selectedPosition = binding.spinnerCategory.selectedItemPosition
            val categoryId = if (selectedPosition >= 0 && selectedPosition < categoriesList.size) {
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
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
