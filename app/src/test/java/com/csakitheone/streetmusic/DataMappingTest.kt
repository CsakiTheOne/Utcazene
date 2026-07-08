package com.csakitheone.streetmusic

import com.csakitheone.streetmusic.data.api.ApiVenue
import com.csakitheone.streetmusic.data.local.VenueEntity
import com.csakitheone.streetmusic.data.model.Venue
import org.junit.Assert.assertEquals
import org.junit.Test

class DataMappingTest {

    @Test
    fun apiVenueToVenueEntityMapping() {
        val apiVenue = ApiVenue(id = 1, name = "Stage 1", address = "Address 1")
        val entity = VenueEntity(id = apiVenue.id, name = apiVenue.name, address = apiVenue.address)
        
        assertEquals(1, entity.id)
        assertEquals("Stage 1", entity.name)
        assertEquals("Address 1", entity.address)
    }

    @Test
    fun venueEntityToDomainVenueMapping() {
        val entity = VenueEntity(id = 1, name = "Stage 1", address = "Address 1")
        val domain = Venue(id = entity.id, name = entity.name, address = entity.address)
        
        assertEquals(1, domain.id)
        assertEquals("Stage 1", domain.name)
        assertEquals("Address 1", domain.address)
    }

    @Test
    fun artistEntityToDomainArtistMapping() {
        val entity = com.csakitheone.streetmusic.data.local.ArtistEntity(
            id = 1, name = "Artist 1", country = "HU", description = "Desc",
            image = "img", slug = "slug", youtubeEmbed = "yt", tags = "headliner,friend"
        )
        val domain = com.csakitheone.streetmusic.data.model.Artist(
            id = entity.id, name = entity.name, country = entity.country,
            description = entity.description, image = entity.image, slug = entity.slug,
            youtubeEmbed = entity.youtubeEmbed, tags = entity.tags.split(",")
        )

        assertEquals(listOf("headliner", "friend"), domain.tags)
    }
}
