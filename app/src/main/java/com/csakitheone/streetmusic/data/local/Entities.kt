package com.csakitheone.streetmusic.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "artists")
@Serializable
data class ArtistEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val country: String,
    val description: String,
    val image: String?,
    val slug: String,
    val youtubeEmbed: String?,
    val tags: String // Comma-separated tags
)

@Entity(tableName = "events")
@Serializable
data class EventEntity(
    @PrimaryKey val id: Int,
    val artistId: Int,
    val artistSlug: String,
    val artistName: String,
    val startTime: String,
    val endTime: String,
    val place: String
)
