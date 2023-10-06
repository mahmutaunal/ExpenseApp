package com.example.task.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.task.MyApplication
import com.example.task.model.Expense
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExpenseViewModel : ViewModel() {

    private val _expenseList = MutableLiveData<List<Expense>>()
    val expenseList: LiveData<List<Expense>>
        get() = _expenseList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    var expenseAmount: String = ""
    var expenseCategory: String = ""
    var expenseLocation: String? = null

    private val expenseRef = FirebaseDatabase.getInstance().getReference("Expenses")

    init {
        loadExpensesFromFirebase()
    }

    private fun loadExpensesFromFirebase() {
        expenseRef.addValueEventListener(object : ValueEventListener {
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

    fun addExpense() {
        val amount = expenseAmount
        val category = expenseCategory
        val location = expenseLocation

        // If amount or category is null, warn the user and do not perform the action
        if (amount == null || category == null) {
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

        // Create expense model
        val expense = Expense("", category, amountValue, location)

        // Add expense to Firebase
        val expenseId = expenseRef.push().key
        expenseId?.let {
            expense.id = it
            expenseRef.child(it).setValue(expense)
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