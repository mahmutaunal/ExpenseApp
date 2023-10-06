package com.example.task

import android.app.Application
import android.content.Context
import android.widget.Toast

class MyApplication : Application() {

    companion object {
        private var instance: MyApplication? = null

        private fun applicationContext(): Context {
            return instance!!.applicationContext
        }

        fun showToast(message: String) {
            Toast.makeText(applicationContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}