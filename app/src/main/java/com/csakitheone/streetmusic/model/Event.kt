package com.csakitheone.streetmusic.model

import androidx.annotation.Keep

@Keep
data class Event(
    val musician: Musician,
    val day: Int,
    val time: String,
    val place: Place,
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Event) return false
        return musician == other.musician && day == other.day && time == other.time
    }
}