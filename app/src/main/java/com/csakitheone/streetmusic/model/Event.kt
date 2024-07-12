package com.csakitheone.streetmusic.model

import androidx.annotation.Keep

@Keep
data class Event(
    val id: Int,
    val musician: Musician,
    val year: Int,
    val day: Int,
    val time: String,
    val place: Place,
) {
    override fun toString(): String {
        return "$musician $year-$day $time"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Event) return false
        return musician == other.musician && day == other.day && time == other.time
    }
}