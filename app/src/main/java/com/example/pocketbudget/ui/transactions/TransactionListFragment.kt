package com.example.pocketbudget.ui.transactions

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketbudget.R
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.databinding.FragmentTransactionListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TransactionListFragment : Fragment(R.layout.fragment_transaction_list) {

    private val viewModel: TransactionListViewModel by viewModels()
    private var _binding: FragmentTransactionListBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTransactionListBinding.bind(view)

        setupSpinners()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currency.collectLatest { currency ->
                    transactionAdapter = TransactionAdapter(
                        currency = currency,
                        onItemClick = { txWithCat ->
                            val bundle = bundleOf("transactionId" to txWithCat.transaction.id)
                            findNavController().navigate(R.id.action_transactionListFragment_to_addEditTransactionFragment, bundle)
                        },
                        onDeleteClick = { txWithCat ->
                            viewModel.deleteTransaction(txWithCat.transaction)
                        }
                    )
                    binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
                    binding.rvTransactions.adapter = transactionAdapter

                    launch {
                        viewModel.transactions.collectLatest { pagingData ->
                            transactionAdapter.submitData(pagingData)
                        }
                    }
                }
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_transactionListFragment_to_addEditTransactionFragment)
        }
    }

    private fun setupSpinners() {
        val types = arrayOf("Tous les types", "Revenus (INCOME)", "Dépenses (EXPENSE)")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = typeAdapter
        binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val type = when (position) {
                    1 -> "INCOME"
                    2 -> "EXPENSE"
                    else -> ""
                }
                viewModel.setFilterType(type)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val sorts = arrayOf("Date (Plus récent)", "Date (Plus ancien)", "Montant (Plus élevé)", "Montant (Plus faible)")
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sorts)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = sortAdapter
        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val sort = when (position) {
                    1 -> "DATE_ASC"
                    2 -> "AMOUNT_DESC"
                    3 -> "AMOUNT_ASC"
                    else -> "DATE_DESC"
                }
                viewModel.setSortOrder(sort)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collectLatest { categories ->
                    val catList = mutableListOf<CategoryEntity>()
                    catList.add(CategoryEntity(id = -1, name = "Toutes les catégories", type = "", iconName = "", colorHex = ""))
                    catList.addAll(categories)
                    val catNames = catList.map { it.name }
                    val catAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, catNames)
                    catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerCategory.adapter = catAdapter
                    binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            val selectedCat = catList[position]
                            viewModel.setFilterCategory(selectedCat.id)
                        }
                        override fun onNothingSelected(parent: AdapterView<*>) {}
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
