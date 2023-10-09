package com.example.task.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.task.MyApplication
import com.example.task.R
import com.example.task.model.Expense
import com.example.task.model.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ExpenseAddViewModel : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _categories = MutableLiveData<List<String>>()
    private val categories: LiveData<List<String>> get() = _categories

    private val _selectedCategory = MutableLiveData<String>()
    val selectedCategory: LiveData<String> get() = _selectedCategory

    private val _selectedUser = MutableLiveData<User?>()
    val selectedUser: LiveData<User?> get() = _selectedUser

    private val registeredUserList = mutableListOf<User>()

    private val _locationLatitude = MutableLiveData<String>()
    val locationLatitude: LiveData<String>
        get() = _locationLatitude

    private val _locationLongitude = MutableLiveData<String>()
    val locationLongitude: LiveData<String>
        get() = _locationLongitude

    var amount: String = ""

    private val expenseRef = FirebaseDatabase.getInstance().getReference("Expenses")
    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    init {
        loadRegisteredUsers()

        setPredefinedCategories()
        setUsers(registeredUserList)
    }

    private fun setPredefinedCategories() {
        val predefinedCategories = listOf("Fuel", "Shopping")
        _categories.value = predefinedCategories
    }

    fun getCategoriesAdapter(context: Context): ArrayAdapter<String> {
        val adapter = ArrayAdapter(context, R.layout.dropdown_item, categories.value!!)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedUser(user: User?) {
        _selectedUser.value = user
    }

    fun getUsersAdapter(context: Context): ArrayAdapter<User> {
        val adapter = ArrayAdapter(context, R.layout.dropdown_item, users.value.orEmpty())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    private fun setUsers(users: List<User>) {
        _users.value = users
    }

    private fun loadRegisteredUsers() {
        //get current userId
        var uid: String? = null
        val user = Firebase.auth.currentUser
        user?.let {
            uid = it.uid
        }

        // Get all registered users data from Firebase
        viewModelScope.launch {
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val userId = snapshot.key
                        val email = snapshot.child("email").getValue(String::class.java) ?: ""

                        if (userId != null) {
                            if (userId != uid) {
                                val user = User(userId, email)
                                registeredUserList.add(user)
                            }
                        }
                    }
                    _users.postValue(registeredUserList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Users Error", databaseError.details)
                    MyApplication.showToast("Error!")
                }
            })
        }
    }

    fun addExpense() {
        val expenseAmount = amount
        val expenseCategory = selectedCategory.value
        var expenseLocationLatitude = locationLatitude.value
        var expenseLocationLongitude = locationLongitude.value
        val expenseSharedUser = selectedUser.value

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
        if (expenseLocationLatitude.isNullOrEmpty() && expenseLocationLongitude.isNullOrEmpty()) {
            expenseLocationLatitude = "-"
            expenseLocationLongitude = "-"
        }

        // Create expense model
        val expense = Expense("", expenseCategory, amountValue, expenseLocationLatitude, expenseLocationLongitude, expenseSharedUser)

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

        // Add expense to Firebase for shared user if not null
        if (expenseSharedUser != null) {
            viewModelScope.launch {
                val expenseId = expenseRef.push().key
                expenseId?.let {
                    expense.id = it
                    expenseRef.child(expenseSharedUser.userId!!).child(it).setValue(expense)
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

    }

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
                    Log.e("Location", it.latitude.toString())
                }
            }
    }

    private fun saveCurrentLocation(currentLocationLatitude: String, currentLocationLongitude: String) {
        _locationLatitude.value = currentLocationLatitude
        _locationLongitude.value = currentLocationLongitude
    }

    // Used to open ExpenseListFragment after expense is added
    private val _navigateToExpenseListFragment = MutableLiveData<Boolean>()
    val navigateToExpenseListFragment: LiveData<Boolean>
        get() = _navigateToExpenseListFragment

    fun onNavigateToExpenseListFragmentComplete() {
        _navigateToExpenseListFragment.value = false
    }

    private fun navigateToExpenseListFragment() {
        _navigateToExpenseListFragment.value = true
    }

    private fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

}