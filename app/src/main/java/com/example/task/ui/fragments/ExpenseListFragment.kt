package com.example.task.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.task.adapter.ExpenseAdapter
import com.example.task.databinding.FragmentExpenseListBinding
import com.example.task.viewmodel.ExpenseListViewModel

class ExpenseListFragment : Fragment() {

    private lateinit var binding: FragmentExpenseListBinding
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenseAddViewModel: ExpenseListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExpenseListBinding.inflate(inflater, container, false)

        setupRecyclerView()

        expenseAddViewModel.expenseList.observe(viewLifecycleOwner) { expenses ->
            expenses?.let {
                expenseAdapter.setData(it)
            }
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expenseAdapter
        }
    }

}
