package de.leuchtetgruen.pluslocation.helpers

import android.content.Context

object SavedCode {
    private val CODE = "code"
    private val NAME = "name"
    private val PREFERENCE_FILE = "codestorage"

    fun changedCode(code : String, name : String, context : Context) {
        val editor = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE).edit()

        editor.putString(CODE, code)
        editor.putString(NAME, name)
        editor.apply()
    }

    fun savedCode(context : Context) : String {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        return sharedPreferences.getString(CODE, Constants.TV_TOWER_BLN_CODE)
    }

    fun savedName(context : Context) : String {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        return sharedPreferences.getString(NAME, "")
    }
}