package com.ayala.dam.firebasenotesfragments

import android.content.Context
import android.content.SharedPreferences

class Prefs (context: Context) {
    val PREFS_NAME = "com.ayala.dam.firebasenotesfragments"
    val SHARED_NAME = "shared_name"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, 0)

    var token: String
        get() = prefs.getString(SHARED_NAME, "")
        set(value) = prefs.edit().putString(SHARED_NAME, value).apply()
}