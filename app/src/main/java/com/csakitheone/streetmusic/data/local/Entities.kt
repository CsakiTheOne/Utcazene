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

@Entity(tableName = "venues")
@Serializable
data class VenueEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val address: String
)

@Entity(tableName = "thread_nodes")
@Serializable
data class ThreadNode(
    @PrimaryKey val id: String,
    val parentId: String,
    val senderName: String,
    val content: String,
) {
    companion object {

        val MAIN = ThreadNode(
            id = "main",
            parentId = "",
            senderName = "UZ App",
            content = "Here you can chat with nearby friends and comment to artist profiles without internet. You can send messages even when nobody's connected.",
        )

    }
}
