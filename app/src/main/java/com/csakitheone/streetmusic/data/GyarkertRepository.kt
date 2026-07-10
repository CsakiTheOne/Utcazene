package com.csakitheone.streetmusic.data

import com.csakitheone.streetmusic.data.model.ExternalEvent
import java.time.LocalTime

class GyarkertRepository {
    companion object {

        val url = "https://gyarkert.hu/"

        val pontOttPartiUrl = "https://uni-pannon.hu/pont-ott-parti"

        /**
         * Events on July 23.
         */
        val pontOttPartiEvents = listOf(
            ExternalEvent(
                name = "Kapunyitás, HÖK elnöki köszöntő",
                day = 23,
                startTime = LocalTime.of(17, 0),
            ),
            ExternalEvent(
                name = "Warm up DJ - Shake It",
                day = 23,
                startTime = LocalTime.of(17, 0),
                endTime = LocalTime.of(19, 0),
            ),
            ExternalEvent(
                name = "Éberkóma koncert",
                day = 23,
                startTime = LocalTime.of(19, 0),
                endTime = LocalTime.of(19, 50),
            ),
            ExternalEvent(
                name = "Rektori köszöntő",
                day = 23,
                startTime = LocalTime.of(19, 50),
                description = "Köszöntőt mond Dr. Abonyi János a Pannon Egyetem rektora.",
            ),
            ExternalEvent(
                name = "Ponthatárok kihirdetése",
                day = 23,
                startTime = LocalTime.of(20, 0),
            ),
            ExternalEvent(
                name = "Brains koncert",
                day = 23,
                startTime = LocalTime.of(20, 15),
                endTime = LocalTime.of(22, 0),
            ),
        )

        val day24facebookUrl = "https://www.facebook.com/events/4243592019197077/"

        val day25facebookUrl = "https://www.facebook.com/events/1606183307163353/"

    }
}