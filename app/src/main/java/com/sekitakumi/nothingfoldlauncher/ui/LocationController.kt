package com.sekitakumi.nothingfoldlauncher.ui

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Callers must check for ACCESS_COARSE_LOCATION themselves before calling
 * [getCurrentLocation] - this class only guards against the OS throwing
 * SecurityException if that check was skipped or the permission was revoked
 * mid-call.
 */
class LocationController(private val context: Context) {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    suspend fun getCurrentLocation(): Location? {
        val manager = locationManager ?: return null
        lastKnownLocation(manager)?.let { return it }
        return requestSingleUpdate(manager)
    }

    private fun lastKnownLocation(manager: LocationManager): Location? = try {
        manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    } catch (e: SecurityException) {
        null
    }

    private suspend fun requestSingleUpdate(manager: LocationManager): Location? =
        suspendCancellableCoroutine { continuation ->
            val provider = when {
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                else -> null
            }
            if (provider == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (continuation.isActive) continuation.resume(location)
                }
            }

            try {
                manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            } catch (e: SecurityException) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            continuation.invokeOnCancellation { manager.removeUpdates(listener) }
        }
}
