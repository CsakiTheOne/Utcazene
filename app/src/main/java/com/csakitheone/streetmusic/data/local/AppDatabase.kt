package com.csakitheone.streetmusic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ArtistEntity::class, EventEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
    abstract fun eventDao(): EventDao
}
