package com.csakitheone.streetmusic.data.model

import java.time.LocalTime

data class UnlockFestEvent(
    val name: String,
    val url: String? = null,
    val day: Int,
    val order: Int,
)

data class PontOttPartiEvent(
    val name: String,
    val startTime: LocalTime,
    val endTime: LocalTime? = null,
    val description: String? = null,
)
