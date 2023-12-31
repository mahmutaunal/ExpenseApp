package com.example.task.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.task.MyApplication.Companion.showToast
import com.example.task.R
import com.example.task.databinding.FragmentExpenseAddBinding
import com.example.task.viewmodel.ExpenseAddViewModel

class ExpenseAddFragment : Fragment() {

    private lateinit var binding: FragmentExpenseAddBinding
    private val expenseAddViewModel: ExpenseAddViewModel by viewModels()

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExpenseAddBinding.inflate(inflater, container, false)

        // Set ViewModel and lifecycle owner for binding in the layout
        binding.viewModel = expenseAddViewModel
        binding.lifecycleOwner = this

        // Setup the category dropdown
        setupCategoryDropdown()

        // Handle adding an expense when fabDone is clicked
        binding.fabDone.setOnClickListener {
            expenseAddViewModel.addExpense()
        }

        // Observe a LiveData to navigate to the expense list fragment
        expenseAddViewModel.navigateToExpenseListFragment.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                openExpenseListFragment()
                expenseAddViewModel.onNavigateToExpenseListFragmentComplete()
            }
        }

        // Handle location permission and getting the current location when currentLocationTv is clicked
        binding.currentLocationTv.setOnClickListener {
            checkLocationPermissionAndGetCurrentLocation()
        }

        return binding.root
    }

    private fun setupCategoryDropdown() {
        val autoCompleteTextView = binding.etCategory
        autoCompleteTextView.setAdapter(expenseAddViewModel.getCategoriesAdapter(requireContext()))
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = autoCompleteTextView.adapter.getItem(position) as String
            expenseAddViewModel.setSelectedCategory(selectedCategory)
        }
    }

    private fun openExpenseListFragment() {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val expenseListFragment = ExpenseListFragment()
        transaction.replace(R.id.nav_host_fragment, expenseListFragment)
        transaction.commit()
    }

    private fun checkLocationPermissionAndGetCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Ask for permission
            requestLocationPermission()
        } else {
            // Permission already exists, get location information
            expenseAddViewModel.getCurrentLocationAndSaveExpense(requireContext())
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User gave permission, get location information
                expenseAddViewModel.getCurrentLocationAndSaveExpense(requireContext())
            } else {
                // User denied permission, show warning
                showToast("Location permission denied.")
            }
        }
    }

}