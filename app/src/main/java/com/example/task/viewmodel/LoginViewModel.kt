package com.example.task.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.task.MyApplication
import com.example.task.ui.MainActivity
import com.example.task.ui.RegisterActivity
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

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

    fun onLoginClick(context: Context) {
        // If email or password is null, warn the user and do not perform the action
        if (email.isEmpty() || password.isEmpty()) {
            setErrorMessage("Email and Password are required.")
            return
        }

        _isLoading.value = true

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false

                if (task.isSuccessful) {
                    // Login successful, start MainActivity
                    MyApplication.showToast("Login successful.")

                    val intent = Intent(context.applicationContext, MainActivity::class.java)
                    context.startActivity(intent)
                    requestFinishActivity()
                } else {
                    // Login failed
                    MyApplication.showToast("Authentication failed.")
                }
            }
    }

    // Open registerActivity
    fun onRegisterClick(context: Context) {
        val intent = Intent(context.applicationContext, RegisterActivity::class.java)
        context.startActivity(intent)
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