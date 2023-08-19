package com.example.chatapp.util

import android.content.SharedPreferences

class PreferenceManager(private val sharedPreferences: SharedPreferences) {

    fun putBoolean(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor?.putBoolean(key, value)
        editor?.apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun putString(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor?.putString(key, value)
        editor?.apply()
    }

    fun getString(key: String): String {
        return sharedPreferences.getString(key, null).toString()
    }

    fun clear() {
        val editor = sharedPreferences.edit()
        editor?.clear()
        editor?.apply()
    }

    fun changePreference() {
        sharedPreferences.edit().putBoolean("isShow", true).apply()
    }


    fun isShow(): Boolean {
        return sharedPreferences.getBoolean("isShow", false)
    }
}