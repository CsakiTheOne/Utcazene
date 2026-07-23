package com.csakitheone.streetmusic.data

import com.csakitheone.streetmusic.data.model.Artist
import com.csakitheone.streetmusic.data.model.Event
import com.csakitheone.streetmusic.data.model.Venue

class FriendRepository {
    companion object {
        val artists = listOf(
            Artist(
                id = -1,
                name = "Shántolók",
                description = "Először a Lovassy Alma Mater rendezvényén hallottam " +
                        "sea shanty-ket énekelni az urakat ahol is egyértelműen megnyerték a " +
                        "közönséget. Ez abból is látszott, hogy egy daluk után két hölgy a " +
                        "nézőtéren felállva, egy nagy szivet mutatott a fiúknak. Egy amúgy is " +
                        "szórakoztató műfajt adnak elő, néha hangszerkísérettel. Aki kedveli a " +
                        "sea shanty-ket, mindenképp meg kell hallgatnia őket!",
                country = "HU",
                image = "https://i.ytimg.com/vi/grWgky_ZIJw/maxresdefault.jpg",
                slug = "shantolok",
                youtubeEmbed = "grWgky_ZIJw",
                tags = listOf(Artist.TAG_FRIEND),
            ),
            Artist(
                id = -2,
                name = "odaLÖKött KÖLtészet",
                description = "Ha viszket a tollad, vakard meg a papírt! Ha viszket a füled, hallgasd meg más mit írt!",
                country = "HU",
                image = null,
                slug = "odalokkott_kolteszet",
                youtubeEmbed = null,
                tags = listOf(Artist.TAG_FRIEND),
            ),
        )

        val events = listOf(
            Event(
                id = -1,
                artistId = -1,
                artistSlug = "shantolok",
                artistName = "Shántolók",
                startTime = "2026-07-23T18:00:00",
                endTime = "2026-07-23T19:00:00",
                place = "Sarolta udvar",
            ),
            Event(
                id = -2,
                artistId = -100,
                artistSlug = "ring-of-cash",
                artistName = "Ring Of Cash",
                startTime = "2026-07-22T19:00:00",
                endTime = "2026-07-22T20:30:00",
                place = "Nyárkert",
            ),
            Event(
                id = -3,
                artistId = -100,
                artistSlug = "santa-machete",
                artistName = "Santa Machete",
                startTime = "2026-07-22T21:30:00",
                endTime = "2026-07-22T23:00:00",
                place = "Nyárkert",
            ),
            Event(
                id = -4,
                artistId = -2,
                artistSlug = "odalokkott_kolteszet",
                artistName = "odaLÖKött KÖLtészet",
                startTime = "2026-07-24T18:00:00",
                endTime = "2026-07-24T19:00:00",
                place = "Kossuth utca félemelet",
            ),
        )

        val venues = listOf(
            Venue(
                id = -1,
                name = "Nyárkert",
                address = "Színházkert",
            )
        )
    }
}
