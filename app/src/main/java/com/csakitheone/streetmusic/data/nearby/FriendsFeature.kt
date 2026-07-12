package com.csakitheone.streetmusic.data.nearby

import android.util.Log
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class FriendsPayload(
    val nickname: String,
    val screen: String,
    val favoriteSlugs: Set<String>,
)

class FriendsFeature(
    private val nearbyManager: NearbyManager,
    private val scope: CoroutineScope
) {
    private val serviceId = "com.csakitheone.streetmusic.NEARBY_GANG"
    private val strategy = Strategy.P2P_CLUSTER

    private val _connectedFriends = MutableStateFlow<Map<String, FriendsPayload>>(emptyMap())
    val connectedFriends: StateFlow<Map<String, FriendsPayload>> = _connectedFriends.asStateFlow()

    val nearbyFavorites: StateFlow<Map<String, Set<String>>> = _connectedFriends
        .map { it.mapValues { entry -> entry.value.favoriteSlugs } }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    private var isActive = false
    private var localNickname = "Friend"
    private var localScreen = "Home"
    private var localFavorites = emptySet<String>()

    private val connectingEndpoints = mutableSetOf<String>()
    private val pendingNames = mutableMapOf<String, String>()

    fun setActive(active: Boolean) {
        if (isActive == active) return
        isActive = active
        if (active) {
            start()
        } else {
            stop()
        }
    }

    fun updateLocalNickname(nickname: String) {
        val changed = localNickname != nickname
        localNickname = nickname
        if (isActive && changed) {
            restart()
        }
    }

    fun updateLocalScreen(screen: String) {
        val changed = localScreen != screen
        localScreen = screen
        if (isActive && changed) {
            broadcastFavorites()
        }
    }

    fun updateLocalFavorites(favorites: Set<String>) {
        val changed = localFavorites != favorites
        localFavorites = favorites
        if (isActive && changed) {
            broadcastFavorites()
        }
    }

    private fun start() {
        nearbyManager.requestAdvertising(
            this,
            nearbyManager.packName(localNickname),
            serviceId,
            strategy,
            connectionLifecycleCallback
        ) {
            nearbyManager.requestDiscovery(
                this,
                serviceId,
                strategy,
                endpointDiscoveryCallback
            )
            broadcastFavorites()
        }
    }

    private fun stop() {
        nearbyManager.releaseAdvertising(this)
        nearbyManager.releaseDiscovery(this)
        _connectedFriends.value = emptyMap()
        synchronized(connectingEndpoints) {
            connectingEndpoints.clear()
            pendingNames.clear()
        }
    }

    private fun restart() {
        stop()
        start()
    }

    private fun broadcastFavorites() {
        val payloadData =
            Json.encodeToString(FriendsPayload(localNickname, localScreen, localFavorites))
        val payload = Payload.fromBytes(payloadData.toByteArray())

        _connectedFriends.value.keys.forEach { endpointId ->
            nearbyManager.connectionsClient.sendPayload(endpointId, payload)
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val (peerName, peerId) = nearbyManager.unpackName(info.endpointName)
            if (peerId == nearbyManager.localId) return

            // Deterministic Initiator: Only the device with the smaller ID initiates the connection
            if (nearbyManager.localId >= peerId) {
                Log.d("FriendsFeature", "Found $peerName ($peerId), waiting for them to initiate.")
                return
            }

            Log.d("FriendsFeature", "Friend found: $endpointId ($peerName). Initiating...")

            synchronized(connectingEndpoints) {
                if (_connectedFriends.value.containsKey(endpointId) || connectingEndpoints.contains(
                        endpointId
                    )
                ) {
                    return
                }
                connectingEndpoints.add(endpointId)
                pendingNames[endpointId] = peerName
            }

            scope.launch {
                if (!isActive || _connectedFriends.value.containsKey(endpointId)) {
                    synchronized(connectingEndpoints) { connectingEndpoints.remove(endpointId) }
                    return@launch
                }
                nearbyManager.connectionsClient.requestConnection(
                    nearbyManager.packName(localNickname),
                    endpointId,
                    connectionLifecycleCallback
                ).addOnFailureListener {
                    synchronized(connectingEndpoints) { connectingEndpoints.remove(endpointId) }
                }
            }
        }

        override fun onEndpointLost(endpointId: String) {
            synchronized(connectingEndpoints) {
                connectingEndpoints.remove(endpointId)
                pendingNames.remove(endpointId)
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            val (_, peerId) = nearbyManager.unpackName(info.endpointName)
            if (peerId == nearbyManager.localId) {
                nearbyManager.connectionsClient.rejectConnection(endpointId)
                return
            }
            nearbyManager.connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            val peerName = synchronized(connectingEndpoints) {
                connectingEndpoints.remove(endpointId)
                pendingNames.remove(endpointId) ?: "Friend"
            }

            if (result.status.isSuccess) {
                _connectedFriends.value += (endpointId to FriendsPayload(
                    peerName,
                    "Home",
                    emptySet()
                ))
                broadcastFavorites()
            }
        }

        override fun onDisconnected(endpointId: String) {
            _connectedFriends.value -= endpointId
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val data = payload.asBytes()?.let { String(it) } ?: return
                try {
                    val friendsPayload = Json.decodeFromString<FriendsPayload>(data)
                    _connectedFriends.value += (endpointId to friendsPayload)
                } catch (e: Exception) {
                    Log.e("FriendsFeature", "Error decoding payload", e)
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }
}
