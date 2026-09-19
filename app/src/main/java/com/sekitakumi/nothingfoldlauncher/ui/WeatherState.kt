package com.sekitakumi.nothingfoldlauncher.ui

data class WeatherState(
    val temperatureCelsius: Double,
    val weatherCode: Int,
    val humidityPercent: Int,
    val precipitationProbabilityPercent: Int,
    val fetchedAtMillis: Long,
)
