package com.csakitheone.streetmusic.data

import com.csakitheone.streetmusic.data.model.Artist
import com.csakitheone.streetmusic.data.model.Event
import com.csakitheone.streetmusic.data.model.ExternalEvent
import java.time.LocalDateTime
import java.time.LocalTime

class CombinedRepository {
    companion object {
        /**
         * Returns a list of [Event]s and [ExternalEvent]s combined from multiple sources.
         */
        fun getCombinedEvents(utcazeneEvents: List<Event>): List<Any> {
            val externalEvents = (ImuRepository.events + GyarkertRepository.pontOttPartiEvents)
            val mappedUtcazene = utcazeneEvents.map { it.toExternalEvent() }
            return (externalEvents + mappedUtcazene)
                .sortedBy { it.day * 24 * 60 * 60 + it.startTime.toSecondOfDay() }
                .map { externalEvent ->
                    if (externalEvent.description?.startsWith("Utcazene event") == true) {
                        val id = externalEvent.description.substringAfter("#").substringBefore(".")
                        val utcazeneEvent = utcazeneEvents.find { it.id == id.toInt() }
                        if (utcazeneEvent != null) {
                            return@map utcazeneEvent.toExternalEvent()
                        } else {
                            return@map externalEvent
                        }
                    }
                    return@map externalEvent
                }
        }

        /**
         * Returns a list of [Event]s and [ExternalEvent]s combined from multiple sources for a given day.
         * @param day The day to get events for.
         */
        fun getCombinedEventsForDay(day: Int, utcazeneEvents: List<Event>): List<Any> {
            val externalEvents = (ImuRepository.events + GyarkertRepository.pontOttPartiEvents)
                .filter { it.day == day }

            val mappedUtcazene = utcazeneEvents
                .filter { it.startTime.substring(8, 10).toInt() == day }
                .map { it.toExternalEvent() }

            return (externalEvents + mappedUtcazene)
                .sortedBy { it.startTime.toSecondOfDay() }
                .map { externalEvent ->
                    if (externalEvent.description?.startsWith("Utcazene event") == true) {
                        val id = externalEvent.description.substringAfter("#").substringBefore(".")
                        val utcazeneEvent = utcazeneEvents.find { it.id == id.toInt() }
                        if (utcazeneEvent != null) {
                            return@map utcazeneEvent.toExternalEvent()
                        } else {
                            return@map externalEvent
                        }
                    }
                    return@map externalEvent
                }
        }

        fun getSlugForAny(any: Any): String {
            return when (any) {
                is Artist -> any.slug
                is Event -> "${any.artistSlug} at ${any.startTime}"
                is ExternalEvent -> any.slug
                else -> throw IllegalArgumentException("Unknown type")
            }
        }

        fun getIsAfterAny(any: Any, now: LocalDateTime): Boolean {
            return when (any) {
                is Event -> LocalDateTime.parse(any.startTime).isAfter(now)
                is ExternalEvent -> any.startTime.isAfter(now.toLocalTime())
                else -> throw IllegalArgumentException("Unknown type")
            }
        }
    }
}
