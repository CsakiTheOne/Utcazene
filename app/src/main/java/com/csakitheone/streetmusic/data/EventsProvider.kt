package com.csakitheone.streetmusic.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.preference.PreferenceManager
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.model.Place
import com.csakitheone.streetmusic.util.Helper

class EventsProvider {
    companion object {

        private val musicians = listOf(
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
            Musician.fromString("Kraków Street Band (PL)")
                .copy(tags = listOf(Musician.TAG_FOREIGN)),
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

        fun readEventsFromCsv(context: Context): List<Event> {
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

            val rawEvents = mutableListOf<Event>()
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
                                .replace("utcazenész megálló", "")
                                .replace(Regex("""[ -]"""), "")
                                .take(4)
                                .equals(
                                    cells.first()
                                        .replace("utcazenész megálló", "")
                                        .replace(Regex("""[ -]"""), "")
                                        .take(4),
                                    true
                                )
                        } ?: throw Exception("Place not found: ${cells.first()}")
                    }
                }
            }

            return rawEvents
        }

        private val customMusicians = listOf(
            Musician(
                name = "Shántolók",
                description = "Először a Lovassy Alma Mater rendezvényén hallottam " +
                        "sea shanty-ket énekelni az urakat ahol is egyértelműen megnyerték a " +
                        "közönséget. Ez abból is látszott, hogy egy daluk után két hölgy a " +
                        "nézőtéren felállva egy nagy szivet mutatott a fiúknak. Egy amúgy is " +
                        "szórakoztató műfajt adnak elő, néha hangszerkísérettel. Aki kedveli a " +
                        "sea shanty-ket, mindenképp meg kell hallgatnia őket!",
                /*
                description = "Figyelem! Ennek a bandának a leírását a ChatGPT írta!\n" +
                        "Ezt a csodálatos tengerészeti kórust mindenkinek látnia kell, aki a parton járva szívesen énekelne együtt a hullámokkal. A Lovassy László Gimnázium fiatal tehetségei különleges élményt nyújtanak a közönségnek a hagyományos tengerész dalok átélésével.\n" +
                        "Ezek a zenei kalózok olyan dallamokat szövögetnek a hangok hálójában, hogy megállás nélkül dúdolni fogsz hajónak lenni, még akkor is, ha soha nem ültél egy fedélzeten sem. A hangzásuk egyszerre hívogató és megható, olyan, mintha a szelek maguk szabadították volna el a vitorlákat.\n" +
                        "Együtt járják az óceánt a dalok, ahol mindenki tagja a történetnek. A fiatal tengeri farkasok fellelkesítik a közönséget a felhőtlen vidámsággal és lendülettel, amely átjárja előadásaikat. Énekük hullámzik az égen, és felrázza az emberek szíveit, mintha egy hatalmas vihar tombolna bennük.\n" +
                        "A Lovassy Alma mater rendezvényén már felmutatták tehetségüket és elvarázsolták a közönséget. Most pedig itt vannak, hogy színre vigyenek egy igazi tengeri show-t, és elhívjanak mindenkit egy kalandos utazásra a zene hullámain.\n" +
                        "Ne hagyd ki a lehetőséget, hogy együtt énekelj velük a tengeri költészet és a távoli tájak dallamaiban. Ez a kórussal való találkozás igazi élmény lesz, ami megdobogtatja a szíved, és elkápráztat a tengeri mágia varázslatával. Csatlakozz hozzájuk, és indulj velük a tengeri ének hajóján!",
                */
                country = "HU",
                imageUrl = "https://i.ytimg.com/vi/grWgky_ZIJw/maxresdefault.jpg",
                youtubeUrl = "https://youtu.be/grWgky_ZIJw",
                tags = listOf(Musician.TAG_FRIEND),
            )
        )

        val STATE_DOWNLOADING = R.string.data_state_downloading
        val STATE_DOWNLOADED = R.string.data_state_downloaded
        val STATE_DOWNLOAD_ERROR = R.string.data_state_download_error
        val STATE_PREFERENCES = R.string.data_state_preferences
        val STATE_APP = R.string.data_state_app
        val STATE_UNKNOWN = R.string.data_state_unknown

        var state by mutableStateOf(STATE_UNKNOWN)

        val customEvents = listOf(
            Event(
                musician = customMusicians[0],
                day = 19,
                time = "17:00",
                place = Place("Óváros tér"),
            )
        )

        /**
         * Gets the events in this order:
         * 1. If there is an unmetered network connection and downloaded events are old, downloads it from the API.
         * 2. If there is no network, but events were downloaded once, reads it from preferences.
         * 3. If there are no events in the preferences, reads it from the CSV.
         */
        fun getEvents(
            context: Context,
            forceDownload: Boolean = false,
            skipDownload: Boolean = false,
            callback: (List<Event>) -> Unit = {}
        ) {
            state = STATE_UNKNOWN
            // 1.
            val timeTillOld = 1000L * 60 * 30
            val isDataOld = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getLong(
                    UzApi.PREF_KEY_API_LAST_DOWNLOAD_TIMESTAMP,
                    0L
                ) + timeTillOld < System.currentTimeMillis()
            if (
                forceDownload ||
                (
                        isDataOld &&
                                Helper.isUnmeteredNetworkAvailable(context) &&
                                !skipDownload
                        )
            ) {
                state = STATE_DOWNLOADING
                UzApi.downloadEvents(context) {
                    if (it.isNotEmpty()) {
                        callback(it + customEvents)
                        state = STATE_DOWNLOADED
                    } else {
                        state = STATE_DOWNLOAD_ERROR
                        getEvents(context, skipDownload = true, callback = callback)
                    }
                }
                return
            }
            // 2.
            val json = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(UzApi.PREF_KEY_API_RESPONSE_ARTISTS, "")
            if (!json.isNullOrBlank()) {
                callback(UzApi.artistsJsonToEvents(json) + customEvents)
                state = STATE_PREFERENCES
                return
            }
            // 3.
            callback(readEventsFromCsv(context) + customEvents)
            state = STATE_APP
        }

    }
}