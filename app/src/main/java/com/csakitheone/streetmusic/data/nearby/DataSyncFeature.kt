package com.csakitheone.streetmusic.data.nearby

import android.util.Log
import com.csakitheone.streetmusic.data.local.ArtistEntity
import com.csakitheone.streetmusic.data.local.EventEntity
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DataSyncPayload(
    val artists: List<ArtistEntity>,
    val events: List<EventEntity>
)

@Serializable
data class DataSyncHandshake(
    val nickname: String
)

class DataSyncFeature(
    private val nearbyManager: NearbyManager,
    private val scope: CoroutineScope
) {
    private val connectionsClient = nearbyManager.connectionsClient
    private val serviceId = "com.csakitheone.streetmusic.nearby_data_sync"
    private val strategy = Strategy.P2P_STAR

    private val _discoveredEndpoints = MutableStateFlow<Map<String, String>>(emptyMap())
    val discoveredEndpoints: StateFlow<Map<String, String>> = _discoveredEndpoints.asStateFlow()

    private val _connectedEndpoints = MutableStateFlow<Map<String, DataSyncHandshake>>(emptyMap())
    val connectedEndpoints: StateFlow<Map<String, DataSyncHandshake>> = _connectedEndpoints.asStateFlow()

    private val _incomingData = MutableStateFlow<DataSyncPayload?>(null)
    val incomingData: StateFlow<DataSyncPayload?> = _incomingData.asStateFlow()

    private var localNickname = "Friend"
    private var isAdvertising = false
    private var isDiscovering = false

    fun startAdvertising(nickname: String) {
        localNickname = nickname
        isAdvertising = true
        nearbyManager.requestAdvertising(
            this,
            nearbyManager.packName(localNickname),
            serviceId,
            strategy,
            connectionLifecycleCallback
        )
    }

    fun startDiscovery() {
        isDiscovering = true
        _discoveredEndpoints.value = emptyMap()
        nearbyManager.requestDiscovery(
            this,
            serviceId,
            strategy,
            endpointDiscoveryCallback
        )
    }

    fun stop() {
        isAdvertising = false
        isDiscovering = false
        nearbyManager.releaseAdvertising(this)
        nearbyManager.releaseDiscovery(this)
        _connectedEndpoints.value = emptyMap()
        _discoveredEndpoints.value = emptyMap()
    }

    fun connectToEndpoint(endpointId: String) {
        connectionsClient.requestConnection(
            nearbyManager.packName(localNickname),
            endpointId,
            connectionLifecycleCallback
        )
    }

    fun sendData(endpointId: String, artists: List<ArtistEntity>, events: List<EventEntity>) {
        val payloadData = Json.encodeToString(DataSyncPayload(artists, events))
        val payload = Payload.fromBytes(payloadData.toByteArray())
        connectionsClient.sendPayload(endpointId, payload)
    }

    fun clearIncomingData() {
        _incomingData.value = null
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val (peerName, peerId) = nearbyManager.unpackName(info.endpointName)
            if (peerId == nearbyManager.localId) return
            _discoveredEndpoints.value += (endpointId to peerName)
        }

        override fun onEndpointLost(endpointId: String) {
            _discoveredEndpoints.value -= endpointId
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            val (_, peerId) = nearbyManager.unpackName(info.endpointName)
            if (peerId == nearbyManager.localId) {
                connectionsClient.rejectConnection(endpointId)
                return
            }
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                val peerName = _discoveredEndpoints.value[endpointId] ?: "Friend"
                _connectedEndpoints.value += (endpointId to DataSyncHandshake(peerName))
            }
        }

        override fun onDisconnected(endpointId: String) {
            _connectedEndpoints.value -= endpointId
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val data = payload.asBytes()?.let { String(it) } ?: return
                try {
                    val syncPayload = Json.decodeFromString<DataSyncPayload>(data)
                    _incomingData.value = syncPayload
                } catch (e: Exception) {
                    Log.e("DataSyncFeature", "Error decoding payload", e)
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }
}
