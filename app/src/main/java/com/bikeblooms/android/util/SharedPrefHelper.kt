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


    fun saveString(key: String, value: String) {
        with(sharedPrefs.edit()) {
            putString(key, value)
        }.apply()
    }

    fun getBool(key: String): Boolean {
        return sharedPrefs.getBoolean(key, false)
    }

    fun getString(key: String): String {
        return sharedPrefs.getString(key, "").toString()
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        saveBool("isLoginned", isLoggedIn)
    }

    fun isLoggedIn(): Boolean {
        return getBool("isLoginned")
    }

    fun setFirebaseUserID(fireBaseUserId: String) {
        saveString("fireBaseUserId", fireBaseUserId)
    }

    fun getUserFirebaseId(): String {
        return getString("fireBaseUserId")
    }

    fun clearAll() {
        sharedPrefs.edit().clear().apply()
    }
}