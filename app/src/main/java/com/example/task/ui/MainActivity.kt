package com.example.task.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.task.R
import com.example.task.databinding.ActivityMainBinding
import com.example.task.ui.fragments.ExpenseAddFragment
import com.example.task.ui.fragments.ExpenseListFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mainToolbar)

        setupListExpenseFragment()
        setupAddExpenseButton()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout_item) {
            logout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()

        Toast.makeText(applicationContext, "Logged Out", Toast.LENGTH_SHORT).show()

        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupListExpenseFragment() {
        val expenseListFragment = ExpenseListFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, expenseListFragment)
            .commit()
    }

    private fun setupAddExpenseButton() {
        binding.fabAdd.setOnClickListener {
            val expenseAddFragment = ExpenseAddFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, expenseAddFragment)
                .addToBackStack(null)
                .commit()

            binding.fabAdd.visibility = View.GONE
        }
    }
}