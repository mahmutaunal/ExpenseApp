package com.example.task.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.task.MyApplication
import com.example.task.model.Expense
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlin.math.log

class ExpenseListViewModel : ViewModel() {

    private val _expenseList = MutableLiveData<List<Expense>>()
    val expenseList: LiveData<List<Expense>>
        get() = _expenseList

    private var latitude: String? = null
    private var longitude: String? = null

    private var expenses = mutableListOf<Expense>()

    // Variable to store the matched user's ID
    private val connectedUserId: MutableLiveData<String> = MutableLiveData()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val expensesRef = database.getReference("Expenses")
    private val usersRef = database.getReference("Users")

    init {
        loadExpensesForCurrentUser()
    }


    private fun loadExpensesForCurrentUser() {
        // Get current userId
        var uid: String? = null
        val user = Firebase.auth.currentUser
        user?.let {
            uid = it.uid
        }

        Log.e("current", uid.toString())

        // Get All expenses data from Firebase for current user
        viewModelScope.launch {
            expensesRef.child(uid!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
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

        // Get connected user expense list
        getConnectedUserId()
    }

    fun setConnectedUserId(userId: String) {
        connectedUserId.value = userId
        // Pull data from Firebase when Connected User ID changes
        loadExpenseForConnectedUser(userId)
    }

    private fun loadExpenseForConnectedUser(userId: String) {
        // Get All expenses data from Firebase for connected user
        if (userId != "" || userId != null) {
            Log.e("connected", userId)
            viewModelScope.launch {
                expensesRef.child(userId)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
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
                        }
                    })
            }
        }
    }

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

    fun onLocationItemClick(context: Context, position: Int) {
        // Get clicked position
        latitude = _expenseList.value!![position].locationLatitude
        longitude = _expenseList.value!![position].locationLongitude

        // Open maps app
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=")
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
    }

}