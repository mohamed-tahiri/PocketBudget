package com.example.pocketbudget.ui.transactions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketbudget.data.local.relation.TransactionWithCategory
import com.example.pocketbudget.databinding.ItemTransactionBinding
import com.example.pocketbudget.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private var currency: String,
    private val onItemClick: (TransactionWithCategory) -> Unit,
    private val onDeleteClick: (TransactionWithCategory) -> Unit
) : PagingDataAdapter<TransactionWithCategory, TransactionAdapter.TransactionViewHolder>(DIFF_CALLBACK) {

    fun updateCurrency(newCurrency: String) {
        if (currency != newCurrency) {
            currency = newCurrency
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item, currency, onItemClick, onDeleteClick)
        }
    }

    class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(
            item: TransactionWithCategory,
            currency: String,
            onItemClick: (TransactionWithCategory) -> Unit,
            onDeleteClick: (TransactionWithCategory) -> Unit
        ) {
            binding.tvTitle.text = item.transaction.title
            binding.tvCategory.text = item.category.name
            binding.tvDate.text = dateFormat.format(Date(item.transaction.date))

            val prefix = if (item.transaction.type == "INCOME") "+" else "-"
            binding.tvAmount.text = "$prefix${CurrencyFormatter.format(item.transaction.amount, currency)}"
            binding.tvAmount.setTextColor(
                if (item.transaction.type == "INCOME") Color.parseColor("#43A047")
                else Color.parseColor("#E53935")
            )

            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionWithCategory>() {
            override fun areItemsTheSame(
                oldItem: TransactionWithCategory,
                newItem: TransactionWithCategory
            ): Boolean {
                return oldItem.transaction.id == newItem.transaction.id
            }

            override fun areContentsTheSame(
                oldItem: TransactionWithCategory,
                newItem: TransactionWithCategory
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
