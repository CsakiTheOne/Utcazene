package com.csakitheone.streetmusic.model

import androidx.annotation.Keep

@Keep
data class Event(
    val author: Author,
    val day: Int,
    val time: String,
    val place: Place,
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Event) return false
        return author == other.author && day == other.day && time == other.time && place == other.place
    }
}