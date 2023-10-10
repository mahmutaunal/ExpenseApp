package com.example.task.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.example.task.databinding.ActivityRegisterBinding
import com.example.task.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.registerToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.viewModel = registerViewModel
        binding.lifecycleOwner = this

        // Update progress bar visibility with LiveData
        registerViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Check Activity completion by observing LiveData
        registerViewModel.shouldFinishActivity.observe(this) { shouldFinish ->
            if (shouldFinish) {
                // Finish Activity
                finish()
                // Reset request in ViewModel
                registerViewModel.onActivityFinished()
            }
        }
    }
}