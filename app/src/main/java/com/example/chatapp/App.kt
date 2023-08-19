package com.example.chatapp

import android.app.Application
import android.content.SharedPreferences
import com.example.chatapp.util.PreferenceManager

class App : Application() {

    private lateinit var preferences: SharedPreferences

    companion object {
        lateinit var prefs: PreferenceManager
    }

    override fun onCreate() {
        preferences = applicationContext.getSharedPreferences("settings", MODE_PRIVATE)
        prefs = PreferenceManager(preferences)
        super.onCreate()
    }
}