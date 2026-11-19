package com.example.pocketbudget.ui.categories

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketbudget.R
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.databinding.FragmentCategoryListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryListFragment : Fragment(R.layout.fragment_category_list) {

    private val viewModel: CategoryListViewModel by viewModels()
    private var _binding: FragmentCategoryListBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCategoryListBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collectLatest { categories ->
                    val adapter = CategoryAdapter(
                        onEditClick = { category ->
                            showAddEditCategoryDialog(category)
                        },
                        onDeleteClick = { category ->
                            viewModel.deleteCategory(category)
                        }
                    )
                    binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
                    binding.rvCategories.adapter = adapter
                    adapter.submitList(categories)
                }
            }
        }

        binding.fabAddCategory.setOnClickListener {
            showAddEditCategoryDialog(null)
        }
    }

    private fun showAddEditCategoryDialog(category: CategoryEntity?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_category, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerType)
        val etColor = dialogView.findViewById<EditText>(R.id.etColor)

        val types = arrayOf("EXPENSE", "INCOME")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter

        if (category != null) {
            etName.setText(category.name)
            etColor.setText(category.colorHex)
            if (category.type == "INCOME") spinnerType.setSelection(1)
            else spinnerType.setSelection(0)
        } else {
            etColor.setText("#E53935")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (category == null) "Ajouter une catégorie" else "Modifier la catégorie")
            .setView(dialogView)
            .setPositiveButton("Enregistrer") { _, _ ->
                val name = etName.text.toString()
                val type = spinnerType.selectedItem.toString()
                val color = etColor.text.toString().ifBlank { "#E53935" }
                viewModel.saveCategory(
                    id = category?.id ?: 0L,
                    name = name,
                    type = type,
                    iconName = "ic_folder",
                    colorHex = color
                )
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
