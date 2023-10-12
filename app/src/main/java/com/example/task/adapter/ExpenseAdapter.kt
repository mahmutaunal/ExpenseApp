package com.example.task.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.task.databinding.ItemExpenseBinding
import com.example.task.model.Expense
import com.example.task.viewmodel.ExpenseListViewModel

class ExpenseAdapter : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    private var expenseList = emptyList<Expense>()
    private val expenseListViewModel = ExpenseListViewModel()

    inner class ViewHolder(val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.expense = expense
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.bind(expense)

        // Open maps for click textview
        holder.binding.locationTv.setOnClickListener {
            expenseListViewModel.onLocationItemClick(it.context, position)
        }

        // Open maps for click imageview
        holder.binding.locationIv.setOnClickListener {
            expenseListViewModel.onLocationItemClick(it.context, position)
        }
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }

    // Function to update the expenseList with a new list of Expense objects and notify the adapter of the change
    @SuppressLint("NotifyDataSetChanged")
    fun setData(newExpenseList: List<Expense>) {
        expenseList = newExpenseList
        notifyDataSetChanged()
    }
}