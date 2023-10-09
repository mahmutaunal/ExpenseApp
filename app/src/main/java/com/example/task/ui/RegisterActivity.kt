package com.example.task.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.task.databinding.ActivityRegisterBinding
import com.example.task.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.registerToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener { controlEditText() }
    }

    private fun controlEditText() {
        binding.progressBar.visibility = View.VISIBLE

        if (binding.etRegisterEmail.text!!.equals("")) {
            binding.etRegisterEmailLayout.error = "Please enter email."
            binding.progressBar.visibility = View.GONE
        } else if (binding.etRegisterPassword.text!!.equals("")) {
            binding.etRegisterPasswordLayout.error = "Please enter password."
            binding.progressBar.visibility = View.GONE
        } else {
            register()
        }
    }

    private fun register() {
        auth.createUserWithEmailAndPassword(
            binding.etRegisterEmail.text.toString(),
            binding.etRegisterPassword.text.toString()
        )
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    binding.progressBar.visibility = View.GONE

                    // User successfully registered, add information to Firebase Realtime Database
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val userRef =
                        FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
                    val user = User(userId, binding.etRegisterEmail.text.toString(), false, null)
                    userRef.setValue(user)

                    // Sign in success
                    Toast.makeText(
                        baseContext,
                        "Account created.",
                        Toast.LENGTH_SHORT,
                    ).show()

                    // Start MainActivity
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    binding.progressBar.visibility = View.GONE

                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}