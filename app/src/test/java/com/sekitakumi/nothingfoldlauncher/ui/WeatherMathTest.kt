package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherMathTest {

    @Test
    fun `isWeatherCacheFresh is true right after fetching`() {
        assertTrue(isWeatherCacheFresh(fetchedAtMillis = 1000L, nowMillis = 1000L))
    }

    @Test
    fun `isWeatherCacheFresh is true just under the ttl`() {
        val ttlMillis = 30 * 60 * 1000L
        assertTrue(isWeatherCacheFresh(fetchedAtMillis = 0L, nowMillis = ttlMillis - 1))
    }

    @Test
    fun `isWeatherCacheFresh is false at exactly the ttl`() {
        val ttlMillis = 30 * 60 * 1000L
        assertFalse(isWeatherCacheFresh(fetchedAtMillis = 0L, nowMillis = ttlMillis))
    }

    @Test
    fun `isWeatherCacheFresh is false well past the ttl`() {
        val ttlMillis = 30 * 60 * 1000L
        assertFalse(isWeatherCacheFresh(fetchedAtMillis = 0L, nowMillis = ttlMillis + 60_000L))
    }

    @Test
    fun `isWeatherCacheFresh is false when fetchedAtMillis is in the future`() {
        assertFalse(isWeatherCacheFresh(fetchedAtMillis = 5000L, nowMillis = 1000L))
    }

    @Test
    fun `weatherConditionFor maps representative WMO codes`() {
        assertEquals(WeatherCondition.CLEAR, weatherConditionFor(0))
        assertEquals(WeatherCondition.PARTLY_CLOUDY, weatherConditionFor(2))
        assertEquals(WeatherCondition.CLOUDY, weatherConditionFor(3))
        assertEquals(WeatherCondition.FOG, weatherConditionFor(45))
        assertEquals(WeatherCondition.DRIZZLE, weatherConditionFor(55))
        assertEquals(WeatherCondition.RAIN, weatherConditionFor(61))
        assertEquals(WeatherCondition.SNOW, weatherConditionFor(71))
        assertEquals(WeatherCondition.THUNDERSTORM, weatherConditionFor(95))
    }

    @Test
    fun `weatherConditionFor falls back to unknown for an unmapped code`() {
        assertEquals(WeatherCondition.UNKNOWN, weatherConditionFor(999))
    }

    @Test
    fun `formatTemperature rounds to the nearest degree`() {
        assertEquals("12°", formatTemperature(12.4))
        assertEquals("13°", formatTemperature(12.5))
        assertEquals("-3°", formatTemperature(-3.2))
    }
}
