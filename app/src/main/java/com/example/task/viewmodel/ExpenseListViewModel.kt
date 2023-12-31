package com.example.task.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.task.MyApplication
import com.example.task.R
import com.example.task.model.Expense
import com.example.task.ui.fragments.ExpenseAddFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ExpenseListViewModel : ViewModel() {

    // LiveData for expense list and related properties
    private val _expenseList = MutableLiveData<List<Expense>>()
    val expenseList: LiveData<List<Expense>>
        get() = _expenseList

    private var location: String? = null
    private var expenses = mutableListOf<Expense>()

    // Variable to store the matched user's ID
    private val connectedUserId: MutableLiveData<String> = MutableLiveData()

    // LiveData for refreshing indicators
    private val _refreshing = MutableLiveData<Boolean>()
    val refreshing: LiveData<Boolean>
        get() = _refreshing

    // LiveData for loading indicators
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    // LiveData for the message to be shown if the expense list is empty
    private val _data: MutableLiveData<String> = MutableLiveData()
    val data: LiveData<String> get() = _data

    // Database references
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val expensesRef = database.getReference("Expenses")
    private val usersRef = database.getReference("Users")

    init {
        loadExpensesForCurrentUser()
    }

    // Function to load expenses for the current user
    private fun loadExpensesForCurrentUser() {
        _isLoading.value = true

        // Get current userId
        var uid: String? = null
        val user = Firebase.auth.currentUser
        user?.let {
            uid = it.uid
        }

        // Get All expenses data from Firebase for current user
        viewModelScope.launch {
            _isLoading.value = false

            expensesRef.child(uid!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val expense = snapshot.getValue(Expense::class.java)
                        if (expense!!.category.isEmpty()) {
                            _data.value = "There is no expense to list!"
                        } else {
                            expense.let {
                                expenses.add(it)
                                _data.value = ""
                            }
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

        // Get connected user expense list
        getConnectedUserId()
    }

    // Functions to handle connected user and their expenses
    fun setConnectedUserId(userId: String) {
        connectedUserId.value = userId
        // Pull data from Firebase when Connected User ID changes
        loadExpenseForConnectedUser(userId)
    }

    // Function to load expenses for the connected user
    private fun loadExpenseForConnectedUser(userId: String) {
        // Get All expenses data from Firebase for connected user
        if (userId != "" || userId != null) {
            viewModelScope.launch {
                expensesRef.child(userId)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                val expense = snapshot.getValue(Expense::class.java)
                                if (expense!!.category.isEmpty()) {
                                    _data.value = "There is no expense to list!"
                                } else {
                                    expense.let {
                                        expenses.add(it)
                                        _data.value = ""
                                    }
                                }
                            }

                            _expenseList.value = expenses
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e("List Error", databaseError.details)
                        }
                    })
            }
        }
    }

    // Functions to handle connected user and their expenses
    private fun getConnectedUserId() {
        //get current userId
        var uid: String? = null
        val user = Firebase.auth.currentUser
        user?.let {
            uid = it.uid
        }

        val connectedUserIdRef = usersRef.child(uid!!).child("connectedUserId")
        val connectedUserIdListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                setConnectedUserId(dataSnapshot.value.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Connected User Id Error", databaseError.details)
            }
        }

        connectedUserIdRef.addValueEventListener(connectedUserIdListener)
    }

    // Function to handle location item click
    fun onLocationItemClick(context: Context, position: Int) {
        // Get clicked position
        location = _expenseList.value!![position].location

        if (location != "" || location != null || location != "-") {
            // Open maps app
            val gmmIntentUri = Uri.parse("geo:0,0?q=$location")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            // If you don't have the maps app installed, head to the Play Store to install Google Maps
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val appStoreIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.google.android.apps.maps")
                )
                context.startActivity(appStoreIntent)
            }
        } else {
            MyApplication.showToast("Invalid location!")
        }
    }

    // Functions to handle navigation and UI interactions
    fun openExpenseAddFragment(context: Context) {
        val expenseAddFragment = ExpenseAddFragment()
        (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, expenseAddFragment)
            .addToBackStack(null)
            .commit()
    }

    fun onRefresh() {
        _refreshing.value = true

        // Simulate a delay and then stop the refreshing animation
        Handler(Looper.getMainLooper()).postDelayed({
            _refreshing.value = false
        }, 2000)

        expenses.clear()
        loadExpensesForCurrentUser()
    }

}