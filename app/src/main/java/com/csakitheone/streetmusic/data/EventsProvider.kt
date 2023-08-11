package com.csakitheone.streetmusic.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.preference.PreferenceManager
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.model.Place
import com.csakitheone.streetmusic.util.Helper

class EventsProvider {
    companion object {

        private val customMusicians = listOf(
            Musician(
                name = "Shántolók",
                description = "Először a Lovassy Alma Mater rendezvényén hallottam " +
                        "sea shanty-ket énekelni az urakat ahol is egyértelműen megnyerték a " +
                        "közönséget. Ez abból is látszott, hogy egy daluk után két hölgy a " +
                        "nézőtéren felállva egy nagy szivet mutatott a fiúknak. Egy amúgy is " +
                        "szórakoztató műfajt adnak elő, néha hangszerkísérettel. Aki kedveli a " +
                        "sea shanty-ket, mindenképp meg kell hallgatnia őket!",
                country = "HU",
                imageUrl = "https://i.ytimg.com/vi/grWgky_ZIJw/maxresdefault.jpg",
                youtubeUrl = "https://youtu.be/grWgky_ZIJw",
                tags = listOf(Musician.TAG_FRIEND),
            )
        )

        val STATE_DOWNLOADING = R.string.data_state_downloading
        val STATE_DOWNLOADED = R.string.data_state_downloaded
        val STATE_DOWNLOAD_ERROR = R.string.data_state_download_error
        val STATE_PREFERENCES = R.string.data_state_preferences
        val STATE_APP = R.string.data_state_app
        val STATE_UNKNOWN = R.string.data_state_unknown

        var state by mutableStateOf(STATE_UNKNOWN)

        val customEvents = listOf(
            Event(
                musician = customMusicians[0],
                day = 19,
                time = "17:00",
                place = Place("Óváros tér"),
            )
        )

        /**
         * Gets the events in this order:
         * 1. If there is an unmetered network connection and downloaded events are old, downloads it from the API.
         * 2. If there is no network, but events were downloaded once, reads it from preferences.
         * 3. If there are no events in the preferences, reads it from the CSV.
         */
        fun getEvents(
            context: Context,
            forceDownload: Boolean = false,
            skipDownload: Boolean = false,
            callback: (List<Event>) -> Unit = {}
        ) {
            state = STATE_UNKNOWN
            callback(listOf())
            return
            // 1.
            val timeTillOld = 1000L * 60 * 30
            val isDataOld = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getLong(
                    UzApi.PREF_KEY_API_LAST_DOWNLOAD_TIMESTAMP,
                    0L
                ) + timeTillOld < System.currentTimeMillis()
            if (
                forceDownload ||
                (
                        isDataOld &&
                                Helper.isUnmeteredNetworkAvailable(context) &&
                                !skipDownload
                        )
            ) {
                state = STATE_DOWNLOADING
                UzApi.downloadEvents(context) {
                    if (it.isNotEmpty()) {
                        callback(it + customEvents)
                        state = STATE_DOWNLOADED
                    } else {
                        state = STATE_DOWNLOAD_ERROR
                        getEvents(context, skipDownload = true, callback = callback)
                    }
                }
                return
            }
            // 2.
            val json = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(UzApi.PREF_KEY_API_RESPONSE_ARTISTS, "")
            if (!json.isNullOrBlank()) {
                callback(UzApi.artistsJsonToEvents(json) + customEvents)
                state = STATE_PREFERENCES
                return
            }
            // 3.
            callback(customEvents)
            state = STATE_APP
        }

    }
}