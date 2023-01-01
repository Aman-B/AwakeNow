package com.bewtechnologies.awakenow.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


object Util {

    fun saveInSharedPref(packageName: String, context: Context) {
        //Fetch array from shared pref
        val arrayList: ArrayList<String> = getListOfAppNamesFromSharedPref(context)

        //add [packageName] to the array
        arrayList.add(packageName)

        //save it again
        val sharedPrefs = context.getSharedPreferences("awakeNow", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        val gson = Gson()
        val json = gson.toJson(arrayList)

        editor.putString("appPackageNames", json)
        editor.apply()
    }

    fun removeFromSharedPref(packageName: String, context: Context) {
        //Fetch array from shared pref
        val arrayList: ArrayList<String> = getListOfAppNamesFromSharedPref(context)

        //remove [packageName] from the array if exists
        arrayList.remove(packageName)

        //save it again
        val sharedPrefs = context.getSharedPreferences("awakeNow", Activity.MODE_PRIVATE)
        val gson = Gson()
        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        val json = gson.toJson(arrayList)
        editor.putString("appPackageNames", json)
        //remove the keyword set with package name also.
        editor.putString(packageName, "")
        editor.apply()
    }

    fun getListOfAppNamesFromSharedPref(context: Context): ArrayList<String> {
        val sharedPrefs = context.getSharedPreferences("awakeNow", Activity.MODE_PRIVATE)
        val gson = Gson()
        val appNameJson = sharedPrefs.getString("appPackageNames", "")
        if (appNameJson != null && !appNameJson.isNullOrEmpty()) {
            val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
            return gson.fromJson(appNameJson, type)
        }
        return arrayListOf()
    }

    fun getTextToLookForFromSharedPref(packageName: String, context: Context): String? {
        val sharedPrefs = context.getSharedPreferences("awakeNow", Activity.MODE_PRIVATE)
        //return text to look for
        return sharedPrefs.getString(packageName, "")
    }

    fun putTextToLookFor(context: Context, appPackageName: String, textToLookFor: String) {
        //put textToLookFor with packageName as key
        val sharedPrefs = context.getSharedPreferences("awakeNow", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.putString(appPackageName, textToLookFor)
        editor.apply()
    }
}