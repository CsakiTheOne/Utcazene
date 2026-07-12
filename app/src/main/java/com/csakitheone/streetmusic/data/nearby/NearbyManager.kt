package com.csakitheone.streetmusic.data.nearby

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class NearbyManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    internal val connectionsClient get() = Nearby.getConnectionsClient(context)
    internal val localId = System.currentTimeMillis().toString(36).takeLast(4)

    val friends = FriendsFeature(this, scope)
    val dataSync = DataSyncFeature(this, scope)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var activeAdvertiser: Any? = null
    private var activeDiscoverer: Any? = null
    private var activeStrategy: Strategy? = null
    private var advertisingRetryCount = 0
    private var discoveryRetryCount = 0

    var useHighPowerDiscovery = false

    fun clearError() {
        _error.value = null
    }

    fun setNearbyFriendsActive(active: Boolean) {
        if (!active) {
            dataSync.stop()
        }
        friends.setActive(active)
        if (active && !hasPermissions()) {
            _error.value = "Permissions required for Nearby features"
        }
    }

    fun updateLocalFavorites(favorites: Set<String>) {
        friends.updateLocalFavorites(favorites)
    }

    fun updateLocalScreen(screen: String) {
        friends.updateLocalScreen(screen)
    }

    fun updateLocalNickname(nickname: String) {
        friends.updateLocalNickname(nickname)
    }

    internal fun packName(name: String) = "${name.take(20)}|$localId"
    internal fun unpackName(packed: String): Pair<String, String> {
        val index = packed.lastIndexOf('|')
        return if (index != -1) {
            packed.substring(0, index) to packed.substring(index + 1)
        } else {
            packed to ""
        }
    }

    fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter?.isEnabled == true
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    internal fun requestAdvertising(
        requester: Any,
        endpointName: String,
        serviceId: String,
        strategy: Strategy,
        callback: ConnectionLifecycleCallback,
        onSuccess: () -> Unit = {}
    ) {
        if (activeStrategy != null && activeStrategy != strategy) {
            scope.launch {
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                connectionsClient.stopAllEndpoints()
                activeStrategy = null
                activeAdvertiser = null
                activeDiscoverer = null
                delay(1.seconds)
                requestAdvertising(requester, endpointName, serviceId, strategy, callback, onSuccess)
            }
            return
        } else if (activeAdvertiser != null && activeAdvertiser != requester) {
            connectionsClient.stopAdvertising()
        }
        activeAdvertiser = requester
        activeStrategy = strategy

        val options = AdvertisingOptions.Builder()
            .setStrategy(strategy)
            .setLowPower(!useHighPowerDiscovery)
            .build()
        connectionsClient.startAdvertising(endpointName, serviceId, callback, options)
            .addOnSuccessListener {
                Log.i("NearbyManager", "Advertising started for ${requester::class.simpleName}")
                advertisingRetryCount = 0
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("NearbyManager", "Advertising failed for ${requester::class.simpleName}", e)
                handleAdvertisingFailure(e, requester, endpointName, serviceId, strategy, callback, onSuccess)
            }
    }

    private fun handleAdvertisingFailure(
        e: Exception,
        requester: Any,
        endpointName: String,
        serviceId: String,
        strategy: Strategy,
        callback: ConnectionLifecycleCallback,
        onSuccess: () -> Unit
    ) {
        val statusCode = (e as? ApiException)?.statusCode
        if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_HAVE_ACTIVE_STRATEGY ||
            statusCode == ConnectionsStatusCodes.STATUS_OUT_OF_ORDER_API_CALL ||
            statusCode == 8
        ) {
            if (advertisingRetryCount < 3) {
                advertisingRetryCount++
                scope.launch {
                    delay(2.seconds)
                    if (activeAdvertiser == requester) {
                        connectionsClient.stopAdvertising()
                        connectionsClient.stopAllEndpoints()
                        delay(500.milliseconds)
                        requestAdvertising(requester, endpointName, serviceId, strategy, callback, onSuccess)
                    }
                }
                return
            }
        }
        _error.value = "Advertising failed: ${e.message}"
    }

    internal fun releaseAdvertising(requester: Any) {
        if (activeAdvertiser == requester) {
            connectionsClient.stopAdvertising()
            activeAdvertiser = null
            if (activeDiscoverer == null) activeStrategy = null
        }
    }

    internal fun requestDiscovery(
        requester: Any,
        serviceId: String,
        strategy: Strategy,
        callback: EndpointDiscoveryCallback,
        onSuccess: () -> Unit = {}
    ) {
        if (activeStrategy != null && activeStrategy != strategy) {
            scope.launch {
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                connectionsClient.stopAllEndpoints()
                activeStrategy = null
                activeAdvertiser = null
                activeDiscoverer = null
                delay(1.seconds)
                requestDiscovery(requester, serviceId, strategy, callback, onSuccess)
            }
            return
        } else if (activeDiscoverer != null && activeDiscoverer != requester) {
            connectionsClient.stopDiscovery()
        }
        activeDiscoverer = requester
        activeStrategy = strategy

        val options = DiscoveryOptions.Builder()
            .setStrategy(strategy)
            .setLowPower(!useHighPowerDiscovery)
            .build()
        connectionsClient.startDiscovery(serviceId, callback, options)
            .addOnSuccessListener {
                Log.i("NearbyManager", "Discovery started for ${requester::class.simpleName}")
                discoveryRetryCount = 0
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("NearbyManager", "Discovery failed for ${requester::class.simpleName}", e)
                handleDiscoveryFailure(e, requester, serviceId, strategy, callback, onSuccess)
            }
    }

    private fun handleDiscoveryFailure(
        e: Exception,
        requester: Any,
        serviceId: String,
        strategy: Strategy,
        callback: EndpointDiscoveryCallback,
        onSuccess: () -> Unit
    ) {
        val statusCode = (e as? ApiException)?.statusCode
        if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_HAVE_ACTIVE_STRATEGY ||
            statusCode == ConnectionsStatusCodes.STATUS_ALREADY_DISCOVERING ||
            statusCode == ConnectionsStatusCodes.STATUS_OUT_OF_ORDER_API_CALL ||
            statusCode == 8
        ) {
            if (discoveryRetryCount < 3) {
                discoveryRetryCount++
                scope.launch {
                    delay(2.seconds)
                    if (activeDiscoverer == requester) {
                        connectionsClient.stopDiscovery()
                        connectionsClient.stopAllEndpoints()
                        delay(500.milliseconds)
                        requestDiscovery(requester, serviceId, strategy, callback, onSuccess)
                    }
                }
                return
            }
        }
        _error.value = "Discovery failed: ${e.message}"
    }

    internal fun releaseDiscovery(requester: Any) {
        if (activeDiscoverer == requester) {
            connectionsClient.stopDiscovery()
            activeDiscoverer = null
            if (activeAdvertiser == null) activeStrategy = null
        }
    }

    companion object {
        val REQUIRED_PERMISSIONS = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            }
            
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                add(Manifest.permission.ACCESS_LOCAL_NETWORK)
            }
        }
    }
}
