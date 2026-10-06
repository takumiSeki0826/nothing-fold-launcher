package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeatherWidget(
    weather: WeatherState?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (weather == null) return

    Column(modifier = modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DotMatrixIcon(
                rows = iconFor(weatherConditionFor(weather.weatherCode)),
                color = Color.White,
                dotSize = WEATHER_DOT_SIZE,
            )
            Spacer(modifier = Modifier.width(4.dp))
            DotMatrixText(text = formatTemperature(weather.temperatureCelsius), fontSize = 12.sp, color = Color.White)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            DotMatrixIcon(rows = DotIcons.DROP, color = Color.Gray, dotSize = DETAIL_DOT_SIZE)
            Spacer(modifier = Modifier.width(2.dp))
            DotMatrixText(text = "${weather.humidityPercent}%", fontSize = 10.sp, color = Color.Gray)
            Spacer(modifier = Modifier.width(6.dp))
            DotMatrixIcon(rows = DotIcons.UMBRELLA, color = Color.Gray, dotSize = DETAIL_DOT_SIZE)
            Spacer(modifier = Modifier.width(2.dp))
            DotMatrixText(text = "${weather.precipitationProbabilityPercent}%", fontSize = 10.sp, color = Color.Gray)
        }
    }
}

private val WEATHER_DOT_SIZE = 1.4.dp
private val DETAIL_DOT_SIZE = 1.2.dp

private fun iconFor(condition: WeatherCondition): List<String> = when (condition) {
    WeatherCondition.CLEAR -> DotIcons.SUN
    WeatherCondition.PARTLY_CLOUDY -> DotIcons.PARTLY_CLOUDY
    WeatherCondition.CLOUDY -> DotIcons.CLOUD
    WeatherCondition.FOG -> DotIcons.FOG
    WeatherCondition.DRIZZLE, WeatherCondition.RAIN -> DotIcons.RAIN
    WeatherCondition.SNOW -> DotIcons.SNOW
    WeatherCondition.THUNDERSTORM -> DotIcons.THUNDERSTORM
    WeatherCondition.UNKNOWN -> dotMatrixGlyph('?')
}
