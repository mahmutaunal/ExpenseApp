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

        // This is a function to show a toast with the given message
        fun showToast(message: String) {
            Toast.makeText(applicationContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    // This is an override function that is called when the application is created
    override fun onCreate() {
        super.onCreate()
        // Set the instance to the current instance of MyApplication
        instance = this
    }

}