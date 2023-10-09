package com.example.task.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.task.MyApplication
import com.example.task.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class UserListViewModel : ViewModel() {

    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>>
        get() = _userList

    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")

    init {
        loadUsers()
    }

    private fun loadUsers() {
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
                        val users = mutableListOf<User>()

                        val userId = snapshot.key

                        if (userId != null) {
                            if (userId != uid) {
                                val user = snapshot.getValue(User::class.java)
                                user?.let {
                                    users.add(it)
                                }
                            }
                        }

                        _userList.value = users
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Users Error", databaseError.details)
                    MyApplication.showToast("Error!")
                }
            })
        }
    }

    fun connectUser(user: User) {
        // Firebase database reference
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("Users")

        // User to connect to
        val connectedUserRef = usersRef.child(user.userId!!)

        // Other user whose connected user ID will be obtained
        val otherUserRef = usersRef.child(user.connectedUserId ?: "")

        // Connection operations
        connectedUserRef.child("isConnected").setValue(true)
        connectedUserRef.child("connectedUserId").setValue(otherUserRef.key)

        // Update the information of the person the connected user is connected to
        otherUserRef.child("isConnected").setValue(true)
        otherUserRef.child("connectedUserId").setValue(connectedUserRef.key)
    }

    fun followUser(user: User) {
        // Canlı takip işlemleri burada gerçekleştirilecek
        // Firebase güncelleme işlemleri burada yapılabilir.
    }

}