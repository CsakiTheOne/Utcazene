package com.csakitheone.streetmusic.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiVenue(
    val id: Int,
    val name: String,
    val address: String
)

@Serializable
data class ApiEvent(
    val id: Int,
    val title: String,
    @SerialName("start_time") val startDate: String,
    @SerialName("end_time") val endDate: String,
    val venue: Int? = null
)

@Serializable
data class ApiArtist(
    val id: Int,
    val name: String,
    val country: String,
    val description: String,
    val image: String?,
    val slug: String,
    val headliner: Boolean,
    @SerialName("youtube_embed") val youtubeEmbed: String?,
    @SerialName("disable_link") val disableLink: Boolean,
    val timeslots: List<ApiTimeSlot>
)

@Serializable
data class ApiTimeSlot(
    val id: Int? = null,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val artist: Int? = null,
    @SerialName("artist_name") val artistName: String? = null,
    @SerialName("artist_slug") val artistSlug: String? = null,
    @SerialName("artist_country") val artistCountry: String? = null,
    @SerialName("artist_headliner") val artistHeadliner: Boolean? = null,
    @SerialName("artist_disable_link") val artistDisableLink: Boolean? = null,
    val event: Int? = null,
    val venue: Int? = null,
    @SerialName("venue_name") val venueName: String? = null,
    @SerialName("event_day") val eventDay: String? = null,
    @SerialName("event__title") val eventTitle: String? = null,
    @SerialName("event__start_time") val eventStartTime: String? = null,
    @SerialName("event__venue__name") val eventVenueName: String? = null
)

@Serializable
data class ApiWeatherResponse(
    val daily: ApiWeatherDaily
)

@Serializable
data class ApiWeatherDaily(
    val time: List<String>,
    @SerialName("temperature_2m_max") val maxTemp: List<Double>,
    @SerialName("temperature_2m_min") val minTemp: List<Double>,
    @SerialName("apparent_temperature_max") val maxApparentTemp: List<Double>,
    @SerialName("apparent_temperature_min") val minApparentTemp: List<Double>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("precipitation_sum") val precipitationSum: List<Double>,
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Int>
)
