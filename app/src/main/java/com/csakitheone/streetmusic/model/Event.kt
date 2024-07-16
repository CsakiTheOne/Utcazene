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
    fun toStringDayTimeAndName(): String {
        return "$day. $time ${musician.name}"
    }

    fun toStringTimeAndName(): String {
        return "$time ${musician.name}"
    }

    //TODO: use musician's name instead of musician.toString() next year
    override fun toString(): String {
        return "$musician $year-$day $time"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Event) return false
        return musician.name == other.musician.name && day == other.day && time == other.time
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + musician.hashCode()
        result = 31 * result + year
        result = 31 * result + day
        result = 31 * result + time.hashCode()
        result = 31 * result + place.hashCode()
        return result
    }
}