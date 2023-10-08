package com.example.task.viewmodel

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.task.MyApplication
import com.example.task.R
import com.example.task.model.Expense
import com.example.task.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ExpenseViewModel : ViewModel() {

    private val _expenseList = MutableLiveData<List<Expense>>()
    val expenseList: LiveData<List<Expense>>
        get() = _expenseList

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

    var expenseAmount: String = ""
    var expenseLocation: String? = null

    private val expenseRef = FirebaseDatabase.getInstance().getReference("Expenses")
    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")

    init {
        loadExpensesFromFirebase()
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

    private fun loadExpensesFromFirebase() {
        //get current userId
        var uid: String? = null
        val user = Firebase.auth.currentUser
        user?.let {
            uid = it.uid
        }

        viewModelScope.launch {
            expenseRef.child(uid!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val expenses = mutableListOf<Expense>()

                    for (snapshot in dataSnapshot.children) {
                        val expense = snapshot.getValue(Expense::class.java)
                        expense?.let {
                            expenses.add(it)
                        }
                    }

                    _expenseList.value = expenses
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("List Error", databaseError.details)
                    MyApplication.showToast("Error!")
                }
            })
        }
    }

    fun addExpense() {
        val amount = expenseAmount
        val category = selectedCategory.value
        var location = expenseLocation
        val sharedUser = selectedUser.value

        // If amount or category is null, warn the user and do not perform the action
        if (amount.isEmpty() || category.isNullOrEmpty()) {
            setErrorMessage("Amount and Category are required.")
            return
        }

        // If amount and category are not empty, perform the conversion
        val amountValue: Double
        try {
            amountValue = amount.toDouble()
        } catch (e: NumberFormatException) {
            MyApplication.showToast("Invalid amount.")
            return
        }

        // If location is empty or null, set it to -
        if (location.isNullOrEmpty()) {
            location = "-"
        }

        // Create expense model
        val expense = Expense("", category, amountValue, location, sharedUser)

        //get current userId
        var uid: String? = null
        val user = Firebase.auth.currentUser
        user?.let {
            uid = it.uid
        }

        // The action is taken depending on whether the shared user is null or not
        if (sharedUser == null) {

            // Add expense to Firebase
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

        } else {

            // Add expense to Firebase
            viewModelScope.launch {
                val expenseId = expenseRef.push().key
                expenseId?.let {
                    expense.id = it

                    // Adding to the user who added the expense
                    expenseRef.child(uid!!).child(it).setValue(expense)
                        .addOnSuccessListener {
                            MyApplication.showToast("Expense added successfully.")

                            // If insertion is successful open ListFragment
                            navigateToExpenseListFragment()
                        }
                        .addOnFailureListener {
                            MyApplication.showToast("Failed to add expense.")
                        }

                    // Adding to shared user
                    expenseRef.child(sharedUser.userId!!).child(it).setValue(expense)
                        .addOnSuccessListener {
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