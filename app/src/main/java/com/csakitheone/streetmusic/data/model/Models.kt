package com.csakitheone.streetmusic.data.model

import java.time.LocalTime

data class Artist(
    val id: Int,
    val name: String,
    val country: String,
    val description: String,
    val image: String?,
    val slug: String,
    val youtubeEmbed: String?,
    val tags: List<String> = emptyList(),
    val isStarred: Boolean = false
)

data class Event(
    val id: Int,
    val artistId: Int,
    val artistSlug: String,
    val artistName: String,
    val startTime: String, // ISO 8601
    val endTime: String,   // ISO 8601
    val place: String,
    val isStarred: Boolean = false
) {
    /**
     * Converts this event to an external event.
     * This should only be used for comparison and sorting, now for display purposes.
     */
    fun toExternalEvent(): ExternalEvent = ExternalEvent(
        name = artistName,
        day = startTime.substring(8, 10).toInt(),
        startTime = LocalTime.parse(startTime.substring(11)),
        endTime = LocalTime.parse(endTime.substring(11)),
        description = place,
        customSlug = "$artistSlug at $startTime"
    )
}
