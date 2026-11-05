package com.xubopos.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xubopos.data.model.KitchenTicketEntity
import com.xubopos.data.model.OrderItem
import com.xubopos.data.model.RestaurantTable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class LocalPosClient(
    serverUri: URI,
    private val role: DeviceRole,
    private val deviceName: String,
    private val deviceId: String = "",
    private val onFullSyncReceived: (FullSyncPayload) -> Unit,
    private val onTablesUpdateReceived: (List<RestaurantTable>) -> Unit,
    private val onKitchenTicketsUpdateReceived: (List<KitchenTicketEntity>) -> Unit,
    private val onActiveItemsUpdateReceived: (List<OrderItem>) -> Unit = {},
    private val onTableLocksUpdateReceived: (List<TableLockEntry>) -> Unit = {},
    private val onKitchenStatusUpdateReceived: (Boolean) -> Unit = {}
) : WebSocketClient(serverUri) {

    private val gson = Gson()
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.CONNECTING)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    companion object {
        private const val TAG = "LocalPosClient"
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        _connectionStatus.value = ConnectionStatus.CONNECTED
        Log.d(TAG, "Connected to server at $uri as $role")

        val handshakePacket = NetworkPacket(
            action = "HANDSHAKE",
            role = role.name,
            senderName = deviceName,
            payload = deviceId
        )
        send(gson.toJson(handshakePacket))
    }

    override fun onMessage(message: String?) {
        if (message.isNullOrBlank()) return
        try {
            val packet = gson.fromJson(message, NetworkPacket::class.java)
            when (packet.action) {
                "FULL_SYNC" -> {
                    val payload = gson.fromJson(packet.payload, FullSyncPayload::class.java)
                    onFullSyncReceived(payload)
                }
                "TABLES_UPDATE" -> {
                    val type = object : TypeToken<List<RestaurantTable>>() {}.type
                    val tables = gson.fromJson<List<RestaurantTable>>(packet.payload, type) ?: emptyList()
                    onTablesUpdateReceived(tables)
                }
                "KITCHEN_TICKETS_UPDATE" -> {
                    val type = object : TypeToken<List<KitchenTicketEntity>>() {}.type
                    val tickets = gson.fromJson<List<KitchenTicketEntity>>(packet.payload, type) ?: emptyList()
                    onKitchenTicketsUpdateReceived(tickets)
                }
                "ITEMS_UPDATE" -> {
                    val type = object : TypeToken<List<OrderItem>>() {}.type
                    val items = gson.fromJson<List<OrderItem>>(packet.payload, type) ?: emptyList()
                    onActiveItemsUpdateReceived(items)
                }
                "TABLE_LOCKS_UPDATE" -> {
                    val type = object : TypeToken<List<TableLockEntry>>() {}.type
                    val locks = gson.fromJson<List<TableLockEntry>>(packet.payload, type) ?: emptyList()
                    onTableLocksUpdateReceived(locks)
                }
                "KITCHEN_STATUS" -> {
                    val isKitchenActive = packet.payload.toBoolean()
                    onKitchenStatusUpdateReceived(isKitchenActive)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing server message", e)
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        Log.d(TAG, "Disconnected from server: $reason (code: $code, remote: $remote)")
    }

    override fun onError(ex: Exception?) {
        _connectionStatus.value = ConnectionStatus.ERROR
        Log.e(TAG, "Client socket error", ex)
    }

    fun sendAddOrder(payload: AddOrderPayload) {
        if (!isOpen) return
        val packet = NetworkPacket(
            action = "ADD_ORDER",
            role = role.name,
            senderName = deviceName,
            payload = gson.toJson(payload)
        )
        send(gson.toJson(packet))
    }

    fun sendUpdateTicketStatus(ticketId: String, status: String) {
        if (!isOpen) return
        val payload = UpdateTicketStatusPayload(ticketId = ticketId, status = status)
        val packet = NetworkPacket(
            action = "UPDATE_TICKET_STATUS",
            role = role.name,
            senderName = deviceName,
            payload = gson.toJson(payload)
        )
        send(gson.toJson(packet))
    }

    fun sendLockTable(tableId: Int, deviceId: String, deviceName: String) {
        if (!isOpen) return
        val payload = TableLockPayload(tableId, deviceId, deviceName)
        val packet = NetworkPacket(
            action = "LOCK_TABLE",
            role = role.name,
            senderName = deviceName,
            payload = gson.toJson(payload)
        )
        send(gson.toJson(packet))
    }

    fun sendUnlockTable(tableId: Int, deviceId: String, deviceName: String) {
        if (!isOpen) return
        val payload = TableLockPayload(tableId, deviceId, deviceName)
        val packet = NetworkPacket(
            action = "UNLOCK_TABLE",
            role = role.name,
            senderName = deviceName,
            payload = gson.toJson(payload)
        )
        send(gson.toJson(packet))
    }
}
