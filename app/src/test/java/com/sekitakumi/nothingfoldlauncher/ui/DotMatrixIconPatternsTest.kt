package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DotMatrixIconPatternsTest {

    private fun List<String>.count(c: Char) = sumOf { row -> row.count { it == c } }

    private val fixedIcons: Map<String, List<String>> = mapOf(
        "wifi" to DotIcons.WIFI,
        "bolt" to DotIcons.BOLT,
        "tailscale" to DotIcons.TAILSCALE,
        "sun" to DotIcons.SUN,
        "partlyCloudy" to DotIcons.PARTLY_CLOUDY,
        "cloud" to DotIcons.CLOUD,
        "fog" to DotIcons.FOG,
        "rain" to DotIcons.RAIN,
        "snow" to DotIcons.SNOW,
        "thunderstorm" to DotIcons.THUNDERSTORM,
        "drop" to DotIcons.DROP,
        "umbrella" to DotIcons.UMBRELLA,
        "play" to DotIcons.PLAY,
        "pause" to DotIcons.PAUSE,
        "bell" to DotIcons.BELL,
        "chevronRight" to DotIcons.CHEVRON_RIGHT,
        "checkOn" to DotIcons.CHECK_ON,
        "checkOff" to DotIcons.CHECK_OFF,
        "star" to DotIcons.STAR,
    )

    @Test
    fun `every fixed icon is a non-empty rectangle of known cells`() {
        fixedIcons.forEach { (name, rows) ->
            assertTrue("$name is empty", rows.isNotEmpty())
            assertTrue("$name has ragged rows", rows.all { it.length == rows[0].length })
            assertTrue("$name has unknown cells", rows.all { r -> r.all { it in "#o." } })
            assertTrue("$name has no lit dot", rows.count('#') > 0)
        }
    }

    @Test
    fun `signalBarsPattern has 4 bars of increasing height`() {
        val p = signalBarsPattern(4)
        assertEquals(5, p.size)
        assertTrue(p.all { it.length == 7 })
        assertEquals(2 + 3 + 4 + 5, p.count('#'))
        assertEquals(0, p.count('o'))
    }

    @Test
    fun `signalBarsPattern lights only the first n bars and dims the rest`() {
        val p = signalBarsPattern(2)
        assertEquals(2 + 3, p.count('#'))
        assertEquals(4 + 5, p.count('o'))
        assertEquals(0, signalBarsPattern(0).count('#'))
    }

    @Test
    fun `signalBarsPattern clamps out of range bars`() {
        assertEquals(signalBarsPattern(4), signalBarsPattern(9))
        assertEquals(signalBarsPattern(0), signalBarsPattern(-1))
    }

    @Test
    fun `batteryPattern fills inner columns in proportion to percent`() {
        val empty = batteryPattern(0)
        assertTrue(empty.all { it.length == empty[0].length })
        val perCol = BATTERY_FILL_ROWS
        assertEquals(perCol * BATTERY_FILL_COLS, batteryPattern(100).count('#') - empty.count('#'))
        assertEquals(perCol * (BATTERY_FILL_COLS / 2), batteryPattern(50).count('#') - empty.count('#'))
    }

    @Test
    fun `batteryPattern shows at least one column for any positive percent`() {
        val empty = batteryPattern(0)
        assertEquals(BATTERY_FILL_ROWS, batteryPattern(1).count('#') - empty.count('#'))
    }

    @Test
    fun `batteryPattern clamps out of range percent`() {
        assertEquals(batteryPattern(100), batteryPattern(150))
        assertEquals(batteryPattern(0), batteryPattern(-5))
    }

    @Test
    fun `icon size follows the glyph grid`() {
        val rows = listOf("#.#", "...")
        assertEquals(2 * 1.5f + 1f, dotMatrixIconWidthPx(rows, 1f), 0.001f)
        assertEquals(1 * 1.5f + 1f, dotMatrixIconHeightPx(rows, 1f), 0.001f)
    }

    @Test
    fun `wifi icon is narrow enough to sit beside the other status icons`() {
        assertTrue("wifi is too wide", DotIcons.WIFI[0].length <= 9)
        assertEquals(6, DotIcons.WIFI.size)
    }
}
