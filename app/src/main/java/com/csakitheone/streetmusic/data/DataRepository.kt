package com.csakitheone.streetmusic.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.PowerManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.csakitheone.streetmusic.data.api.UtcazeneApi
import com.csakitheone.streetmusic.data.api.WeatherApi
import com.csakitheone.streetmusic.data.local.AppDatabase
import com.csakitheone.streetmusic.data.local.ArtistEntity
import com.csakitheone.streetmusic.data.local.EventEntity
import com.csakitheone.streetmusic.data.local.VenueEntity
import com.csakitheone.streetmusic.data.model.Artist
import com.csakitheone.streetmusic.data.model.Event
import com.csakitheone.streetmusic.data.model.HourlyWeather
import com.csakitheone.streetmusic.data.model.Venue
import com.csakitheone.streetmusic.data.model.WeatherForecast
import com.csakitheone.streetmusic.data.nearby.NearbyManager
import com.csakitheone.streetmusic.notifications.AlarmScheduler
import com.csakitheone.streetmusic.ui.widgets.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

val LocalRepository = staticCompositionLocalOf<DataRepository> {
    error("No DataRepository provided")
}

class DataRepository(
    private val context: Context,
    private val api: UtcazeneApi,
    private val weatherApi: WeatherApi,
    private val database: AppDatabase,
    private val connectivityManager: ConnectivityManager,
    private val prefs: SharedPreferences,
    val nearbyManager: NearbyManager
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private val _isBatterySaverEnabled = MutableStateFlow(powerManager.isPowerSaveMode)
    val isBatterySaverEnabled: StateFlow<Boolean> = _isBatterySaverEnabled.asStateFlow()

    private val _batteryLevel = MutableStateFlow(-1)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level != -1 && scale != -1) {
                    val batteryPct = (level * 100 / scale.toFloat()).toInt()
                    _batteryLevel.value = batteryPct
                    nearbyManager.updateLocalBatteryLevel(batteryPct)
                }
            }
        }
    }

    private val batterySaverReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {
                val isEnabled = powerManager.isPowerSaveMode
                _isBatterySaverEnabled.value = isEnabled
                if (isEnabled) {
                    setUseHighPowerDiscovery(false)
                    setIsNearbyFriendsActive(false)
                    setIsNearbyBackgroundEnabled(false)
                    if (_autoUpdateMode.value == AutoUpdateMode.ALWAYS) {
                        setAutoUpdateMode(AutoUpdateMode.NEVER)
                    }
                    setShowImagesOnMetered(false)
                }
            }
        }
    }

    private fun triggerWidgetUpdate() {
        scope.launch {
            WidgetUpdateHelper.updateAllWidgets(context)
        }
    }

    private val _nickname = MutableStateFlow(prefs.getString("nickname", "Friend") ?: "Friend")
    val nickname: StateFlow<String> = _nickname.asStateFlow()
    fun setNickname(value: String) {
        prefs.edit { putString("nickname", value) }
        _nickname.value = value
        nearbyManager.updateLocalNickname(value)
    }

    private val _weather = MutableStateFlow<List<WeatherForecast>>(emptyList())
    val weather: StateFlow<List<WeatherForecast>> = _weather.asStateFlow()
    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    suspend fun updateWeather() {
        _isWeatherLoading.value = true
        try {
            val response = weatherApi.fetchWeather()
            _weather.value = response.daily.time.mapIndexed { index, time ->
                WeatherForecast(
                    time,
                    response.daily.minTemp[index],
                    response.daily.maxTemp[index],
                    response.daily.minApparentTemp[index],
                    response.daily.maxApparentTemp[index],
                    response.daily.weatherCode[index],
                    response.daily.precipitationSum[index],
                    response.daily.precipitationProbabilityMax[index],
                    hourly = response.hourly.time.mapIndexedNotNull { hIndex, hTime ->
                        if (hTime.startsWith(time)) {
                            HourlyWeather(
                                hTime,
                                response.hourly.temperature[hIndex],
                                response.hourly.weatherCode[hIndex],
                                response.hourly.precipitationProbability[hIndex]
                            )
                        } else null
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("DataRepository", "Error fetching weather: ${e.message}")
        } finally {
            _isWeatherLoading.value = false
        }
    }

    private val _isNearbyFriendsActive =
        MutableStateFlow(prefs.getBoolean("nearby_features", false))
    val isNearbyFriendsActive: StateFlow<Boolean> = _isNearbyFriendsActive.asStateFlow()
    fun setIsNearbyFriendsActive(value: Boolean) {
        prefs.edit { putBoolean("nearby_features", value) }
        _isNearbyFriendsActive.value = value
        nearbyManager.setNearbyFriendsActive(value)
        updateNearbyBackgroundService()
    }

    private val _isNearbyBackgroundEnabled =
        MutableStateFlow(prefs.getBoolean("nearby_background", false))
    val isNearbyBackgroundEnabled: StateFlow<Boolean> = _isNearbyBackgroundEnabled.asStateFlow()
    fun setIsNearbyBackgroundEnabled(value: Boolean) {
        prefs.edit { putBoolean("nearby_background", value) }
        _isNearbyBackgroundEnabled.value = value
        updateNearbyBackgroundService()
    }

    private fun updateNearbyBackgroundService() {
        val intent = Intent(context, com.csakitheone.streetmusic.data.nearby.NearbyBackgroundService::class.java)
        if (_isNearbyFriendsActive.value && _isNearbyBackgroundEnabled.value) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.stopService(intent)
        }
    }

    private val _showImagesOnMetered =
        MutableStateFlow(prefs.getBoolean("show_images_on_metered", false))
    val showImagesOnMetered: StateFlow<Boolean> = _showImagesOnMetered.asStateFlow()
    fun setShowImagesOnMetered(value: Boolean) {
        prefs.edit { putBoolean("show_images_on_metered", value) }
        _showImagesOnMetered.value = value
    }

    enum class AutoUpdateMode {
        NEVER, ONLY_UNMETERED, ALWAYS
    }

    private val _autoUpdateMode = MutableStateFlow(
        AutoUpdateMode.entries.find { it.name == prefs.getString("auto_update_mode", "") }
            ?: if (prefs.getBoolean("auto_update_on_unmetered", true)) AutoUpdateMode.ONLY_UNMETERED else AutoUpdateMode.NEVER
    )
    val autoUpdateMode: StateFlow<AutoUpdateMode> = _autoUpdateMode.asStateFlow()
    fun setAutoUpdateMode(value: AutoUpdateMode) {
        prefs.edit { putString("auto_update_mode", value.name) }
        _autoUpdateMode.value = value
    }

    private val _useHighPowerDiscovery =
        MutableStateFlow(prefs.getBoolean("high_power_discovery", false))
    val useHighPowerDiscovery: StateFlow<Boolean> = _useHighPowerDiscovery.asStateFlow()
    fun setUseHighPowerDiscovery(value: Boolean) {
        prefs.edit { putBoolean("high_power_discovery", value) }
        _useHighPowerDiscovery.value = value
        nearbyManager.useHighPowerDiscovery = value
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
    val allFavorites: StateFlow<Set<String>> = combine(
        userFavorites,
        nearbyManager.friends.nearbyFavorites
    ) { local, nearby ->
        local + nearby.values.flatten()
    }.stateIn(scope, SharingStarted.Lazily, _userFavorites.value)

    fun setFavorite(slug: String, value: Boolean) {
        val current = _userFavorites.value.toMutableSet()
        if (value) current.add(slug) else current.remove(slug)

        prefs.edit { putStringSet("fav_slugs", current) }
        _userFavorites.value = current
        nearbyManager.updateLocalFavorites(current)
        triggerWidgetUpdate()

        // Schedule/Cancel notification if it's an event slug
        if (slug.contains(" at ")) {
            scope.launch(Dispatchers.IO) {
                val event = events.first().find { "${it.artistSlug} at ${it.startTime}" == slug }
                if (event != null) {
                    if (value) {
                        AlarmScheduler.scheduleEventAlarm(context, event)
                    } else {
                        AlarmScheduler.cancelEventAlarm(context, event.id)
                    }
                }
            }
        }
    }

    fun toggleFavorite(slug: String) {
        val current = _userFavorites.value.toMutableSet()
        val newValue = !current.contains(slug)
        if (newValue) current.add(slug) else current.remove(slug)

        prefs.edit { putStringSet("fav_slugs", current) }
        _userFavorites.value = current
        nearbyManager.updateLocalFavorites(current)
        triggerWidgetUpdate()

        // Schedule/Cancel notification if it's an event slug
        if (slug.contains(" at ")) {
            scope.launch(Dispatchers.IO) {
                val event = events.first().find { "${it.artistSlug} at ${it.startTime}" == slug }
                if (event != null) {
                    if (newValue) {
                        AlarmScheduler.scheduleEventAlarm(context, event)
                    } else {
                        AlarmScheduler.cancelEventAlarm(context, event.id)
                    }
                }
            }
        }
    }

    fun clearFavorites() {
        prefs.edit { putStringSet("fav_slugs", emptySet()) }
        _userFavorites.value = emptySet()
        nearbyManager.updateLocalFavorites(emptySet())
        triggerWidgetUpdate()
    }

    fun clearMessages() {
        nearbyManager.friends.clearMessages()
    }

    init {
        ContextCompat.registerReceiver(
            context,
            batterySaverReceiver,
            IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        ContextCompat.registerReceiver(
            context,
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        nearbyManager.setNearbyFriendsActive(_isNearbyFriendsActive.value)
        nearbyManager.updateLocalFavorites(_userFavorites.value)
        nearbyManager.updateLocalNickname(_nickname.value)
        nearbyManager.useHighPowerDiscovery = _useHighPowerDiscovery.value

        scope.launch {
            updateWeather()
        }
    }

    var isDownloading by mutableStateOf(false)
        private set

    enum class DownloadResult {
        SUCCESS, NO_CONNECTION, ALREADY_HAS_DATA, ASK_FOR_METERED
    }

    val events: StateFlow<List<Event>> =
        database.eventDao().getAll().combine(userFavorites) { entities, favs ->
            val dbEvents = entities.map {
                Event(
                    it.id, it.artistId, it.artistSlug, it.artistName,
                    it.startTime, it.endTime, it.place,
                    isStarred = favs.contains("${it.artistSlug} at ${it.startTime}")
                )
            }
            val friendEvents = FriendRepository.events.map {
                it.copy(isStarred = favs.contains("${it.artistSlug} at ${it.startTime}"))
            }
            dbEvents + friendEvents
        }.stateIn(scope, SharingStarted.Lazily, emptyList())

    val eventDates: StateFlow<List<String>> = database.eventDao().getDistinctDates().map { dbDates ->
        val friendDates = FriendRepository.events.map { it.startTime.substring(0, 10) }
        (dbDates + friendDates).distinct().sorted()
    }.stateIn(scope, SharingStarted.Lazily, emptyList())

    val venues: StateFlow<List<Venue>> = database.venueDao().getAll().map { entities ->
        val dbVenues = entities.map { Venue(it.id, it.name, it.address) }
        val friendVenues = FriendRepository.venues
        (dbVenues + friendVenues).sortedBy { it.name }
    }.stateIn(scope, SharingStarted.Lazily, emptyList())

    val artists: StateFlow<List<Artist>> = combine(
        database.artistDao().getAll(),
        events,
        eventDates,
        userFavorites
    ) { entities, allEvents, allEventDates, favs ->
        val dbArtists = entities.map {
            Artist(
                it.id, it.name, it.country, it.description, it.image, it.slug,
                it.youtubeEmbed, it.tags.split(",").filter { tag -> tag.isNotBlank() },
                isStarred = favs.contains(it.slug)
            )
        }
        val friendArtists = FriendRepository.artists.map {
            it.copy(isStarred = favs.contains(it.slug))
        }
        val allArtists = (dbArtists + friendArtists).sortedBy { it.name }
        applyVirtualTags(allArtists, allArtists, allEvents, allEventDates)
    }.stateIn(scope, SharingStarted.Lazily, emptyList())

    fun getArtist(slug: String): Flow<Artist?> = artists.map { allArtists ->
        allArtists.find { it.slug == slug }
    }

    fun getEvent(id: Int): Flow<Event?> =
        database.eventDao().getById(id).combine(userFavorites) { entity, favs ->
            entity?.let {
                Event(
                    it.id, it.artistId, it.artistSlug, it.artistName,
                    it.startTime, it.endTime, it.place,
                    isStarred = favs.contains("${it.artistSlug} at ${it.startTime}")
                )
            } ?: FriendRepository.events.find { it.id == id }?.let {
                it.copy(isStarred = favs.contains("${it.artistSlug} at ${it.startTime}"))
            }
        }

    fun getEventsByArtist(artistSlug: String): Flow<List<Event>> =
        database.eventDao().getEventsByArtist(artistSlug).combine(userFavorites) { entities, favs ->
            val dbEvents = entities.map {
                Event(
                    it.id, it.artistId, it.artistSlug, it.artistName,
                    it.startTime, it.endTime, it.place,
                    isStarred = favs.contains("${it.artistSlug} at ${it.startTime}")
                )
            }
            val friendEvents = FriendRepository.events.filter { it.artistSlug == artistSlug }.map {
                it.copy(isStarred = favs.contains("${it.artistSlug} at ${it.startTime}"))
            }
            (dbEvents + friendEvents).sortedBy { it.startTime }
        }

    fun getVenueByName(name: String): Flow<Venue?> =
        database.venueDao().getByName(name).map { entity ->
            entity?.let { Venue(it.id, it.name, it.address) }
        }

    val hasData: Flow<Boolean> = database.artistDao().getCount()
        .combine(database.eventDao().getCount()) { artistCount, eventCount ->
            artistCount > 0 || eventCount > 0
        }

    /**
     * Tries to download the data from the API and store it in the local database.
     * This function checks network conditions, the auto-update setting and if the data is already in the database.
     * @param force If true, the data will be downloaded regardless of the auto-update setting,
     * unless there is no network connection.
     * @return [DownloadResult] indicating the result of the attempt.
     */
    suspend fun tryDownloadData(force: Boolean = false): DownloadResult {
        if (connectivityManager.activeNetwork == null) return DownloadResult.NO_CONNECTION

        val isMetered = connectivityManager.isActiveNetworkMetered
        val hasData = withContext(Dispatchers.IO) { database.eventDao().getCount().first() > 0 }

        // 1. If we don't have any data, we MUST try to get it.
        if (!hasData) {
            if (isMetered && !force) return DownloadResult.ASK_FOR_METERED
            downloadData()
            return DownloadResult.SUCCESS
        }

        // 2. We have data. If user explicitly requested (force), we download.
        if (force) {
            downloadData()
            return DownloadResult.SUCCESS
        }

        // 3. Auto-update logic
        val shouldUpdate = when (_autoUpdateMode.value) {
            AutoUpdateMode.NEVER -> false
            AutoUpdateMode.ONLY_UNMETERED -> !isMetered
            AutoUpdateMode.ALWAYS -> true
        }

        if (shouldUpdate) {
            downloadData()
            return DownloadResult.SUCCESS
        }

        return DownloadResult.ALREADY_HAS_DATA
    }

    /**
     * Downloads the data from the API and stores it in the local database.
     */
    suspend fun downloadData() = withContext(Dispatchers.IO) {
        val yearFilter = 2026
        val emulateFirstDayOfEvent = false

        isDownloading = true

        updateWeather()

        val apiVenues = try {
            api.fetchVenues()
        } catch (e: Exception) {
            Log.e("DataRepository", "Error fetching venues: ${e.message}")
            emptyList()
        }

        if (apiVenues.isNotEmpty()) {
            database.venueDao().deleteAll()
            database.venueDao().insertAll(apiVenues.map {
                VenueEntity(it.id, it.name, it.address)
            })
            Log.d("DataRepository", "Added ${apiVenues.size} venues.")
        }

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

                artist.timeslots.mapIndexedNotNull { index, slot ->
                    val date = slot.eventStartTime ?: ""
                    if (!date.startsWith(yearFilter.toString())) return@mapIndexedNotNull null
                    val eventId = slot.event ?: return@mapIndexedNotNull null

                    val compoundId =
                        (artist.id * 1000) + (index * 100) + eventId

                    EventEntity(
                        id = compoundId,
                        artistId = artist.id,
                        artistName = artist.name,
                        artistSlug = artist.slug,
                        place = slot.eventVenueName ?: "Unknown Venue",
                        startTime = "${date}T${slot.startTime}",
                        endTime = "${date}T${slot.endTime}"
                    )
                }
            }

            Log.d("DataRepository", "Processing ${events.size} events...")

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

            AlarmScheduler.rescheduleAll(context, this@DataRepository)
        }
        isDownloading = false
    }

    fun deleteDatabase() {
        database.clearAllTables()
    }

    suspend fun syncData(
        artists: List<ArtistEntity>,
        events: List<EventEntity>,
        venues: List<VenueEntity>
    ) = withContext(Dispatchers.IO) {
        database.artistDao().deleteAll()
        database.artistDao().insertAll(artists)
        database.eventDao().deleteAll()
        database.eventDao().insertAll(events)
        database.venueDao().deleteAll()
        database.venueDao().insertAll(venues)
    }

    fun getAllArtistEntities(): Flow<List<ArtistEntity>> = database.artistDao().getAll()
    fun getAllEventEntities(): Flow<List<EventEntity>> = database.eventDao().getAll()
    fun getAllVenueEntities(): Flow<List<VenueEntity>> = database.venueDao().getAll()

    private fun applyVirtualTags(
        artistsToTag: List<Artist>,
        allArtists: List<Artist>,
        allEvents: List<Event>,
        allEventDates: List<String>
    ): List<Artist> {
        val completeArtists = allArtists.filter {
            it.description.isNotBlank() && !it.image.isNullOrBlank() && !it.youtubeEmbed.isNullOrBlank()
        }
        val isAnyIncomplete = completeArtists.size != allArtists.size

        return artistsToTag.map { artist ->
            val artistEvents = allEvents.filter { it.artistSlug == artist.slug }
            val tags = artist.tags.toMutableSet()

            if (allEventDates.isNotEmpty()) {
                if (artistEvents.size < allEventDates.size) {
                    if (artistEvents.size == 1) tags.add("onechance")
                    else if (artistEvents.size == 2) tags.add("twoshot")
                } else if (artistEvents.size > allEventDates.size) {
                    tags.add("encore")
                }
            }

            if (isAnyIncomplete) {
                val isComplete = artist.description.isNotBlank() &&
                        !artist.image.isNullOrBlank() &&
                        !artist.youtubeEmbed.isNullOrBlank()
                tags.add(if (isComplete) "complete" else "incomplete")
            }

            artist.copy(tags = tags.toList().sorted())
        }
    }
}
