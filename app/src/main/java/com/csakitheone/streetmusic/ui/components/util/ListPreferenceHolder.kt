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
import java.lang.ClassCastException
import java.lang.reflect.Type

/**
 * A composable function that can load and save a primitive or (some) complex value.
 *
 * @param <T> The type of value held by the preference. Supported data types include: Int, Long, Float, Boolean and String. You can try anything else, but it's not guaranteed to work.
 * @param id The identifier for the preference holder. This will be the key that will be used in shared preferences.
 * @param value The value of the preference. Setting this will automatically save the value.
 * @param onValueChanged A callback function invoked when the value is loaded or changed.
 */
@Composable
fun <T> ListPreferenceHolder(
    id: String,
    value: Collection<T>,
    onValueChanged: (Collection<T>) -> Unit,
    type: Type,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefs by remember { mutableStateOf(PreferenceManager.getDefaultSharedPreferences(context)) }
    var isFirstCallOver by remember { mutableStateOf(false) }

    fun onChange() {
        try {
            val initialValue = prefs.getStringSet(id, setOf())
                ?.map { Gson().fromJson(it, type) as T }
                ?.toList() ?: listOf()
            onValueChanged(initialValue)
        }
        catch (ex: Exception) {
            prefs.edit {
                remove(id)
                commit()
            }
            onValueChanged(listOf())
        }
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
            val strings = value.map { Gson().toJson(it) }.toSet()
            putStringSet(id, strings)
            commit()
        }
        PreferenceHolderSyncManager.onChanged(id)
    }
}