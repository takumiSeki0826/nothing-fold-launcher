package com.sekitakumi.nothingfoldlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeOrderTest {

    @Test
    fun `appRef prefixes the package name`() {
        assertEquals("app:com.example.a", appRef("com.example.a"))
    }

    @Test
    fun `folderRef prefixes the folder id`() {
        assertEquals("folder:abc123", folderRef("abc123"))
    }

    @Test
    fun `reconcileHomeOrder keeps existing refs in their stored order`() {
        val stored = listOf("app:b", "app:a")
        val valid = listOf("app:a", "app:b")
        assertEquals(listOf("app:b", "app:a"), reconcileHomeOrder(stored, valid))
    }

    @Test
    fun `reconcileHomeOrder appends new refs not present in the stored order`() {
        val stored = listOf("app:a")
        val valid = listOf("app:a", "app:b")
        assertEquals(listOf("app:a", "app:b"), reconcileHomeOrder(stored, valid))
    }

    @Test
    fun `reconcileHomeOrder drops stored refs that are no longer valid`() {
        val stored = listOf("app:a", "app:b")
        val valid = listOf("app:b")
        assertEquals(listOf("app:b"), reconcileHomeOrder(stored, valid))
    }

    @Test
    fun `reconcileHomeOrder with empty stored order returns valid refs in order`() {
        assertEquals(listOf("app:a", "app:b"), reconcileHomeOrder(emptyList(), listOf("app:a", "app:b")))
    }

    @Test
    fun `swapHomeOrder exchanges the positions of two refs`() {
        val order = listOf("app:a", "app:b", "app:c")
        assertEquals(listOf("app:c", "app:b", "app:a"), swapHomeOrder(order, "app:a", "app:c"))
    }

    @Test
    fun `swapHomeOrder returns the order unchanged when a ref is missing`() {
        val order = listOf("app:a", "app:b")
        assertEquals(order, swapHomeOrder(order, "app:a", "app:missing"))
    }

    @Test
    fun `moveHomeOrderToEnd moves the ref to the end`() {
        val order = listOf("app:a", "app:b", "app:c")
        assertEquals(listOf("app:a", "app:c", "app:b"), moveHomeOrderToEnd(order, "app:b"))
    }

    @Test
    fun `moveHomeOrderToEnd returns the order unchanged when the ref is missing`() {
        val order = listOf("app:a", "app:b")
        assertEquals(order, moveHomeOrderToEnd(order, "app:missing"))
    }
}
