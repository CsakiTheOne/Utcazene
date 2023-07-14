package com.csakitheone.streetmusic.ui.components.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.csakitheone.streetmusic.data.PreferenceHolderSyncManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * A composable function that can load and save a primitive or (some) complex value.
 *
 * @param <T> The type of value held by the preference. Supported data types include: Int, Long, Float, Boolean and String. You can try anything else, but it's not guaranteed to work.
 * @param id The identifier for the preference holder. This will be the key that will be used in shared preferences.
 * @param value The value of the preference. Setting this will automatically save the value.
 * @param isValueChanged A callback function invoked when the value is loaded or changed.
 * @param defaultValue The default value of the preference.
 */
@Composable
fun <T> PreferenceHolder(
    id: String,
    value: T,
    isValueChanged: (T) -> Unit,
    defaultValue: T,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefs by remember { mutableStateOf(PreferenceManager.getDefaultSharedPreferences(context)) }
    var isFirstCallOver by remember { mutableStateOf(false) }

    fun onChange() {
        val initialValue: T = when (value) {
            is Int -> prefs.getInt(id, value)
            is Long -> prefs.getLong(id, value)
            is Float -> prefs.getFloat(id, value)
            is Boolean -> prefs.getBoolean(id, value)
            is String -> prefs.getString(id, value)
            else -> {
                val type = object: TypeToken<T>() {}.type
                Gson().fromJson(prefs.getString(id, ""), type)
            }
        } as T? ?: defaultValue
        isValueChanged(initialValue)
    }

    fun onSync(eventId: String) {
        if (id == eventId) onChange()
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            PreferenceHolderSyncManager.removeOnChangedListener { onSync(it) }
        }
    }

    LaunchedEffect(Unit) {
        PreferenceHolderSyncManager.addOnChangedListener { onSync(it) }
        onChange()
    }

    LaunchedEffect(value) {
        if (!isFirstCallOver) {
            isFirstCallOver = true
            return@LaunchedEffect
        }
        prefs.edit {
            when (value) {
                is Int -> putInt(id, value)
                is Long -> putLong(id, value)
                is Float -> putFloat(id, value)
                is Boolean -> putBoolean(id, value)
                is String -> putString(id, value)
                else -> putString(id, Gson().toJson(value))
            }
            commit()
        }
        PreferenceHolderSyncManager.onChanged(id)
    }
}