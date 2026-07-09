package com.csakitheone.streetmusic.data

import com.csakitheone.streetmusic.data.model.UnlockFestEvent

class UnlockFestRepository {
    companion object {

        val facebookEventUrl = "https://www.facebook.com/events/1311071261202451/"

        val ticketsUrl = "https://www.tixa.hu/unlock-fest-vol-vii-20260723"

        val events: List<UnlockFestEvent> = listOf(
            UnlockFestEvent("Dummy Toys (China)", "https://dummytoys.bandcamp.com/", 23, 1),
            UnlockFestEvent(
                "Ten Years Suffering",
                "https://tenyearssuffering.bandcamp.com/",
                23,
                2
            ),
            UnlockFestEvent(
                "Szolnoki Lajos és Bodnár Balázs",
                "https://youtu.be/iNcp66EaEy4?is=kpumvVm8R9he81tc",
                23,
                3
            ),
            UnlockFestEvent("After: DJ Windows Media Player", null, 23, 4),
            UnlockFestEvent("Crow Noise (Malaysia)", "https://crownoise.bandcamp.com/", 24, 1),
            UnlockFestEvent("Salabakter", "https://salabakter.bandcamp.com/", 24, 2),
            UnlockFestEvent("Mészárlás", "https://www.instagram.com/meszarlas666", 24, 3),
            UnlockFestEvent("Slave Mentality", "https://slavementalitycrust.bandcamp.com/", 24, 4),
            UnlockFestEvent("Split Second", "https://splitsecondhc.bandcamp.com/", 24, 5),
            UnlockFestEvent("Közöny", "https://www.youtube.com/watch?v=xjhc2-06Xx4", 24, 6),
            UnlockFestEvent("After: DJ Neondervölgyi", null, 24, 7),
            UnlockFestEvent("Gnoj (Serbia)", "https://gnojpunk.bandcamp.com/", 25, 1),
            UnlockFestEvent("Defied", "https://defied.bandcamp.com/", 25, 2),
            UnlockFestEvent("Watchmybag", "https://watchmybag.bandcamp.com/", 25, 3),
            UnlockFestEvent("Funclub", "https://funclub23.bandcamp.com/album/demo-tapes", 25, 4),
            UnlockFestEvent("Raisinboys", "https://raisinboys.bandcamp.com/", 25, 5),
            UnlockFestEvent("Berliose", "https://berliose.bandcamp.com/", 25, 6),
            UnlockFestEvent("After: DJ Girlpower", null, 25, 7),
        )

        val eventDays = events.map { it.day }.toSet().sorted()

        val ticketPrices = mapOf(
            23 to 3000,
            24 to 5000,
            25 to 5000,
        )

    }
}