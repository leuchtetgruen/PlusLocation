package de.leuchtetgruen.pluslocation.persistence

import android.content.Context
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.center
import de.leuchtetgruen.pluslocation.helpers.Constants

object SavedCode {
    private const val CODE = "code"
    private const val NAME = "name"
    private const val PREFERENCE_FILE = "codestorage"

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

    fun savedLocation(context : Context) : WGS84Coordinates = OpenLocationCode(savedCode(context)).decode().center()

    fun savedName(context : Context) : String {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        return sharedPreferences.getString(NAME, Constants.TV_TOWER_BLN_NAME)
    }
}