package com.csakitheone.streetmusic.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "settings")

class DataStore {
    companion object {

        @Composable
        fun <T> getState(key: Preferences.Key<T>, defaultValue: T): State<T> {
            return LocalContext.current.dataStore.data.map {
                it[key] ?: defaultValue
            }.collectAsState(initial = defaultValue)
        }

        fun <T> setValue(context: Context, key: Preferences.Key<T>, value: T) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit {
                    it[key] = value
                }
            }
        }

        val favoriteEventsKey = stringSetPreferencesKey("favorite_events")
        val favoriteMusiciansKey = stringSetPreferencesKey("favorite_musicians")

    }
}