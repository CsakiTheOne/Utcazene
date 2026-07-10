package com.csakitheone.streetmusic.data

import com.csakitheone.streetmusic.data.model.Event
import com.csakitheone.streetmusic.data.model.ExternalEvent
import java.time.LocalTime

class CombinedRepository {
    companion object {
        /**
         * Returns a list of [ExternalEvent]s combined from multiple sources for a given day.
         * Day is the day of the month (e.g. 23).
         */
        fun getCombinedEventsForDay(day: Int, utcazeneEvents: List<Event>): List<ExternalEvent> {
            val externalEvents = (ImuRepository.events + GyarkertRepository.pontOttPartiEvents)
                .filter { it.day == day }
            
            val mappedUtcazene = utcazeneEvents
                .filter { it.startTime.substring(8, 10).toInt() == day }
                .map { it.toExternalEvent() }

            return (externalEvents + mappedUtcazene).sortedBy { it.startTime }
        }
    }
}
