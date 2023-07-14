package com.csakitheone.streetmusic.data

import android.content.Context
import android.util.Log
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.model.Author
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.model.Place

class EventsProvider {
    companion object {

        private val customAuthors = listOf(
            Author(
                name = "Shántolók",
                tags = listOf(Author.TAG_FRIEND),
            )
        )

        private val customEvents = listOf(
            Event(
                author = customAuthors[0],
                day = 19,
                time = "TBA",
                place = Place(name = "TBA"),
            )
        )

        private var rawEvents = mutableListOf<Event>()

        val authors = listOf(
            Author.fromString("Bare Jams (UK)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Charles Pasi (FR)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Che Sudaka (CO/RA/E)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("CQMD (FR)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Daiana Lou (IT)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Estrela Gomes (P)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Félix Rabin (FR)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Henry Facey (UK)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Hugo Barriol (FR)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Keanan Eksteen (ZA)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Kraków Street Band (PL)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Markus Koehorst (NL)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Martin Harley (UK)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Moonshiners (P)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Nathan Johnston (IRE)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Noble Jacks (UK)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Opsa Dehëli (FR)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Reuben Stone (NZ)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Rob Heron & The Tea Pad Orchestra (UK)")
                .copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Roe Byrne (IRE)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Sean Koch (ZA)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Sebastian Schub (UK)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Siricaia (P)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Sissos (AUS)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Slim Paul Trio (FR)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("Tanga Elektra (D)").copy(tags = listOf(Author.TAG_FOREIGN)),
            Author.fromString("The Hillbilly Moonshiners (NL)")
                .copy(tags = listOf(Author.TAG_FOREIGN)),
            Author(name = "A few mistakes ago", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "BlechKraft", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "Borostyán & Veronika", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "Feed The Mogul", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "Géem", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "Hajdu Erik", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "KAM", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "Kustan Adam", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "Léhárt Míra és Polgár Patrik", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "Magácska", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "The Flow Acoustic", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "Nani On The Run", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "No Rules", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "OSSO", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "Prommer Patrik", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "QUACK", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "Red Roosters", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "Rézeleje Fanfárosok", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "Sun Syndrome", tags = listOf(Author.TAG_COMPETING)),
            Author(name = "Tibi and the Otters", tags = listOf(Author.TAG_COMPETING)),

            Author(name = "Gála"),
            Author(name = "Daniel Docherty"),
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
                            val author = authors.firstOrNull {
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
                                    author = author,
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