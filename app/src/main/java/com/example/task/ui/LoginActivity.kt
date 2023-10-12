package com.example.task.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.example.task.databinding.ActivityLoginBinding
import com.example.task.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.loginToolbar)

        // Initialize the Firebase authentication instance
        auth = FirebaseAuth.getInstance()

        // Set ViewModel and lifecycle owner for binding in the layout
        binding.viewModel = loginViewModel
        binding.lifecycleOwner = this

        // Update progress bar visibility with LiveData
        loginViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Check Activity completion by observing LiveData
        loginViewModel.shouldFinishActivity.observe(this) { shouldFinish ->
            if (shouldFinish) {
                // Finish Activity
                finish()
                // Reset request in ViewModel
                loginViewModel.onActivityFinished()
            }
        }
    }

    // This function is called when the activity is about to become visible
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and open MainActivity() accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}