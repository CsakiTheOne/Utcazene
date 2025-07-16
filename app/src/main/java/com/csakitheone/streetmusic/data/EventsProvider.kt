package com.csakitheone.streetmusic.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.model.Place
import com.csakitheone.streetmusic.util.Helper
import com.google.gson.Gson
import java.io.File
import java.time.LocalDate

class EventsProvider {
    companion object {

        private val customMusicians: List<Musician> = listOf(
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
                youtubeUrl = "https://www.youtube.com/@shantolok_band",
                tags = listOf(Musician.TAG_FRIEND),
            ),
        )

        val STATE_DOWNLOADING = R.string.data_state_downloading
        val STATE_DOWNLOADED = R.string.data_state_downloaded
        val STATE_DOWNLOAD_ERROR = R.string.data_state_download_error
        val STATE_CACHE = R.string.data_state_cache
        val STATE_APP = R.string.data_state_app
        val STATE_UNKNOWN = R.string.data_state_unknown

        var state by mutableStateOf(STATE_UNKNOWN)

        val customEvents: List<Event> = listOf(
            Event(
                id = 10_000,
                musician = customMusicians[0],
                year = 2025,
                day = 17,
                time = "20:00",
                place = Place("Szabadság tér"),
            ),
            Event(
                id = 10_001,
                musician = customMusicians[0],
                year = 2025,
                day = 18,
                time = "19:00",
                place = Place("Deutsches Haus, Thököly u. 11."),
            ),
        )

        private fun saveCache(context: Context, events: List<Event>) {
            val cacheFile = File(context.cacheDir, "events.json")
            val json = Gson().toJson(events)
            cacheFile.writeText(json)
        }

        private fun loadCache(context: Context): List<Event> {
            val cacheFile = File(context.cacheDir, "events.json")
            if (!cacheFile.exists()) return listOf()
            val json = cacheFile.readText()
            return Gson().fromJson(json, Array<Event>::class.java).toList()
        }

        /**
         * Gets the events in this order:
         * 1. If there is an unmetered network connection downloads the events from the API.
         * 2. If there is no network, but events were downloaded once, reads it from cache.
         * 3. If there are no events in cache, returns the custom events.
         */
        fun getEventsThisYear(
            context: Context,
            forceDownload: Boolean = false,
            skipDownload: Boolean = false,
            callback: (List<Event>) -> Unit = {}
        ) {
            state = STATE_UNKNOWN
            callback(listOf())
            // 1.
            if (
                forceDownload ||
                (Helper.isUnmeteredNetworkAvailable(context) && !skipDownload)
            ) {
                state = STATE_DOWNLOADING
                UzApi.downloadEvents(context) { events ->
                    val eventsThisYear = events.filter { it.year == LocalDate.now().year }
                    if (eventsThisYear.isNotEmpty()) {
                        callback(eventsThisYear + customEvents)
                        saveCache(context, events)
                        state = STATE_DOWNLOADED
                    } else {
                        state = STATE_DOWNLOAD_ERROR
                        getEventsThisYear(context, skipDownload = true, callback = callback)
                    }
                }
                return
            }
            // 2.
            val cachedEvents = loadCache(context)
            if (cachedEvents.isNotEmpty()) {
                state = STATE_CACHE
                callback(cachedEvents + customEvents)
                return
            }
            // 3.
            state = STATE_APP
            callback(customEvents)
        }

    }
}