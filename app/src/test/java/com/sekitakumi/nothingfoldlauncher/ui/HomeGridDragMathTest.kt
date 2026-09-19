package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeGridDragMathTest {

    private val slotA = Rect(0f, 0f, 10f, 10f) // center (5, 5)
    private val slotB = Rect(20f, 0f, 30f, 10f) // center (25, 5)

    @Test
    fun `picks the slot whose center is closest to the position`() {
        val index = nearestSlotIndex(Offset(6f, 5f), listOf(slotA, slotB))
        assertEquals(0, index)
    }

    @Test
    fun `picks the other slot when the position is closer to it`() {
        val index = nearestSlotIndex(Offset(24f, 5f), listOf(slotA, slotB))
        assertEquals(1, index)
    }

    @Test
    fun `skips null bounds for slots that have not been measured yet`() {
        val index = nearestSlotIndex(Offset(24f, 5f), listOf(slotA, null))
        assertEquals(0, index)
    }

    @Test
    fun `returns null when there are no measured slots`() {
        val index = nearestSlotIndex(Offset(0f, 0f), listOf(null, null))
        assertNull(index)
    }

    @Test
    fun `returns null for an empty list`() {
        assertNull(nearestSlotIndex(Offset(0f, 0f), emptyList()))
    }
}
