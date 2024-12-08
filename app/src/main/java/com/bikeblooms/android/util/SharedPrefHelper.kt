package com.bikeblooms.android.util

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class SharedPrefHelper(var sharedPrefs: SharedPreferences) {

    fun saveBool(key: String, value: Boolean) {
        with(sharedPrefs.edit()) {
            putBoolean(key, value)
        }.apply()
    }

    fun getBool(key: String): Boolean {
        return sharedPrefs.getBoolean(key, false)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        saveBool("isLoginned", isLoggedIn)
    }

    fun isLoggedIn(): Boolean {
        return getBool("isLoginned")
    }
}