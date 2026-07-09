package com.csakitheone.streetmusic.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.csakitheone.streetmusic.data.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists")
    fun getAll(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE slug = :slug LIMIT 1")
    fun getBySlug(slug: String): Flow<ArtistEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(artists: List<ArtistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(artist: ArtistEntity)

    @Query("DELETE FROM artists")
    suspend fun deleteAll()
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun getById(id: Int): Flow<EventEntity?>

    @Query("SELECT COUNT(*) FROM events")
    fun getCount(): Flow<Int>

    @Query("SELECT DISTINCT SUBSTR(startTime, 1, 10) FROM events ORDER BY startTime ASC")
    fun getDistinctDates(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>)

    @Query("DELETE FROM events")
    suspend fun deleteAll()

    @Query("SELECT * FROM events WHERE artistSlug = :artistSlug")
    fun getEventsByArtist(artistSlug: String): Flow<List<EventEntity>>
}
