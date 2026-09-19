package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
            Icon(
                imageVector = iconFor(weatherConditionFor(weather.weatherCode)),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.width(16.dp).height(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = formatTemperature(weather.temperatureCelsius), color = Color.White, fontSize = 12.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.width(10.dp).height(10.dp),
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = "${weather.humidityPercent}%", color = Color.Gray, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Filled.Umbrella,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.width(10.dp).height(10.dp),
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = "${weather.precipitationProbabilityPercent}%", color = Color.Gray, fontSize = 10.sp)
        }
    }
}

private fun iconFor(condition: WeatherCondition): ImageVector = when (condition) {
    WeatherCondition.CLEAR -> Icons.Filled.WbSunny
    WeatherCondition.PARTLY_CLOUDY -> Icons.Filled.WbCloudy
    WeatherCondition.CLOUDY -> Icons.Filled.Cloud
    WeatherCondition.FOG -> Icons.Filled.Dehaze
    WeatherCondition.DRIZZLE, WeatherCondition.RAIN -> Icons.Filled.Grain
    WeatherCondition.SNOW -> Icons.Filled.AcUnit
    WeatherCondition.THUNDERSTORM -> Icons.Filled.Thunderstorm
    WeatherCondition.UNKNOWN -> Icons.AutoMirrored.Filled.HelpOutline
}
