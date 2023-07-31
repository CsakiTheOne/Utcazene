package com.csakitheone.streetmusic.model

import androidx.annotation.Keep
import com.csakitheone.streetmusic.R
import com.google.firebase.firestore.Exclude

@Keep
data class Musician(
    val name: String = "",
    val description: String? = null,
    val country: String? = null,
    val imageUrl: String? = null,
    val youtubeUrl: String? = null,
    val tags: List<String>? = null,
    val years: List<Int>? = null,
) {
    @Exclude
    fun getFlag(): String {
        return countryFlags[country] ?: ""
    }

    fun isIncomplete(): Boolean {
        return description.isNullOrBlank() ||
                country.isNullOrBlank() ||
                imageUrl.isNullOrBlank() ||
                youtubeUrl.isNullOrBlank() ||
                years.isNullOrEmpty()
    }

    fun merge(other: Musician?): Musician {
        if (other == null) return this
        if (name != other.name || country != other.country) {
            throw Exception("Can't merge musicians with different name or country!")
        }
        if (
            (!description.isNullOrBlank() && !other.description.isNullOrBlank() && description != other.description) ||
            (!imageUrl.isNullOrBlank() && !other.imageUrl.isNullOrBlank() && imageUrl != other.imageUrl) ||
            (!youtubeUrl.isNullOrBlank() && !other.youtubeUrl.isNullOrBlank() && youtubeUrl != other.youtubeUrl)
        ) {
            throw Exception("Merge conflict: can't decide which value to use at $name")
        }
        return Musician(
            name = name,
            description = if (!description.isNullOrBlank()) description else other.description,
            country = if (!country.isNullOrBlank()) country else other.country,
            imageUrl = if (!imageUrl.isNullOrBlank()) imageUrl else other.imageUrl,
            youtubeUrl = if (!youtubeUrl.isNullOrBlank()) youtubeUrl else other.youtubeUrl,
            tags = ((tags ?: listOf()) + (other.tags ?: listOf())).distinct(),
            years = ((years ?: listOf()) + (other.years ?: listOf())).distinct(),
        )
    }

    /*override fun equals(other: Any?): Boolean {
        if (other !is Musician) return false
        return name.equals(other.name, true)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }*/

    companion object {

        val TAG_FOREIGN = "foreign"//R.string.musician_tag_foreign
        val TAG_FRIEND = "friend"//R.string.musician_tag_friend
        val TAG_COMPETING = "competing"//R.string.musician_tag_competing
        val TAG_TEREM = "terem"//R.string.musician_tag_terem

        val tagStrings = mapOf(
            TAG_FOREIGN to R.string.musician_tag_foreign,
            TAG_FRIEND to R.string.musician_tag_friend,
            TAG_COMPETING to R.string.musician_tag_competing,
            TAG_TEREM to R.string.musician_tag_terem,
        )

        val countryFlags = mapOf(
            "A" to "🇦🇹",
            "AUS" to "🇦🇹",
            "B" to "🇧🇪",
            "CZ" to "🇨🇿",
            "D" to "🇩🇪",
            "FR" to "🇫🇷",
            "IRE" to "🇮🇪",
            "IT" to "🇮🇹",
            "HU" to "🇭🇺",
            "NL" to "🇳🇱",
            "NZ" to "🇳🇿",
            "P" to "🇵🇹",
            "PL" to "🇵🇱",
            "UK" to "🇬🇧",
            "US" to "🇺🇸",
            "ZA" to "🇿🇦",
        )

        fun fromString(string: String): Musician {
            if (!string.contains("(")) {
                return Musician(name = string)
            }
            val name = string.substringBefore(" (")
            val countryCode = string.substringAfter("(").removeSuffix(")")
            return Musician(
                name = name,
                country = if (countryFlags.containsKey(countryCode)) "${countryFlags[countryCode]} $countryCode"
                else countryCode,
            )
        }

    }
}