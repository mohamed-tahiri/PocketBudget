package com.example.pocketbudget.ui.categories

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketbudget.data.local.entity.CategoryEntity
import com.example.pocketbudget.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onEditClick: (CategoryEntity) -> Unit,
    private val onDeleteClick: (CategoryEntity) -> Unit
) : ListAdapter<CategoryEntity, CategoryAdapter.CategoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), onEditClick, onDeleteClick)
    }

    class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: CategoryEntity,
            onEditClick: (CategoryEntity) -> Unit,
            onDeleteClick: (CategoryEntity) -> Unit
        ) {
            binding.tvCategoryName.text = item.name
            binding.tvCategoryType.text = if (item.type == "INCOME") "Revenu" else "Dépense"
            try {
                binding.viewColorIndicator.setBackgroundColor(Color.parseColor(item.colorHex))
            } catch (e: Exception) {
                binding.viewColorIndicator.setBackgroundColor(Color.GRAY)
            }

            binding.btnEdit.setOnClickListener { onEditClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CategoryEntity>() {
            override fun areItemsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
