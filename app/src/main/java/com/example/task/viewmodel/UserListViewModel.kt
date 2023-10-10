package com.example.task.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.task.MyApplication
import com.example.task.adapter.UserAdapter
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

    // Variable to store the matched user's isConnect status
    private val isUserConnected = MutableLiveData<String>()

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
            usersRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val users = mutableListOf<User>()

                    for (snapshot in dataSnapshot.children) {
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

    fun connectUser(user: List<User>, position: Int) {
        //get current userId
        var uid: String? = null
        val currentUser = Firebase.auth.currentUser
        currentUser?.let {
            uid = it.uid
        }

        // Firebase database reference
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("Users")

        // Current User
        val currentUserRef = usersRef.child(uid.toString())

        // Other user whose connected user ID will be obtained
        val otherUserRef = usersRef.child(user[position].userId)

        // Connection operations for current user
        currentUserRef.child("isConnected").setValue(true)
        currentUserRef.child("connectedUserId").setValue(otherUserRef.key)

        // Update the information of the person the connected user is connected to
        otherUserRef.child("isConnected").setValue(true)
        otherUserRef.child("connectedUserId").setValue(currentUserRef.key)

        // Show successful message
        MyApplication.showToast("Connected")

        // Update connectedUser when connecting occurs
        user[position].isConnected = true

        // hasDisconnectedOnce is reset when the connect operation occurs
        user[position].hasDisconnectedOnce = false
    }

    fun disconnectUser(user: List<User>, position: Int) {
        //get current userId
        var uid: String? = null
        val currentUser = Firebase.auth.currentUser
        currentUser?.let {
            uid = it.uid
        }

        // Firebase database reference
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("Users")

        // User to disconnect to
        val currentUserRef = usersRef.child(uid.toString())

        // Other user whose disconnected user ID will be obtained
        val otherUserRef = usersRef.child(user[position].userId)

        // Disconnection operations
        currentUserRef.child("isConnected").setValue(false)
        currentUserRef.child("connectedUserId").setValue(null)

        // Update the information of the person the connected user is disconnected to
        otherUserRef.child("isConnected").setValue(false)
        otherUserRef.child("connectedUserId").setValue(null)

        // Show successful message
        MyApplication.showToast("Disconnected")

        // Update connectedUser when disconnecting occurs
        user[position].isConnected = false

        // hasDisconnectedOnce is reset when the disconnect operation occurs
        user[position].hasDisconnectedOnce = true
    }

    fun followUser(user: User, position: Int) { }

    fun getIsConnectedStatus(callback: UserAdapter.IsConnectedCallback) {
        //get current userId
        var uid: String? = null
        val user = Firebase.auth.currentUser
        user?.let {
            uid = it.uid
        }

        val connectedUserIdRef = usersRef.child(uid!!).child("isConnected")
        val connectedUserIdListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                setIsConnectedStatus(dataSnapshot.value.toString())
                callback.onIsCurrentUserConnectedFetched(dataSnapshot.value.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Connected User Id Error", databaseError.details)
            }
        }

        connectedUserIdRef.addValueEventListener(connectedUserIdListener)
    }

    private fun setIsConnectedStatus(isConnected: String) {
        isUserConnected.value = isConnected
    }

}