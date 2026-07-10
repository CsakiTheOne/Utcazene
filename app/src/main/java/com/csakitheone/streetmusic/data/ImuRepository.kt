package com.csakitheone.streetmusic.data

import com.csakitheone.streetmusic.data.model.ImuEvent
import java.time.LocalTime

/**
 * Íródeák Művészeti Udvar adatai
 */
class ImuRepository {
    companion object {

        val imuFacebookUrl = "https://www.facebook.com/imuveszprem/"

        val events = listOf(
            // 07.22 - Szerda
            ImuEvent("Klëo", 22, LocalTime.of(17, 15)),
            ImuEvent("Ro-Bi 2 Men RT", 22, LocalTime.of(18, 25)),
            ImuEvent("RoëN", 22, LocalTime.of(19, 35)),
            ImuEvent("Percember Plays: Alice in Chains", 22, LocalTime.of(20, 45)),
            ImuEvent("Burn on Board", 22, LocalTime.of(21, 55)),
            ImuEvent("Éjfény", 22, LocalTime.of(23, 5)),

            // 07.23 - Csütörtök
            ImuEvent("Fényhozó", 23, LocalTime.of(17, 15)),
            ImuEvent("Nevünk Senki", 23, LocalTime.of(18, 25)),
            ImuEvent("The Managers", 23, LocalTime.of(19, 35)),
            ImuEvent("Dr. GROG", 23, LocalTime.of(20, 45)),
            ImuEvent("M.C.S. Blues Band", 23, LocalTime.of(21, 55)),
            ImuEvent("FeedBack", 23, LocalTime.of(23, 5)),
            ImuEvent("Preach", 23, LocalTime.of(0, 15)),

            // 07.24 - Péntek
            ImuEvent("BlackSmith", 24, LocalTime.of(17, 15)),
            ImuEvent("Soul & Emotion", 24, LocalTime.of(18, 25)),
            ImuEvent("Zella", 24, LocalTime.of(19, 35)),
            ImuEvent("Carolina Reaper", 24, LocalTime.of(20, 45)),
            ImuEvent("Dalok a Fiókból", 24, LocalTime.of(21, 55)),
            ImuEvent("SUPERUNKNOWNS", 24, LocalTime.of(23, 5)),
            ImuEvent("The Crazy Rogues", 24, LocalTime.of(0, 15)),

            // 07.25 - Szombat
            ImuEvent("MeTa Duó", 25, LocalTime.of(17, 15)),
            ImuEvent("Kaizen Kollektíve", 25, LocalTime.of(18, 25)),
            ImuEvent("Picur", 25, LocalTime.of(19, 35)),
            ImuEvent("IMU AS", 25, LocalTime.of(20, 45)),
            ImuEvent("IMU AS", 25, LocalTime.of(21, 55)),
            ImuEvent("Tengeri Püspök", 25, LocalTime.of(23, 5)),
            ImuEvent("Dogs'N'Roses", 25, LocalTime.of(0, 15)),
        )

        val eventDays = events.map { it.day }.toSet().sorted()

    }
}
