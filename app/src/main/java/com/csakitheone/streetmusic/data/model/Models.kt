package com.csakitheone.streetmusic.data.model

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
)
