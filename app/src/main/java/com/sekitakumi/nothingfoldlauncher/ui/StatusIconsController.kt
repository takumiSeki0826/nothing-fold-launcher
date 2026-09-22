package com.sekitakumi.nothingfoldlauncher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StatusIconsController(private val context: Context) {

    private val _batteryPercent = MutableStateFlow(0)
    val batteryPercent: StateFlow<Int> = _batteryPercent.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _wifiConnected = MutableStateFlow(false)
    val wifiConnected: StateFlow<Boolean> = _wifiConnected.asStateFlow()

    private val _vpnConnected = MutableStateFlow(false)
    val vpnConnected: StateFlow<Boolean> = _vpnConnected.asStateFlow()

    private val _tailscaleConnected = MutableStateFlow(false)
    val tailscaleConnected: StateFlow<Boolean> = _tailscaleConnected.asStateFlow()

    private val _signalBars = MutableStateFlow<Int?>(null)
    val signalBars: StateFlow<Int?> = _signalBars.asStateFlow()

    private val _networkType = MutableStateFlow<String?>(null)
    val networkType: StateFlow<String?> = _networkType.asStateFlow()

    private val _cellularDbm = MutableStateFlow<Int?>(null)
    private val _wifiRssi = MutableStateFlow<Int?>(null)

    private val _signalDbm = MutableStateFlow<Int?>(null)
    val signalDbm: StateFlow<Int?> = _signalDbm.asStateFlow()

    private fun refreshSignalDbm() {
        _signalDbm.value = preferredSignalDbm(
            wifiConnected = _wifiConnected.value,
            wifiRssi = _wifiRssi.value,
            cellularDbm = _cellularDbm.value,
        )
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(receivedContext: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
            _batteryPercent.value = batteryPercent(level, scale)

            val status = intent?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
            _isCharging.value = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING
        }
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            _wifiConnected.value = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            _vpnConnected.value = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            refreshSignalDbm()
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            _tailscaleConnected.value = detectTailscaleInterface(linkProperties)
        }

        override fun onLost(network: Network) {
            _wifiConnected.value = false
            _vpnConnected.value = false
            _tailscaleConnected.value = false
            _wifiRssi.value = null
            refreshSignalDbm()
        }
    }

    private val wifiManager =
        context.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    @Suppress("DEPRECATION")
    private val wifiRssiReceiver = object : BroadcastReceiver() {
        override fun onReceive(receivedContext: Context?, intent: Intent?) {
            _wifiRssi.value = wifiManager?.connectionInfo?.rssi
            refreshSignalDbm()
        }
    }

    // Tailscale isn't distinguishable via NetworkCapabilities or interface name (both are generic
    // "tun0"), so this looks for its CGNAT-range address instead; any failure here just falls
    // back to the generic VPN badge via vpnBadgeFor.
    private fun detectTailscaleInterface(linkProperties: LinkProperties): Boolean = try {
        linkProperties.linkAddresses.any { isTailscaleCgnatAddress(it.address.address) }
    } catch (e: Exception) {
        Log.w(TAG, "Failed to inspect link properties for Tailscale detection", e)
        false
    }

    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    private val telephonyCallback: TelephonyCallback? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            object :
                TelephonyCallback(),
                TelephonyCallback.SignalStrengthsListener,
                TelephonyCallback.DisplayInfoListener {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    _signalBars.value = signalBars(signalStrength.level)
                    _cellularDbm.value = signalStrength.cellSignalStrengths.firstOrNull()?.dbm
                    refreshSignalDbm()
                }

                override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                    _networkType.value = networkTypeLabel(
                        networkType = telephonyDisplayInfo.networkType,
                        overrideNetworkType = telephonyDisplayInfo.overrideNetworkType,
                    )
                }
            }
        } else {
            null
        }

    @Suppress("DEPRECATION")
    private val phoneStateListener: PhoneStateListener? =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            object : PhoneStateListener() {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    _signalBars.value = signalBars(signalStrength.level)
                    _cellularDbm.value = signalStrength.cellSignalStrengths.firstOrNull()?.dbm
                    refreshSignalDbm()
                }
            }
        } else {
            null
        }

    fun register() {
        ContextCompat.registerReceiver(
            context,
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        connectivityManager?.registerDefaultNetworkCallback(networkCallback)
        registerSignalStrengthListener()
        ContextCompat.registerReceiver(
            context,
            wifiRssiReceiver,
            IntentFilter(WifiManager.RSSI_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    fun unregister() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: IllegalArgumentException) {
            // No-op if it was never registered
        }
        connectivityManager?.unregisterNetworkCallback(networkCallback)
        unregisterSignalStrengthListener()
        try {
            context.unregisterReceiver(wifiRssiReceiver)
        } catch (e: IllegalArgumentException) {
            // No-op if it was never registered
        }
    }

    private fun registerSignalStrengthListener() {
        val manager = telephonyManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let {
                try {
                    manager.registerTelephonyCallback(context.mainExecutor, it)
                } catch (e: SecurityException) {
                    _signalBars.value = null
                }
            }
        } else {
            @Suppress("DEPRECATION")
            try {
                manager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
            } catch (e: SecurityException) {
                _signalBars.value = null
            }
        }
    }

    private fun unregisterSignalStrengthListener() {
        val manager = telephonyManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let { manager.unregisterTelephonyCallback(it) }
        } else {
            @Suppress("DEPRECATION")
            manager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
    }

    private companion object {
        const val TAG = "StatusIconsController"
    }
}
