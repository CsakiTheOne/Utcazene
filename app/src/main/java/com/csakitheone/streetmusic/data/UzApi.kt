package com.csakitheone.streetmusic.data

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.model.Place
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager


class UzApi {
    data class ApiTimeslot(
        val start_time: String,
        val end_time: String,
        val event: Int,
        val event__title: String,
        val event__start_time: String,
        val event__venue__name: String,
    )

    data class ApiArtist(
        val id: Int,
        val name: String,
        val country: String,
        val description: String,
        val image: String,
        val slug: String,
        val headliner: Boolean,
        val youtube_embed: String,
        val timeslots: List<ApiTimeslot>,
    )

    companion object {

        var client: OkHttpClient
            private set

        const val PREF_KEY_API_RESPONSE_ARTISTS = "apiResponseArtists"
        const val PREF_KEY_API_LAST_DOWNLOAD_TIMESTAMP = "apiLastDownloadTimestamp"

        private val apiUrl = "https://utcazene.hu/api"
        private val endpointVenues = "/venues/"
        private val endpointEvents = "/events/"
        private val endpointArtists = "/artists/"
        private val endpointTimeslots = "/timeslots/"

        init {
            val trustAllCerts = arrayOf(object: X509TrustManager {
                override fun checkClientTrusted(
                    p0: Array<out X509Certificate>?,
                    p1: String?
                ) {}
                override fun checkServerTrusted(
                    p0: Array<out X509Certificate>?,
                    p1: String?
                ) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(
                null,
                trustAllCerts,
                SecureRandom(),
            )

            client = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0])
                .hostnameVerifier(HostnameVerifier { hostname, sslSession ->
                    return@HostnameVerifier true//hostname.contains("utcazene")
                })
                .build()
        }

        fun downloadEvents(context: Context, callback: (List<Event>) -> Unit) {
            Thread {
                try {
                    Log.i("UzApi", "Attempting to download events...")
                    val response = client
                        .newCall(
                            Request.Builder()
                                .url("$apiUrl$endpointArtists")
                                .get()
                                .build()
                        )
                        .execute()

                    val responseString = response.body?.string()

                    Log.i("UzApi", "Response string: $responseString")

                    if (!responseString.isNullOrBlank()) {
                        Log.i("UzApi", "Saving response to preferences...")

                        PreferenceManager.getDefaultSharedPreferences(context).edit {
                            putString(PREF_KEY_API_RESPONSE_ARTISTS, responseString)
                            putLong(PREF_KEY_API_LAST_DOWNLOAD_TIMESTAMP, System.currentTimeMillis())
                            commit()
                        }
                    }

                    callback(artistsJsonToEvents(responseString))
                }
                catch (ex: Exception) {
                    Log.e("UzApi", ex.stackTraceToString())
                    callback(listOf())
                }

            }.start()
        }

        fun artistsJsonToEvents(json: String?): List<Event> {
            if (json.isNullOrBlank()) {
                return emptyList()
            }

            val responseType = object : TypeToken<List<ApiArtist>>() {}.type
            val apiArtists = Gson().fromJson<List<ApiArtist>>(json, responseType)

            val events = mutableListOf<Event>()

            apiArtists.map { artist ->
                val musician = Musician(
                    name = artist.name,
                    description = artist.description
                        .replace(Regex("""<[br/ ]{2,4}>"""), "\n"),
                    country = artist.country,
                    imageUrl = artist.image,
                    youtubeUrl = "https://www.youtube.com/watch?v=${artist.youtube_embed}",
                    tags = listOf(if (artist.headliner) Musician.TAG_FOREIGN else Musician.TAG_COMPETING)
                )
                artist.timeslots.map { timeslot ->
                    val place = Place.getValues().firstOrNull { place ->
                        place.name.substringBefore('(').trim()
                            .replace("utcazenész megálló", "")
                            .replace(Regex("""[ -]"""), "")
                            .equals(
                                timeslot.event__venue__name
                                    .replace("utcazenész megálló", "")
                                    .replace(Regex("""[ -]"""), ""),
                                true
                            )
                    } ?: Place.MISSING
                    if (place == Place.MISSING) {
                        Log.w("UzApi", "Couldn't find place: ${timeslot.event__venue__name}")
                    }

                    events.add(
                        Event(
                            musician = musician,
                            day = timeslot.event__start_time.substringAfterLast('-').toInt(),
                            time = timeslot.start_time.substringBeforeLast(':'),
                            place = place,
                        )
                    )
                }
            }

            return  events
        }

    }
}