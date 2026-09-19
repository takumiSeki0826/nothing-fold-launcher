package com.sekitakumi.nothingfoldlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PackageListEncodingTest {

    @Test
    fun `encodePackageList joins packages with commas`() {
        assertEquals(
            "com.example.a,com.example.b",
            encodePackageList(listOf("com.example.a", "com.example.b")),
        )
    }

    @Test
    fun `encodePackageList returns empty string for empty list`() {
        assertEquals("", encodePackageList(emptyList()))
    }

    @Test
    fun `decodePackageList splits comma separated packages`() {
        assertEquals(
            listOf("com.example.a", "com.example.b"),
            decodePackageList("com.example.a,com.example.b"),
        )
    }

    @Test
    fun `decodePackageList treats a value with no commas as a single element list`() {
        // Backward compatibility: pre-existing single package names stored before
        // folders existed must still decode correctly.
        assertEquals(listOf("com.example.a"), decodePackageList("com.example.a"))
    }

    @Test
    fun `decodePackageList returns empty list for null`() {
        assertEquals(emptyList<String>(), decodePackageList(null))
    }

    @Test
    fun `decodePackageList returns empty list for blank string`() {
        assertEquals(emptyList<String>(), decodePackageList(""))
    }

    @Test
    fun `decodePackageList filters out blank segments`() {
        assertEquals(listOf("com.example.a", "com.example.b"), decodePackageList("com.example.a,,com.example.b,"))
    }
}
