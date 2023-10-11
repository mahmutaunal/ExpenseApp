package com.example.task.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.task.R
import com.example.task.databinding.ActivityMainBinding
import com.example.task.ui.fragments.ExpenseListFragment
import com.example.task.ui.fragments.UserListFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mainToolbar)

        setupListExpenseFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout_item) {
            showLogoutConfirmationDialog()
        } else if (item.itemId == R.id.users_item) {
            openUsersFragment()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupListExpenseFragment() {
        val expenseListFragment = ExpenseListFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, expenseListFragment)
            .commit()
    }

    private fun openUsersFragment() {
        val userListFragment = UserListFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, userListFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Logout") { _, _ ->
                // Actions when the user clicks Yes
                logout()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Actions to take when the user clicks Cancel
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()

        Toast.makeText(applicationContext, "Logged Out", Toast.LENGTH_SHORT).show()

        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}