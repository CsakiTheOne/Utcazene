package com.csakitheone.streetmusic.data.api

import android.annotation.SuppressLint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class UtcazeneApi {
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://utcazene.hu/api"

    suspend fun fetchArtists(): List<ApiArtist> = fetch("$baseUrl/artists/")

    /**
     * Creates an OkHttpClient that trusts all certificates.
     * WARNING: This is highly insecure.
     */
    @SuppressLint("CustomX509TrustManager")
    fun unsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    private suspend inline fun <reified T> fetch(url: String): T = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        unsafeOkHttpClient().newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val body = response.body.string()
            return@withContext json.decodeFromString<T>(body)
        }
    }
}
