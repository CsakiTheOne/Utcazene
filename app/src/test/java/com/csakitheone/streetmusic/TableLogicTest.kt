package com.csakitheone.streetmusic

import com.csakitheone.streetmusic.data.model.TableItem
import org.junit.Assert.*
import org.junit.Test

class TableLogicTest {

    @Test
    fun `test card stack shuffle`() {
        val originalCards = (0..51).toList()
        val stack = TableItem.CardStack("1", 0f, 0f, cards = originalCards)
        val shuffledStack = stack.copy(cards = stack.cards.shuffled())
        
        assertEquals(originalCards.size, shuffledStack.cards.size)
        assertNotEquals(originalCards, shuffledStack.cards)
    }

    @Test
    fun `test card stack merge`() {
        val stack1 = TableItem.CardStack("1", 0f, 0f, cards = listOf(0, 1, 2))
        val stack2 = TableItem.CardStack("2", 0f, 0f, cards = listOf(3, 4, 5))
        
        val mergedStack = stack1.copy(cards = stack1.cards + stack2.cards)
        
        assertEquals(6, mergedStack.cards.size)
        assertEquals(listOf(0, 1, 2, 3, 4, 5), mergedStack.cards)
    }

    @Test
    fun `test P2P synchronization logic`() {
        // Simulating the logic in FriendsFeature.allTableItems
        val localItems = mutableMapOf<String, TableItem>(
            "item1" to TableItem.Die("item1", 0.1f, 0.1f, lastUpdated = 100, value = 1)
        )
        
        val receivedItems = listOf(
            TableItem.Die("item1", 0.2f, 0.2f, lastUpdated = 200, value = 6), // Newer
            TableItem.Die("item2", 0.5f, 0.5f, lastUpdated = 150, value = 3)  // New item
        )
        
        val all = localItems.toMutableMap()
        receivedItems.forEach { item ->
            val existing = all[item.id]
            if (existing == null || item.lastUpdated > existing.lastUpdated) {
                all[item.id] = item
            }
        }
        
        val finalItems = all.values.toList()
        assertEquals(2, finalItems.size)
        
        val item1 = finalItems.find { it.id == "item1" } as TableItem.Die
        assertEquals(6, item1.value)
        assertEquals(0.2f, item1.x)
    }
}
