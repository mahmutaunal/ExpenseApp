package com.example.task.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.task.MyApplication
import com.example.task.service.FirebaseService
import com.example.task.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    // LiveData for error message
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    // LiveData loading indicator
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    // LiveData for activity termination request
    private val _shouldFinishActivity = MutableLiveData<Boolean>()
    val shouldFinishActivity: LiveData<Boolean>
        get() = _shouldFinishActivity

    // Variables for email, password
    var email: String = ""
    var password: String = ""

    // Variables for recipient token
    private var recipientToken: String = ""

    // Firebase database references
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    // Function to handle register click
    fun onRegisterClick(context: Context) {
        // If email or password is null, warn the user and do not perform the action
        if (email.isEmpty() || password.isEmpty()) {
            setErrorMessage("Email and Password are required.")
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Await for getToken to complete
                setToken(context)

                // This part will be executed after getToken has completed
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        _isLoading.value = false

                        if (task.isSuccessful) {
                            // Registration successful, start MainActivity
                            // User successfully registered, add information to Firebase Realtime Database
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            saveUserData(userId!!, email, recipientToken, false, "", false, "")

                            MyApplication.showToast("Account created.")

                            val intent =
                                Intent(context.applicationContext, MainActivity::class.java)
                            context.startActivity(intent)
                            requestFinishActivity()
                        } else {
                            // Registration failed.
                            MyApplication.showToast("Account creating failed.")
                        }
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                MyApplication.showToast("Token retrieval failed.")
            }
        }
    }

    // Function to save user data to Firebase Realtime Database
    private fun saveUserData(
        userId: String,
        email: String,
        token: String,
        isConnected: Boolean,
        connectedUserId: String?,
        isFollowing: Boolean,
        followingUserId: String?
    ) {
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("Users")

        val user = HashMap<String, Any>()
        user["userId"] = userId
        user["email"] = email
        user["token"] = token
        user["isConnected"] = isConnected
        user["connectedUserId"] = connectedUserId.orEmpty()
        user["isFollowing"] = isFollowing
        user["followingUserId"] = followingUserId.orEmpty()

        usersRef.child(userId).setValue(user)
    }

    // Function to set the recipient token
    private fun setToken(context: Context) {
        FirebaseService.sharedPref =
            context.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            FirebaseService.token = token
            recipientToken = token

            Log.d("TokenDebug", "Token obtained: $token")
        }
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/myTopic")
    }

    // Functions to handle error message
    private fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

    // When this method is called, request that the Activity be terminated.
    private fun requestFinishActivity() {
        _shouldFinishActivity.value = true
    }

    // When the Activity's termination request is completed, reset LiveData.
    fun onActivityFinished() {
        _shouldFinishActivity.value = false
    }
}