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
) {
    companion object {
        const val TAG_FRIEND = "friend"
    }
}

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
        description = "Utcazene event #$id. Location: $place",
        customSlug = "$artistSlug at $startTime"
    )
}

data class Venue(
    val id: Int,
    val name: String,
    val address: String
)

val tagInfo = mapOf(
    "headliner" to "This artist isn't participating in Utcazene's competition. Enjoy the party!",
    "competitor" to "This artist participates in Utcazene's competition. If you like them, you can vote for them on the official website.",
    "onechance" to "This artist only performs once during the whole event. Don't miss the party!",
    "twoshot" to "This artist performs twice during the whole event.",
    "encore" to "This artist performs more than the number of days the event lasts.",
    "complete" to "This artist has a description, image and YouTube URL.",
    "incomplete" to "This artist doesn't have a description, image and/or YouTube URL.",
    Artist.TAG_FRIEND to "This artist is a friend of the developer and performs outside the official event. Check them out!",
)
