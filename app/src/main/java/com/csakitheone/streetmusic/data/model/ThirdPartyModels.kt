package com.csakitheone.streetmusic.data.model

import java.time.LocalTime

data class UnlockFestEvent(
    val name: String,
    val url: String? = null,
    val day: Int,
    val order: Int,
)

data class ExternalEvent(
    val name: String,
    val day: Int,
    val startTime: LocalTime,
    val endTime: LocalTime? = null,
    val description: String? = null,
    val customSlug: String? = null,
) {
    val slug: String get() = customSlug ?: "$name at $day $startTime"
}
