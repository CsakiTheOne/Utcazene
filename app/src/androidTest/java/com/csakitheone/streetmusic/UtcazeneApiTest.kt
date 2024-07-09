package com.csakitheone.streetmusic

import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test
import org.junit.runner.RunWith
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class UtcazeneApiTest {

    private val apiUrl = "https://utcazene.hu/api"
    private val endpointVenues = "/venues/"
    private val endpointEvents = "/events/"
    private val endpointArtists = "/artists/"
    private val endpointTimeslots = "/timeslots/"

    private fun createHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(
                p0: Array<out X509Certificate>?,
                p1: String?
            ) {
            }

            override fun checkServerTrusted(
                p0: Array<out X509Certificate>?,
                p1: String?
            ) {
            }

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

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0])
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    @Test
    fun testApi() {
        val client = createHttpClient()
        val request = Request.Builder()
            .url("$apiUrl/")
            .get()
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        println(body)
    }

    @Test
    fun testVenues() {
        val client = createHttpClient()
        val request = Request.Builder()
            .url("$apiUrl$endpointVenues")
            .get()
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        println(body)
    }

    @Test
    fun testEvents() {
        val client = createHttpClient()
        val request = Request.Builder()
            .url("$apiUrl$endpointEvents")
            .get()
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        println(body)
    }

    @Test
    fun testArtists() {
        val client = createHttpClient()
        val request = Request.Builder()
            .url("$apiUrl$endpointArtists")
            .get()
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        println(body)
    }

    @Test
    fun testTimeslots() {
        val client = createHttpClient()
        val request = Request.Builder()
            .url("$apiUrl$endpointTimeslots")
            .get()
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        println(body)
    }

}