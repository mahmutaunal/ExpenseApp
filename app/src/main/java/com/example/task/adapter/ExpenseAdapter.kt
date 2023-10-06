package com.example.task.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.task.databinding.ItemExpenseBinding
import com.example.task.model.Expense

class ExpenseAdapter() :
    RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    private var expenseList = emptyList<Expense>()

    class ViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.expense = expense
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentExpense = expenseList[position]
        holder.bind(currentExpense)
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newExpenseList: List<Expense>) {
        expenseList = newExpenseList
        notifyDataSetChanged()
    }

}