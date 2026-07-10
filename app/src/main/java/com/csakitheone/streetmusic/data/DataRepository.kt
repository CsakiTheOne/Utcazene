package com.csakitheone.streetmusic.data

import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.edit
import com.csakitheone.streetmusic.data.api.UtcazeneApi
import com.csakitheone.streetmusic.data.local.AppDatabase
import com.csakitheone.streetmusic.data.local.ArtistEntity
import com.csakitheone.streetmusic.data.local.EventEntity
import com.csakitheone.streetmusic.data.model.Artist
import com.csakitheone.streetmusic.data.model.Event
import com.csakitheone.streetmusic.data.nearby.NearbyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

val LocalRepository = staticCompositionLocalOf<DataRepository> {
    error("No DataRepository provided")
}

class DataRepository(
    private val api: UtcazeneApi,
    private val database: AppDatabase,
    private val connectivityManager: ConnectivityManager,
    private val prefs: SharedPreferences,
    val nearbyManager: NearbyManager
) {

    private val _nickname = MutableStateFlow(prefs.getString("nickname", "Friend") ?: "Friend")
    val nickname: StateFlow<String> = _nickname.asStateFlow()
    fun setNickname(value: String) {
        prefs.edit { putString("nickname", value) }
        _nickname.value = value
        nearbyManager.updateLocalNickname(value)
    }

    private val _nearbyFeatures = MutableStateFlow(prefs.getBoolean("nearby_features", false))
    val nearbyFeatures: StateFlow<Boolean> = _nearbyFeatures.asStateFlow()
    fun setNearbyFeatures(value: Boolean) {
        prefs.edit { putBoolean("nearby_features", value) }
        _nearbyFeatures.value = value
        nearbyManager.setNearbyActive(value)
    }

    private val _showImagesOnMetered =
        MutableStateFlow(prefs.getBoolean("show_images_on_metered", false))
    val showImagesOnMetered: StateFlow<Boolean> = _showImagesOnMetered.asStateFlow()
    fun setShowImagesOnMetered(value: Boolean) {
        prefs.edit { putBoolean("show_images_on_metered", value) }
        _showImagesOnMetered.value = value
    }

    fun shouldShowImage(): Boolean {
        return !connectivityManager.isActiveNetworkMetered || _showImagesOnMetered.value
    }

    private val _userFavorites =
        MutableStateFlow(prefs.getStringSet("fav_slugs", emptySet()) ?: emptySet())
    val userFavorites: StateFlow<Set<String>> = _userFavorites.asStateFlow()

    /**
     * User's favorite items + favorites from nearby devices
     */
    val allFavorites: Flow<Set<String>> = combine(
        userFavorites,
        nearbyManager.friends.nearbyFavorites
    ) { local, nearby ->
        local + nearby.values.flatten()
    }

    fun setFavorite(slug: String, value: Boolean) {
        val current = _userFavorites.value.toMutableSet()
        if (value) current.add(slug) else current.remove(slug)

        prefs.edit { putStringSet("fav_slugs", current) }
        _userFavorites.value = current
        nearbyManager.updateLocalFavorites(current)
    }

    fun toggleFavorite(slug: String) {
        val current = _userFavorites.value.toMutableSet()
        if (current.contains(slug)) current.remove(slug) else current.add(slug)

        prefs.edit { putStringSet("fav_slugs", current) }
        _userFavorites.value = current
        nearbyManager.updateLocalFavorites(current)
    }

    fun clearFavorites() {
        prefs.edit { putStringSet("fav_slugs", emptySet()) }
        _userFavorites.value = emptySet()
        nearbyManager.updateLocalFavorites(emptySet())
    }

    init {
        nearbyManager.setNearbyActive(_nearbyFeatures.value)
        nearbyManager.updateLocalFavorites(_userFavorites.value)
        nearbyManager.updateLocalNickname(_nickname.value)
    }

    var isDownloading by mutableStateOf(false)
        private set

    enum class DownloadResult {
        SUCCESS, NO_CONNECTION, ALREADY_HAS_DATA, ASK_FOR_METERED
    }

    val artists: Flow<List<Artist>> =
        database.artistDao().getAll().combine(userFavorites) { entities, favs ->
            entities.map {
                Artist(
                    it.id, it.name, it.country, it.description, it.image, it.slug,
                    it.youtubeEmbed, it.tags.split(",").filter { tag -> tag.isNotBlank() },
                    isStarred = favs.contains(it.slug)
                )
            }.sortedBy { it.name }
        }

    val events: Flow<List<Event>> =
        database.eventDao().getAll().combine(userFavorites) { entities, favs ->
            entities.map {
                Event(
                    it.id, it.artistId, it.artistSlug, it.artistName,
                    it.startTime, it.endTime, it.place,
                    isStarred = favs.contains("${it.artistSlug} at ${it.startTime}")
                )
            }
        }

    val eventDates: Flow<List<String>> = database.eventDao().getDistinctDates()

    fun getArtist(slug: String): Flow<Artist?> =
        database.artistDao().getBySlug(slug).combine(userFavorites) { entity, favs ->
            entity?.let {
                Artist(
                    it.id, it.name, it.country, it.description, it.image, it.slug,
                    it.youtubeEmbed, it.tags.split(",").filter { tag -> tag.isNotBlank() },
                    isStarred = favs.contains(it.slug)
                )
            }
        }

    fun getEvent(id: Int): Flow<Event?> =
        database.eventDao().getById(id).combine(userFavorites) { entity, favs ->
            entity?.let {
                Event(
                    it.id, it.artistId, it.artistSlug, it.artistName,
                    it.startTime, it.endTime, it.place,
                    isStarred = favs.contains("${it.artistSlug} at ${it.startTime}")
                )
            }
        }

    fun getEventsByArtist(artistSlug: String): Flow<List<Event>> =
        database.eventDao().getEventsByArtist(artistSlug).combine(userFavorites) { entities, favs ->
            entities.map {
                Event(
                    it.id, it.artistId, it.artistSlug, it.artistName,
                    it.startTime, it.endTime, it.place,
                    isStarred = favs.contains("${it.artistSlug} at ${it.startTime}")
                )
            }
        }

    val hasData: Flow<Boolean> = database.artistDao().getCount()
        .combine(database.eventDao().getCount()) { artistCount, eventCount ->
            artistCount > 0 || eventCount > 0
        }

    /**
     * Tries to download the data from the API and store it in the local database.
     * This function checks network conditions and if the data is already in the database.
     * @param force If true, the data will be downloaded even if it is already in the database.
     * @return [DownloadResult] indicating the result of the attempt.
     */
    suspend fun tryDownloadData(force: Boolean = false): DownloadResult {
        val isMetered = connectivityManager.isActiveNetworkMetered
        val hasData =
            withContext(Dispatchers.IO) { database.eventDao().getCount().first() > 0 }

        if (connectivityManager.activeNetwork == null) return DownloadResult.NO_CONNECTION

        if (!isMetered) {
            downloadData()
            return DownloadResult.SUCCESS
        }

        if (hasData) {
            return DownloadResult.ALREADY_HAS_DATA
        }

        if (!force) {
            return DownloadResult.ASK_FOR_METERED
        }

        downloadData()
        return DownloadResult.SUCCESS
    }

    /**
     * Downloads the data from the API and stores it in the local database.
     */
    suspend fun downloadData() = withContext(Dispatchers.IO) {
        val yearFilter = 2026
        val emulateFirstDayOfEvent = false

        isDownloading = true

        val apiArtists = try {
            api.fetchArtists()
        } catch (e: Exception) {
            Log.e("DataRepository", "Error fetching artists: ${e.message}")
            emptyList()
        }

        if (apiArtists.isNotEmpty()) {
            database.artistDao().deleteAll()

            Log.d("DataRepository", "Processing ${apiArtists.size} artists...")

            val events = apiArtists.flatMap { artist ->
                val artistTags = mutableListOf<String>()
                if (artist.headliner) artistTags.add("headliner")
                else artistTags.add("competitor")

                val hasEventInFilteredYear = artist.timeslots.any { slot ->
                    val date = slot.eventStartTime ?: ""
                    date.startsWith(yearFilter.toString())
                } || artist.timeslots.isEmpty()
                if (!hasEventInFilteredYear) {
                    Log.d("DataRepository", "Skipping artist: ${artist.name}")
                    return@flatMap emptyList()
                }

                database.artistDao().insert(
                    ArtistEntity(
                        artist.id, artist.name, artist.country, artist.description,
                        artist.image, artist.slug, artist.youtubeEmbed,
                        artistTags.joinToString(",")
                    )
                )
                Log.d("DataRepository", "Added artist: ${artist.name}")

                artist.timeslots.mapNotNull { slot ->
                    val date = slot.eventStartTime ?: ""
                    if (!date.startsWith(yearFilter.toString())) return@mapNotNull null

                    EventEntity(
                        artistId = artist.id,
                        artistName = artist.name,
                        artistSlug = artist.slug,
                        place = slot.eventVenueName ?: "Unknown Venue",
                        startTime = "${date}T${slot.startTime}",
                        endTime = "${date}T${slot.endTime}"
                    )
                }
            }

            database.eventDao().deleteAll()
            if (events.isNotEmpty()) {
                if (emulateFirstDayOfEvent) {
                    val originalFirstDayOfEvent =
                        events.minOf { LocalDate.parse(it.startTime.substring(0, 10)).dayOfMonth }
                    val dayDifference = LocalDate.now().dayOfMonth - originalFirstDayOfEvent
                    database.eventDao().insertAll(events.map { event ->
                        val newDayOfMonth = event.startTime.substring(8, 10).toInt() + dayDifference
                        val newDate = "${LocalDate.now().year}-${
                            LocalDate.now().monthValue.toString().padStart(2, '0')
                        }-${
                            newDayOfMonth.toString().padStart(2, '0')
                        }"
                        event.copy(
                            startTime = "${newDate}T${event.startTime.substring(11)}",
                            endTime = "${newDate}T${event.endTime.substring(11)}"
                        )
                    })
                } else {
                    database.eventDao().insertAll(events)
                }
            }
        }
        isDownloading = false
    }

    fun deleteDatabase() {
        database.clearAllTables()
    }

    suspend fun syncData(artists: List<ArtistEntity>, events: List<EventEntity>) =
        withContext(Dispatchers.IO) {
            database.artistDao().deleteAll()
            database.artistDao().insertAll(artists)
            database.eventDao().deleteAll()
            database.eventDao().insertAll(events)
        }

    fun getAllArtistEntities(): Flow<List<ArtistEntity>> = database.artistDao().getAll()
    fun getAllEventEntities(): Flow<List<EventEntity>> = database.eventDao().getAll()
    fun getEventEntitiesByArtist(artistSlug: String): Flow<List<EventEntity>> =
        database.eventDao().getEventsByArtist(artistSlug)
}
