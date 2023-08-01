package com.valorem.flobooks.showcaselib

import android.content.Context
import androidx.core.content.edit

internal class ShowcasePrefs(context: Context) {
    private val prefs by lazy {
        context.getSharedPreferences("showcase_pref", Context.MODE_PRIVATE)
    }

    fun setShown(key: String) = prefs.edit { putBoolean(key, true) }

    fun canShow(key: String): Boolean = !prefs.getBoolean(key, false)

    fun clearAll() = prefs.edit { clear() }
}