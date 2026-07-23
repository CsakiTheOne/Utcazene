package com.csakitheone.streetmusic.data.model

import kotlinx.serialization.Serializable

@Serializable
sealed class TableItem {
    abstract val id: String
    abstract val x: Float
    abstract val y: Float
    abstract val lastUpdated: Long
    abstract val isDeleted: Boolean

    @Serializable
    data class Die(
        override val id: String,
        override val x: Float,
        override val y: Float,
        override val lastUpdated: Long = System.currentTimeMillis(),
        override val isDeleted: Boolean = false,
        val value: Int = (1..6).random()
    ) : TableItem()

    @Serializable
    data class CardStack(
        override val id: String,
        override val x: Float,
        override val y: Float,
        override val lastUpdated: Long = System.currentTimeMillis(),
        override val isDeleted: Boolean = false,
        val cards: List<Int>, // 0-51: (index / 13) is suit, (index % 13) is rank
        val isFlipped: Boolean = false
    ) : TableItem()

    @Serializable
    data class Coin(
        override val id: String,
        override val x: Float,
        override val y: Float,
        override val lastUpdated: Long = System.currentTimeMillis(),
        override val isDeleted: Boolean = false,
        val isHeads: Boolean = true
    ) : TableItem()

    @Serializable
    data class Counter(
        override val id: String,
        override val x: Float,
        override val y: Float,
        override val lastUpdated: Long = System.currentTimeMillis(),
        override val isDeleted: Boolean = false,
        val count: Int = 0,
        val label: String = ""
    ) : TableItem()
}
