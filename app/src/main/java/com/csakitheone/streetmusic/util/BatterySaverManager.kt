package com.csakitheone.streetmusic.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BatterySaverManager {
    companion object {

        var isBatterySaverEnabled by mutableStateOf(false)

    }
}