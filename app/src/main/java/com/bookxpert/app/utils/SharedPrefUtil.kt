package com.bookxpert.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.bookxpert.app.constants.AppConstants

class SharedPrefUtil(context: Context) {

    val TAG = SharedPrefUtil::class.java.simpleName


    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(AppConstants.PREFS_KEY_NAME, Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    private val onBoardScreenPreferences: SharedPreferences =
        context.getSharedPreferences(AppConstants.PREFS_KEY_NAME, Context.MODE_PRIVATE)
    val onBoardEditor: SharedPreferences.Editor = onBoardScreenPreferences.edit()


    companion object {
        private var instance: SharedPrefUtil? = null

        fun getInstance(ctx: Context): SharedPrefUtil {
            if (instance == null) {
                instance = SharedPrefUtil(ctx)
            }
            return instance!!
        }
    }

    var firstLaunch: Boolean
        get() = sharedPreferences[AppConstants.FIRST_LAUNCH, false]!!
        set(value) = sharedPreferences.set(AppConstants.FIRST_LAUNCH, value)

    operator fun SharedPreferences.set(key: String, value: Any?) {
        when (value) {
            is String? -> edit { it.putString(key, value) }
            is Int -> edit { it.putInt(key, value) }
            is Boolean -> edit { it.putBoolean(key, value) }
            is Float -> edit { it.putFloat(key, value) }
            is Long -> edit { it.putLong(key, value) }
            else -> {}
        }
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    inline operator fun <reified T : Any> SharedPreferences.get(
        key: String,
        defaultValue: T? = null
    ): T? {
        return when (T::class) {
            String::class -> getString(key, defaultValue as? String) as T?
            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
            Long::class -> getLong(key, defaultValue as? Long ?: -1) as T?
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

}