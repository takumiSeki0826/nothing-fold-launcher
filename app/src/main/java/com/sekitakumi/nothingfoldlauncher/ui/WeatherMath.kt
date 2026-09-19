package com.sekitakumi.nothingfoldlauncher.ui

import kotlin.math.roundToInt

private const val CACHE_TTL_MILLIS = 30 * 60 * 1000L

fun isWeatherCacheFresh(fetchedAtMillis: Long, nowMillis: Long): Boolean =
    nowMillis - fetchedAtMillis in 0 until CACHE_TTL_MILLIS

enum class WeatherCondition {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDERSTORM,
    UNKNOWN,
}

/** Maps an Open-Meteo WMO weather code to a coarse condition for icon selection. */
fun weatherConditionFor(weatherCode: Int): WeatherCondition = when (weatherCode) {
    0 -> WeatherCondition.CLEAR
    1, 2 -> WeatherCondition.PARTLY_CLOUDY
    3 -> WeatherCondition.CLOUDY
    45, 48 -> WeatherCondition.FOG
    51, 53, 55, 56, 57 -> WeatherCondition.DRIZZLE
    61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.RAIN
    71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
    95, 96, 99 -> WeatherCondition.THUNDERSTORM
    else -> WeatherCondition.UNKNOWN
}

fun formatTemperature(celsius: Double): String = "${celsius.roundToInt()}°"
