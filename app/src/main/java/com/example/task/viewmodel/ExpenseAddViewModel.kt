package com.example.task.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.task.MyApplication
import com.example.task.R
import com.example.task.model.Expense
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ExpenseAddViewModel : ViewModel() {

    // LiveData for error message
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    // LiveData for predefined categories
    private val _categories = MutableLiveData<List<String>>()
    private val categories: LiveData<List<String>> get() = _categories

    // LiveData for selected category
    private val _selectedCategory = MutableLiveData<String>()
    val selectedCategory: LiveData<String> get() = _selectedCategory

    // LiveData for selected location
    private val _location = MutableLiveData<String>()
    val location: LiveData<String>
        get() = _location

    // Other properties related to expense details
    var amount: String = ""

    // Firebase Realtime Database reference
    private val expenseRef = FirebaseDatabase.getInstance().getReference("Expenses")

    // FusedLocationProviderClient for getting device location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    init {
        setPredefinedCategories()
    }

    // Function to set predefined categories
    private fun setPredefinedCategories() {
        val predefinedCategories = listOf("Fuel", "Shopping")
        _categories.value = predefinedCategories
    }

    // Function to get ArrayAdapter for categories
    fun getCategoriesAdapter(context: Context): ArrayAdapter<String> {
        val adapter = ArrayAdapter(context, R.layout.dropdown_item, categories.value!!)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    // Functions to handle selected category and add expense
    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    // Function to add an expense
    fun addExpense() {
        val expenseAmount = amount
        val expenseCategory = selectedCategory.value
        var expenseLocation = location.value

        // If amount or category is null, warn the user and do not perform the action
        if (expenseAmount.isEmpty() || expenseCategory.isNullOrEmpty()) {
            setErrorMessage("Amount and Category are required.")
            return
        }

        // If amount and category are not empty, perform the conversion
        val amountValue: Double
        try {
            amountValue = expenseAmount.toDouble()
        } catch (e: NumberFormatException) {
            MyApplication.showToast("Invalid amount.")
            return
        }

        // If location is empty or null, set it to -
        if (expenseLocation.isNullOrEmpty()) {
            expenseLocation = "-"
        }

        // Create expense model
        val expense = Expense(
            "",
            expenseCategory,
            amountValue,
            expenseLocation
        )

        //get current userId
        var uid: String? = null
        val user = Firebase.auth.currentUser
        user?.let {
            uid = it.uid
        }

        // Add expense to Firebase for current user
        viewModelScope.launch {
            val expenseId = expenseRef.push().key
            expenseId?.let {
                expense.id = it
                expenseRef.child(uid!!).child(it).setValue(expense)
                    .addOnSuccessListener {
                        MyApplication.showToast("Expense added successfully.")

                        // If insertion is successful open ListFragment
                        navigateToExpenseListFragment()
                    }
                    .addOnFailureListener {
                        MyApplication.showToast("Failed to add expense.")
                    }
            }
        }
    }

    // Functions to handle location and save expense
    fun getCurrentLocationAndSaveExpense(context: Context) {
        // Initialize
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Check location permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Stop the process if there is no permission
            MyApplication.showToast("Location permission denied.")
            return
        }

        // Get location information
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLocationLatitude = "${it.latitude}"
                    val currentLocationLongitude = "${it.longitude}"
                    saveCurrentLocation(currentLocationLatitude, currentLocationLongitude)
                }
            }
    }

    // Save current location in a variable
    private fun saveCurrentLocation(
        currentLocationLatitude: String,
        currentLocationLongitude: String
    ) {
        _location.value = "$currentLocationLatitude , $currentLocationLongitude"
    }

    // Functions to handle navigation to ExpenseListFragment
    private val _navigateToExpenseListFragment = MutableLiveData<Boolean>()
    val navigateToExpenseListFragment: LiveData<Boolean>
        get() = _navigateToExpenseListFragment

    fun onNavigateToExpenseListFragmentComplete() {
        _navigateToExpenseListFragment.value = false
    }

    private fun navigateToExpenseListFragment() {
        _navigateToExpenseListFragment.value = true
    }

    // Function to set error message
    private fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

}