package com.example.task.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.task.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.registerToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("Users")

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
                    saveUserData(userId!!, binding.etRegisterEmail.text.toString(), false, "", true)

                    // Sign in success message
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

    private fun saveUserData(userId: String, email: String, isConnected: Boolean, connectedUserId: String?, hasDisconnectedOnce: Boolean) {
        val user = HashMap<String, Any>()
        user["userId"] = userId
        user["email"] = email
        user["isConnected"] = isConnected
        user["connectedUserId"] = connectedUserId.orEmpty()
        user["hasDisconnectedOnce"] = hasDisconnectedOnce

        usersRef.child(userId).setValue(user)
    }
}