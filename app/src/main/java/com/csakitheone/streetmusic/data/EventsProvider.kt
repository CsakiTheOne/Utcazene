package com.csakitheone.streetmusic.data

import android.content.Context
import android.util.Log
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.model.Place

class EventsProvider {
    companion object {

        val isDownloaded: Boolean
            get() = false

        private val customMusicians = listOf(
            Musician(
                name = "Shántolók",
                tags = listOf(Musician.TAG_FRIEND),
            )
        )

        private val customEvents = listOf(
            Event(
                musician = customMusicians[0],
                day = 19,
                time = "TBA",
                place = Place(name = "TBA"),
            )
        )

        private var rawEvents = mutableListOf<Event>()

        val musicians = listOf(
            Musician.fromString("Bare Jams (UK)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Charles Pasi (FR)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Che Sudaka (CO/RA/E)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("CQMD (FR)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Daiana Lou (IT)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Estrela Gomes (P)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Félix Rabin (FR)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Henry Facey (UK)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Hugo Barriol (FR)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Keanan Eksteen (ZA)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Kraków Street Band (PL)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Markus Koehorst (NL)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Martin Harley (UK)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Moonshiners (P)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Nathan Johnston (IRE)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Noble Jacks (UK)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Opsa Dehëli (FR)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Reuben Stone (NZ)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Rob Heron & The Tea Pad Orchestra (UK)")
                .copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Roe Byrne (IRE)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Sean Koch (ZA)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Sebastian Schub (UK)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Siricaia (P)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Sissos (AUS)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Slim Paul Trio (FR)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("Tanga Elektra (D)").copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician.fromString("The Hillbilly Moonshiners (NL)")
                .copy(tags = listOf(Musician.TAG_FOREIGN)),
            Musician(name = "A few mistakes ago", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "BlechKraft", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "Borostyán & Veronika", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "Feed The Mogul", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "Géem", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "Hajdu Erik", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "KAM", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "Kustan Adam", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "Léhárt Míra és Polgár Patrik", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "Magácska", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "The Flow Acoustic", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "Nani On The Run", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "No Rules", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "OSSO", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "Prommer Patrik", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "QUACK", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "Red Roosters", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "Rézeleje Fanfárosok", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "Sun Syndrome", tags = listOf(Musician.TAG_COMPETING)),
            Musician(name = "Tibi and the Otters", tags = listOf(Musician.TAG_COMPETING)),

            Musician(name = "Gála"),
            Musician(name = "Daniel Docherty"),
        ).sortedBy { it.name }

        fun loadEvents(context: Context): List<Event> {
            fun String.cells(): List<String> = this.trim().split(',').map { it.trim() }

            val lines = context.resources.openRawResource(R.raw.events)
                .reader(Charsets.UTF_8)
                .readLines()

            Log.i("EventsProvider", lines.first())

            if (lines.first().cells().first() != "events") {
                throw Exception(
                    "Events file validation failed! First cell: ${
                        lines.first().cells().first()
                    }"
                )
            }

            rawEvents = mutableListOf()
            var time: String
            var place = Place(name = "TBA")
            lines.forEach { line ->
                val cells = line.cells()
                if (cells.size != 5) {
                    throw Exception("Events file validation failed in line: $line")
                }
                if (cells.first().matches(Regex("""[0-9]{1,2}\.[0-9]{2}"""))) {
                    time = cells.first().replace('.', ':')
                    (1..4).forEach { col ->
                        if (cells[col].isNotBlank()) {
                            val author = musicians.firstOrNull {
                                it.name.take(25)
                                    .equals(cells[col].substringBefore('(').trim().take(25), true)
                            }
                                ?: throw Exception(
                                    "Author not found: ${
                                        cells[col].substringBefore(
                                            '('
                                        ).trim()
                                    }"
                                )
                            rawEvents.add(
                                Event(
                                    musician = author,
                                    day = 19 + col - 1,
                                    time = time,
                                    place = place,
                                )
                            )
                            Log.i("EventsProvider", "Loaded event: ${rawEvents.last()}")
                        }
                    }
                } else if (cells.first() != "events") {
                    place = Place.getValues().firstOrNull {
                        it.name
                            .substringBefore("(")
                            .replace(Regex(""" -"""), "")
                            .equals(
                                cells.first()
                                    .substringBefore("(")
                                    .replace(Regex(""" -"""), ""),
                                true
                            )
                    } ?: Place.MISSING
                    if (place == Place.MISSING) {
                        place = Place.getValues().firstOrNull {
                            it.name
                                .replace(Regex(""" -"""), "")
                                .take(4)
                                .equals(
                                    cells.first()
                                        .replace(Regex(""" -"""), "")
                                        .take(4),
                                    true
                                )
                        } ?: throw Exception("Place not found: ${cells.first()}")
                    }
                }
            }

            return rawEvents
        }

        fun getEvents(context: Context? = null): List<Event> {
            if (rawEvents.isEmpty()) {
                if (context == null) Log.e("EventsProvider", "Events can't be loaded!")
                else loadEvents(context)
            }

            val events = mutableListOf<Event>()
            events.addAll(rawEvents)
            events.addAll(customEvents)
            return events
        }

    }
}