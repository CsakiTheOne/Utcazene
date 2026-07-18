package com.csakitheone.streetmusic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.csakitheone.streetmusic.data.local.ThreadNode

@Database(
    entities = [ArtistEntity::class, EventEntity::class, VenueEntity::class, ThreadNode::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
    abstract fun eventDao(): EventDao
    abstract fun venueDao(): VenueDao
    abstract fun threadNodeDao(): ThreadNodeDao
}
