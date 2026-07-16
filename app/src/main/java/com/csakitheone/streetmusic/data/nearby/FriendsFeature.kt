package com.csakitheone.streetmusic.data.nearby

import android.util.Log
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ThreadNode(
    val id: String,
    val parentId: String,
    val senderName: String,
    val content: String,
) {
    companion object {

        val MAIN = ThreadNode(
            id = "main",
            parentId = "",
            senderName = "UZ App",
            content = "Welcome to the main thread!",
        )

    }
}

@Serializable
data class FriendsPayload(
    val nickname: String,
    val screen: String,
    val favoriteSlugs: Set<String>,
    val threadNodes: Set<ThreadNode> = emptySet(),
    val peerId: String = "",
)

class FriendsFeature(
    private val nearbyManager: NearbyManager,
    private val scope: CoroutineScope
) {
    private val serviceId = "com.csakitheone.streetmusic.NEARBY_GANG"
    private val strategy = Strategy.P2P_CLUSTER

    private val _connectedFriends = MutableStateFlow<Map<String, FriendsPayload>>(emptyMap())
    val connectedFriends: StateFlow<Map<String, FriendsPayload>> = _connectedFriends.asStateFlow()

    private val _localThreadNodes = MutableStateFlow<Set<ThreadNode>>(setOf(ThreadNode.MAIN))
    val localThreadNodes: StateFlow<Set<ThreadNode>> = _localThreadNodes.asStateFlow()

    val allThreadNodes: StateFlow<Set<ThreadNode>> = combine(
        _localThreadNodes,
        _connectedFriends
    ) { local, connected ->
        val all = local.toMutableSet()
        connected.values.forEach { all.addAll(it.threadNodes) }
        all
    }.stateIn(scope, SharingStarted.Eagerly, setOf(ThreadNode.MAIN))

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

    fun sendMessage(parentId: String, content: String) {
        val newNode = ThreadNode(
            id = System.currentTimeMillis().toString(36),
            parentId = parentId,
            senderName = localNickname,
            content = content.take(280),
        )
        _localThreadNodes.value += newNode
        if (isActive) {
            broadcastFavorites()
        }
    }

    fun clearMessages() {
        _localThreadNodes.value = setOf(ThreadNode.MAIN)
        if (isActive) {
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
        isActive = false
        nearbyManager.connectionsClient.stopAllEndpoints()
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
        val payloadData = Json.encodeToString(
            FriendsPayload(
                localNickname,
                localScreen,
                localFavorites,
                _localThreadNodes.value,
                nearbyManager.localId
            )
        )
        val payload = Payload.fromBytes(payloadData.toByteArray())

        _connectedFriends.value.keys.forEach { endpointId ->
            nearbyManager.connectionsClient.sendPayload(endpointId, payload)
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val (peerName, peerId) = nearbyManager.unpackName(info.endpointName)
            if (peerId == nearbyManager.localId) return

            // Avoid connecting to the same device multiple times
            if (_connectedFriends.value.values.any { it.peerId == peerId }) {
                Log.d("FriendsFeature", "Already connected to peer $peerId, ignoring endpoint $endpointId")
                return
            }

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
                    Log.d("FriendsFeature", "Already connecting or connected to $endpointId")
                    return
                }
                connectingEndpoints.add(endpointId)
                pendingNames[endpointId] = peerName
            }

            scope.launch {
                kotlinx.coroutines.delay(500)
                if (!isActive || _connectedFriends.value.containsKey(endpointId)) {
                    synchronized(connectingEndpoints) {
                        connectingEndpoints.remove(endpointId)
                        pendingNames.remove(endpointId)
                    }
                    return@launch
                }
                nearbyManager.connectionsClient.requestConnection(
                    nearbyManager.packName(localNickname),
                    endpointId,
                    connectionLifecycleCallback
                ).addOnSuccessListener {
                    Log.d("FriendsFeature", "Connection request sent to $peerName ($endpointId)")
                }.addOnFailureListener { e ->
                    val statusCode = (e as? com.google.android.gms.common.api.ApiException)?.statusCode
                    if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT) {
                        Log.w("FriendsFeature", "Already connected to $peerName ($endpointId). Waiting for callbacks...")
                        // Don't remove from connectingEndpoints, let onConnectionResult handle it
                        return@addOnFailureListener
                    }
                    
                    Log.e("FriendsFeature", "Connection request failed to $peerName ($endpointId): ${e.message}")
                    synchronized(connectingEndpoints) {
                        connectingEndpoints.remove(endpointId)
                        pendingNames.remove(endpointId)
                    }
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
            val (peerName, peerId) = nearbyManager.unpackName(info.endpointName)
            Log.d("FriendsFeature", "Connection initiated with $peerName ($peerId, $endpointId). Incoming: ${info.isIncomingConnection}")

            if (peerId == nearbyManager.localId) {
                Log.w("FriendsFeature", "Rejecting connection from self.")
                nearbyManager.connectionsClient.rejectConnection(endpointId)
                return
            }

            if (_connectedFriends.value.values.any { it.peerId == peerId }) {
                Log.d("FriendsFeature", "Already connected to peer $peerId, allowing new connection $endpointId")
                // We allow it, but we might want to clean up the old one later if it doesn't disconnect
            }

            Log.d("FriendsFeature", "Accepting connection from $peerName ($endpointId)")
            nearbyManager.connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnFailureListener { e ->
                    Log.e("FriendsFeature", "Failed to accept connection from $peerName ($endpointId)", e)
                }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            val peerName = synchronized(connectingEndpoints) {
                connectingEndpoints.remove(endpointId)
                pendingNames.remove(endpointId) ?: "Friend"
            }

            if (result.status.isSuccess) {
                Log.i("FriendsFeature", "Successfully connected to $peerName ($endpointId)")
                // We don't have peerId here easily unless we store it in another map, 
                // but it will be updated soon by the payload.
                _connectedFriends.value += (endpointId to FriendsPayload(
                    peerName,
                    "Home",
                    emptySet()
                ))
                broadcastFavorites()
            } else {
                Log.e("FriendsFeature", "Connection failed to $peerName ($endpointId): ${result.status.statusMessage} (${result.status.statusCode})")
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i("FriendsFeature", "Disconnected from $endpointId")
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
                    // Merge incoming thread nodes into local set
                    _localThreadNodes.value += friendsPayload.threadNodes
                } catch (e: Exception) {
                    Log.e("FriendsFeature", "Error decoding payload", e)
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }
}
