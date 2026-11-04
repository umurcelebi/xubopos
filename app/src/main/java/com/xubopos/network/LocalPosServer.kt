package com.xubopos.network

import android.util.Log
import com.google.gson.Gson
import com.xubopos.data.model.KitchenTicketEntity
import com.xubopos.data.model.OrderItem
import com.xubopos.data.model.RestaurantTable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class LocalPosServer(
    port: Int = 8887,
    private val onClientHandshake: (WebSocket) -> Unit,
    private val onAddOrderReceived: (AddOrderPayload) -> Unit,
    private val onUpdateTicketReceived: (UpdateTicketStatusPayload) -> Unit
) : WebSocketServer(InetSocketAddress(port)) {

    private val gson = Gson()
    private val _connectedClientCount = MutableStateFlow(0)
    val connectedClientCount: StateFlow<Int> = _connectedClientCount.asStateFlow()

    companion object {
        private const val TAG = "LocalPosServer"
    }

    override fun onStart() {
        Log.d(TAG, "LocalPosServer started on port $port")
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        val count = connections.size
        _connectedClientCount.value = count
        Log.d(TAG, "New client connected from ${conn?.remoteSocketAddress}. Total clients: $count")
        conn?.let { onClientHandshake(it) }
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        val count = connections.size
        _connectedClientCount.value = count
        Log.d(TAG, "Client disconnected: $reason. Remaining: $count")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        if (message.isNullOrBlank()) return
        try {
            val packet = gson.fromJson(message, NetworkPacket::class.java)
            when (packet.action) {
                "HANDSHAKE" -> {
                    conn?.let { onClientHandshake(it) }
                }
                "ADD_ORDER" -> {
                    val payload = gson.fromJson(packet.payload, AddOrderPayload::class.java)
                    onAddOrderReceived(payload)
                }
                "UPDATE_TICKET_STATUS" -> {
                    val payload = gson.fromJson(packet.payload, UpdateTicketStatusPayload::class.java)
                    onUpdateTicketReceived(payload)
                }
                "PING" -> {
                    val pong = NetworkPacket(action = "PONG")
                    conn?.send(gson.toJson(pong))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling client message", e)
        }
    }

    override fun onMessage(conn: WebSocket?, message: ByteBuffer?) {}

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Log.e(TAG, "Server socket error", ex)
    }

    fun sendPacket(conn: WebSocket, packet: NetworkPacket) {
        try {
            conn.send(gson.toJson(packet))
        } catch (e: Exception) {
            Log.e(TAG, "Error sending packet to client", e)
        }
    }

    fun broadcastPacket(packet: NetworkPacket) {
        try {
            val json = gson.toJson(packet)
            broadcast(json)
        } catch (e: Exception) {
            Log.e(TAG, "Broadcast error", e)
        }
    }

    fun broadcastTables(tables: List<RestaurantTable>) {
        val packet = NetworkPacket(
            action = "TABLES_UPDATE",
            payload = gson.toJson(tables)
        )
        broadcastPacket(packet)
    }

    fun broadcastTickets(tickets: List<KitchenTicketEntity>) {
        val packet = NetworkPacket(
            action = "KITCHEN_TICKETS_UPDATE",
            payload = gson.toJson(tickets)
        )
        broadcastPacket(packet)
    }

    fun broadcastActiveItems(items: List<OrderItem>) {
        val packet = NetworkPacket(
            action = "ITEMS_UPDATE",
            payload = gson.toJson(items)
        )
        broadcastPacket(packet)
    }

    fun broadcastFullSync(payload: FullSyncPayload) {
        val packet = NetworkPacket(
            action = "FULL_SYNC",
            payload = gson.toJson(payload)
        )
        broadcastPacket(packet)
    }
}
