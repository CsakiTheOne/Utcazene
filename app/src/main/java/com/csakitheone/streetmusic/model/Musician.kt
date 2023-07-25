package com.csakitheone.streetmusic.model

import androidx.annotation.Keep
import com.csakitheone.streetmusic.R

@Keep
data class Musician(
    val name: String = "",
    val description: String? = null,
    val country: String? = null,
    val imageUrl: String? = null,
    val youtubeUrl: String? = null,
    val tags: List<Int>? = null,
    val years: List<Int>? = null,
) {
    fun getFlag(): String {
        return countryFlags[country] ?: ""
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Musician) return false
        return name.equals(other.name, true)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    companion object {

        val TAG_FOREIGN = R.string.musician_tag_foreign
        val TAG_FRIEND = R.string.musician_tag_friend
        val TAG_COMPETING = R.string.musician_tag_competing
        val TAG_TEREM = R.string.musician_tag_terem

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