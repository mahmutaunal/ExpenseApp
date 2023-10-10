package com.example.task.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.task.MyApplication
import com.example.task.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterViewModel : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _shouldFinishActivity = MutableLiveData<Boolean>()
    val shouldFinishActivity: LiveData<Boolean>
        get() = _shouldFinishActivity

    var email: String = ""
    var password: String = ""

    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    fun onRegisterClick(context: Context) {
        // If email or password is null, warn the user and do not perform the action
        if (email.isEmpty() || password.isEmpty()) {
            setErrorMessage("Email and Password are required.")
            return
        }

        _isLoading.value = true

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false

                if (task.isSuccessful) {
                    // Registration successful, start MainActivity
                    // User successfully registered, add information to Firebase Realtime Database
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    saveUserData(userId!!, email, false, "")

                    MyApplication.showToast("Account created.")

                    val intent = Intent(context.applicationContext, MainActivity::class.java)
                    context.startActivity(intent)
                    requestFinishActivity()
                } else {
                    // Registration failed.
                    MyApplication.showToast("Account creating failed.")
                }
            }
    }

    private fun saveUserData(
        userId: String,
        email: String,
        isConnected: Boolean,
        connectedUserId: String?
    ) {
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("Users")

        val user = HashMap<String, Any>()
        user["userId"] = userId
        user["email"] = email
        user["isConnected"] = isConnected
        user["connectedUserId"] = connectedUserId.orEmpty()

        usersRef.child(userId).setValue(user)
    }

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