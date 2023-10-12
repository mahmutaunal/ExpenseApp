package com.example.task.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.task.MyApplication
import com.example.task.adapter.UserAdapter
import com.example.task.model.User
import com.example.task.model.notification.NotificationData
import com.example.task.model.notification.PushNotification
import com.example.task.util.RetrofitInstance
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserListViewModel : ViewModel() {

    // LiveData for the list of users
    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>>
        get() = _userList

    // Variable to store the matched user's isConnect status
    private val isUserConnected = MutableLiveData<String>()

    // LiveData for the refreshing state
    private val _refreshing = MutableLiveData<Boolean>()
    val refreshing: LiveData<Boolean>
        get() = _refreshing

    // LiveData for the loading state
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    // LiveData for the message to be shown if the expense list is empty
    private val _data: MutableLiveData<String> = MutableLiveData()
    val data: LiveData<String> get() = _data

    // Firebase database reference for users
    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")

    init {
        loadUsers()
    }

    // Load users from Firebase and update the user list LiveData
    private fun loadUsers() {
        _isLoading.value = true

        // Get current userId
        var uid: String? = null
        val user = Firebase.auth.currentUser
        user?.let {
            uid = it.uid
        }

        // Get all registered users data from Firebase
        viewModelScope.launch {
            _isLoading.value = false

            usersRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val users = mutableListOf<User>()

                    for (snapshot in dataSnapshot.children) {
                        val userId = snapshot.key

                        if (userId != null) {
                            if (userId != uid) {
                                val user = snapshot.getValue(User::class.java)
                                if (user == null) {
                                    _data.value = "There is no user to list!"
                                } else {
                                    user.let {
                                        users.add(it)
                                        _data.value = ""
                                    }
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

    // Functions to handle connecting
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
    }

    // Functions to handle disconnecting
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
    }

    // Functions to handle following
    fun followUser(user: List<User>, position: Int) {
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

        // Other user whose followed user ID will be obtained
        val otherUserRef = usersRef.child(user[position].userId)

        // Follow operations for current user
        currentUserRef.child("isFollowing").setValue(true)
        currentUserRef.child("followingUserId").setValue(otherUserRef.key)

        // Update the information of the person the following user is followed to
        otherUserRef.child("isFollowing").setValue(true)
        otherUserRef.child("followingUserId").setValue(currentUserRef.key)

        // Show successful message
        MyApplication.showToast("Being followed")

        // Update followedUser when connecting occurs
        user[position].isFollowing = true
    }

    // Functions to handle unfollowing
    fun unFollowUser(user: List<User>, position: Int) {
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

        // Other user whose unFollow user ID will be obtained
        val otherUserRef = usersRef.child(user[position].userId)

        // unFollowed operations for current user
        currentUserRef.child("isFollowing").setValue(false)
        currentUserRef.child("followingUserId").setValue(otherUserRef.key)

        // Update the information of the person the unFollowing user is followed to
        otherUserRef.child("isFollowing").setValue(false)
        otherUserRef.child("followingUserId").setValue(currentUserRef.key)

        // Show successful message
        MyApplication.showToast("Being followed")

        // Update followedUser when connecting occurs
        user[position].isFollowing = false
    }

    // Functions to handle getting the user's connected status and sending push notifications
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

    fun onRefresh() {
        _refreshing.value = true

        // Simulate a delay and then stop the refreshing animation
        Handler(Looper.getMainLooper()).postDelayed({
            _refreshing.value = false
        }, 2000)
    }

    fun sendPushNotification() {
        PushNotification(
            NotificationData("Task", "User wants to follow you."),
            "/topics/myTopic"
        ).also {
            sendNotification(it)
        }
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.d("Notification", "Response: $response")
                } else {
                    Log.e("Notification Error", response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e("Notification Error", e.toString())
            }
        }

}