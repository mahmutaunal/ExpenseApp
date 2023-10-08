package com.example.task.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.task.R
import com.example.task.databinding.FragmentExpenseAddBinding
import com.example.task.model.User
import com.example.task.viewmodel.ExpenseViewModel

class ExpenseAddFragment : Fragment() {

    private lateinit var binding: FragmentExpenseAddBinding
    private val expenseViewModel: ExpenseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExpenseAddBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = expenseViewModel

        setupCategoryDropdown()
        setupSharedUserDropdown()

        binding.fabDone.setOnClickListener {
            expenseViewModel.addExpense()
        }

        expenseViewModel.navigateToExpenseListFragment.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                openExpenseListFragment()
                expenseViewModel.onNavigateToExpenseListFragmentComplete()
            }
        }

        return binding.root
    }

    private fun setupCategoryDropdown() {
        val autoCompleteTextView = binding.etCategory
        autoCompleteTextView.setAdapter(expenseViewModel.getCategoriesAdapter(requireContext()))
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = autoCompleteTextView.adapter.getItem(position) as String
            expenseViewModel.setSelectedCategory(selectedCategory)
        }
    }

    private fun setupSharedUserDropdown() {
        // Create adapter to show users
        expenseViewModel.users.observe(viewLifecycleOwner) { _ ->
            expenseViewModel.getUsersAdapter(requireContext())
        }

        // Observer to update selected user when dropdown item is selected
        binding.etSharedUser.setOnItemClickListener { parent, _, position, _ ->
            val selectedUser = parent.getItemAtPosition(position) as User
            expenseViewModel.setSelectedUser(selectedUser)
        }
    }

    private fun openExpenseListFragment() {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val expenseListFragment = ExpenseListFragment()
        transaction.replace(R.id.nav_host_fragment, expenseListFragment)
        transaction.commit()
    }

}