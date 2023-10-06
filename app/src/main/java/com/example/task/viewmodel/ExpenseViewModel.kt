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

                _expenseList.postValue(expenses)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("List Error", databaseError.details)
                MyApplication.showToast("Error!")
            }
        })
    }

}