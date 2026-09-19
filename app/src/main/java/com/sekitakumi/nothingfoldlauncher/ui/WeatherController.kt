package com.sekitakumi.nothingfoldlauncher.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject

private const val TAG = "WeatherController"
private const val LOCATION_TIMEOUT_MILLIS = 10_000L
private const val REQUEST_TIMEOUT_MILLIS = 10_000

/**
 * Weather is polled on demand rather than subscribed to like the other
 * controllers (StatusIconsController etc.), so this exposes
 * refreshIfStale()/dispose() instead of register()/unregister().
 */
class WeatherController(private val context: Context) {

    private val locationController = LocationController(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _weather = MutableStateFlow<WeatherState?>(null)
    val weather: StateFlow<WeatherState?> = _weather.asStateFlow()

    private var inFlight: Job? = null

    fun refreshIfStale() {
        val cached = _weather.value
        if (cached != null && isWeatherCacheFresh(cached.fetchedAtMillis, System.currentTimeMillis())) {
            return
        }
        if (!hasLocationPermission()) return
        if (inFlight?.isActive == true) return

        inFlight = scope.launch {
            val result = withContext(Dispatchers.IO) { fetchWeather() }
            _weather.value = result
        }
    }

    fun dispose() {
        scope.cancel()
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    private suspend fun fetchWeather(): WeatherState? {
        val location = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
            locationController.getCurrentLocation()
        } ?: return null

        return try {
            val rawJson = requestOpenMeteo(location.latitude, location.longitude)
            parseWeatherResponse(rawJson, System.currentTimeMillis())
        } catch (e: Exception) {
            Log.w(TAG, "Weather fetch failed", e)
            null
        }
    }

    private fun requestOpenMeteo(latitude: Double, longitude: Double): String {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,relative_humidity_2m,precipitation_probability,weather_code",
        )
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = REQUEST_TIMEOUT_MILLIS
        connection.readTimeout = REQUEST_TIMEOUT_MILLIS
        try {
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseWeatherResponse(rawJson: String, fetchedAtMillis: Long): WeatherState? = try {
        val current = JSONObject(rawJson).getJSONObject("current")
        WeatherState(
            temperatureCelsius = current.getDouble("temperature_2m"),
            weatherCode = current.getInt("weather_code"),
            humidityPercent = current.getInt("relative_humidity_2m"),
            precipitationProbabilityPercent = current.optInt("precipitation_probability", 0),
            fetchedAtMillis = fetchedAtMillis,
        )
    } catch (e: Exception) {
        Log.w(TAG, "Weather parse failed", e)
        null
    }
}
