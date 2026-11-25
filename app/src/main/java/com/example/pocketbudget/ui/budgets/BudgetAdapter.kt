package com.example.pocketbudget.ui.budgets

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketbudget.data.local.relation.BudgetWithCategoryAndSpent
import com.example.pocketbudget.databinding.ItemBudgetBinding
import com.example.pocketbudget.util.BudgetAlertCalculator
import com.example.pocketbudget.util.CurrencyFormatter

class BudgetAdapter(
    private var currency: String,
    private var alertThreshold: Int,
    private val showActions: Boolean = true,
    private val onEditClick: (BudgetWithCategoryAndSpent) -> Unit,
    private val onDeleteClick: (BudgetWithCategoryAndSpent) -> Unit
) : ListAdapter<BudgetWithCategoryAndSpent, BudgetAdapter.BudgetViewHolder>(DIFF_CALLBACK) {

    fun updatePreferences(newCurrency: String, newThreshold: Int) {
        if (currency != newCurrency || alertThreshold != newThreshold) {
            currency = newCurrency
            alertThreshold = newThreshold
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(getItem(position), currency, alertThreshold, showActions, onEditClick, onDeleteClick)
    }

    class BudgetViewHolder(private val binding: ItemBudgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: BudgetWithCategoryAndSpent,
            currency: String,
            alertThreshold: Int,
            showActions: Boolean,
            onEditClick: (BudgetWithCategoryAndSpent) -> Unit,
            onDeleteClick: (BudgetWithCategoryAndSpent) -> Unit
        ) {
            binding.tvCategoryName.text = item.category.name
            val limitStr = CurrencyFormatter.format(item.budget.monthlyLimit, currency)
            val spentStr = CurrencyFormatter.format(item.spentAmount, currency)
            binding.tvBudgetLimit.text = "Budget: $limitStr (Dépensé: $spentStr)"

            val alertStatus = BudgetAlertCalculator.calculate(item.spentAmount, item.budget.monthlyLimit, alertThreshold)
            binding.progressBar.progress = alertStatus.percentage.coerceIn(0, 100)

            if (alertStatus.isExceeded) {
                binding.progressBar.setIndicatorColor(Color.parseColor("#E53935"))
                binding.tvAlertBadge.text = "Dépassé ! (${alertStatus.percentage}%)"
                binding.tvAlertBadge.setTextColor(Color.parseColor("#E53935"))
                binding.tvAlertBadge.visibility = View.VISIBLE
            } else if (alertStatus.isWarning) {
                binding.progressBar.setIndicatorColor(Color.parseColor("#FB8C00"))
                binding.tvAlertBadge.text = "Attention (${alertStatus.percentage}%)"
                binding.tvAlertBadge.setTextColor(Color.parseColor("#FB8C00"))
                binding.tvAlertBadge.visibility = View.VISIBLE
            } else {
                binding.progressBar.setIndicatorColor(Color.parseColor("#43A047"))
                binding.tvAlertBadge.visibility = View.GONE
            }

            val actionVisibility = if (showActions) View.VISIBLE else View.GONE
            binding.btnEdit.visibility = actionVisibility
            binding.btnDelete.visibility = actionVisibility
            binding.btnEdit.setOnClickListener { onEditClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BudgetWithCategoryAndSpent>() {
            override fun areItemsTheSame(
                oldItem: BudgetWithCategoryAndSpent,
                newItem: BudgetWithCategoryAndSpent
            ): Boolean {
                return oldItem.budget.id == newItem.budget.id
            }

            override fun areContentsTheSame(
                oldItem: BudgetWithCategoryAndSpent,
                newItem: BudgetWithCategoryAndSpent
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
