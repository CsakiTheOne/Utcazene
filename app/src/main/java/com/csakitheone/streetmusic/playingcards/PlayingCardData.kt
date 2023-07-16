package com.csakitheone.streetmusic.playingcards

import androidx.compose.ui.graphics.Color
import java.util.Stack

data class PlayingCardData(
    val suit: Suit,
    val value: Int,
) {
    val label: String
        get() = when(value) {
            1 -> "A"
            11 -> "J"
            12 -> "D"
            13 -> "K"
            else -> value.toString()
        }

    override fun toString(): String {
        return "$label${suit.symbol}"
    }

    enum class Suit(
        val symbol: String,
        val color: Color,
    ) {
        CLUBS(symbol = "♣️", color = Color.Black),
        DIAMONDS(symbol = "♦️", color = Color.Red),
        HEARTS(symbol = "♥️", color = Color.Red),
        SPADES(symbol = "♠️", color = Color.Black),
    }

    companion object {
        fun random(): PlayingCardData {
            return PlayingCardData(Suit.values().random(), (1..13).random())
        }

        fun deck(numberOfDecks: Int = 1): List<PlayingCardData> {
            if (!(1..4).contains(numberOfDecks)) {
                return listOf()
            }
            val cards = mutableListOf<PlayingCardData>()
            repeat(numberOfDecks) {
                Suit.values().forEach { suit ->
                    (1..13).forEach { value ->
                        cards.add(PlayingCardData(suit = suit, value = value))
                    }
                }
            }
            return cards
        }
    }
}