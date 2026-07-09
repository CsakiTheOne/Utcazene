package com.csakitheone.streetmusic.data

import com.csakitheone.streetmusic.data.model.PontOttPartiEvent
import java.time.LocalTime

class GyarkertRepository {
    companion object {

        val url = "https://gyarkert.hu/"

        val pontOttPartiUrl = "https://uni-pannon.hu/pont-ott-parti"

        /**
         * Events on July 23.
         */
        val pontOttPartiEvents = listOf(
            PontOttPartiEvent(
                name = "Kapunyitás, HÖK elnöki köszöntő",
                startTime = LocalTime.of(17, 0),
            ),
            PontOttPartiEvent(
                name = "Warm up DJ - Shake It",
                startTime = LocalTime.of(17, 0),
                endTime = LocalTime.of(19, 0),
            ),
            PontOttPartiEvent(
                name = "Éberkóma koncert",
                startTime = LocalTime.of(19, 0),
                endTime = LocalTime.of(19, 50),
            ),
            PontOttPartiEvent(
                name = "Rektori köszöntő",
                startTime = LocalTime.of(19, 50),
                description = "Köszöntőt mond Dr. Abonyi János a Pannon Egyetem rektora.",
            ),
            PontOttPartiEvent(
                name = "Ponthatárok kihirdetése",
                startTime = LocalTime.of(20, 0),
            ),
            PontOttPartiEvent(
                name = "Brains koncert",
                startTime = LocalTime.of(20, 15),
                endTime = LocalTime.of(22, 0),
            ),
        )

        val day24facebookUrl = "https://www.facebook.com/events/4243592019197077/"

        val day25facebookUrl = "https://www.facebook.com/events/1606183307163353/"

    }
}