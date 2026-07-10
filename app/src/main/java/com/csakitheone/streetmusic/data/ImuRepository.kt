package com.csakitheone.streetmusic.data

import com.csakitheone.streetmusic.data.model.ExternalEvent
import java.time.LocalTime

/**
 * Íródeák Művészeti Udvar adatai
 */
class ImuRepository {
    companion object {

        val imuFacebookUrl = "https://www.facebook.com/events/1016707544548354"

        val events = listOf(
            // 07.22 - Szerda
            ExternalEvent("Klëo", 22, LocalTime.of(17, 15)),
            ExternalEvent("Ro-Bi 2 Men RT", 22, LocalTime.of(18, 25)),
            ExternalEvent("RoëN", 22, LocalTime.of(19, 35)),
            ExternalEvent("Percember Plays: Alice in Chains", 22, LocalTime.of(20, 45)),
            ExternalEvent("Burn on Board", 22, LocalTime.of(21, 55)),
            ExternalEvent("Éjfény", 22, LocalTime.of(23, 5)),

            // 07.23 - Csütörtök
            ExternalEvent("Fényhozó", 23, LocalTime.of(17, 15)),
            ExternalEvent("Nevünk Senki", 23, LocalTime.of(18, 25)),
            ExternalEvent("The Managers", 23, LocalTime.of(19, 35)),
            ExternalEvent("Dr. GROG", 23, LocalTime.of(20, 45)),
            ExternalEvent("M.C.S. Blues Band", 23, LocalTime.of(21, 55)),
            ExternalEvent("FeedBack", 23, LocalTime.of(23, 5)),
            ExternalEvent("Preach", 23, LocalTime.of(0, 15)),

            // 07.24 - Péntek
            ExternalEvent("BlackSmith", 24, LocalTime.of(17, 15)),
            ExternalEvent("Soul & Emotion", 24, LocalTime.of(18, 25)),
            ExternalEvent("Zella", 24, LocalTime.of(19, 35)),
            ExternalEvent("Carolina Reaper", 24, LocalTime.of(20, 45)),
            ExternalEvent("Dalok a Fiókból", 24, LocalTime.of(21, 55)),
            ExternalEvent("SUPERUNKNOWNS", 24, LocalTime.of(23, 5)),
            ExternalEvent("The Crazy Rogues", 24, LocalTime.of(0, 15)),

            // 07.25 - Szombat
            ExternalEvent("MeTa Duó", 25, LocalTime.of(17, 15)),
            ExternalEvent("Kaizen Kollektíve", 25, LocalTime.of(18, 25)),
            ExternalEvent("Picur", 25, LocalTime.of(19, 35)),
            ExternalEvent("IMU AS", 25, LocalTime.of(20, 45)),
            ExternalEvent("IMU AS", 25, LocalTime.of(21, 55)),
            ExternalEvent("Tengeri Püspök", 25, LocalTime.of(23, 5)),
            ExternalEvent("Dogs'N'Roses", 25, LocalTime.of(0, 15)),
        )

        val eventDays = events.map { it.day }.toSet().sorted()

    }
}
