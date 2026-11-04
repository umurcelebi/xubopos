package com.xubopos.network

import com.xubopos.data.model.KitchenTicketEntity
import com.xubopos.data.model.KitchenTicketItemDto
import com.xubopos.data.model.MenuCategory
import com.xubopos.data.model.MenuItem
import com.xubopos.data.model.OrderItem
import com.xubopos.data.model.RestaurantTable

enum class DeviceRole(val title: String, val badge: String, val description: String) {
    SERVER(
        title = "Ana Kasa & Sunucu",
        badge = "KASA",
        description = "Ana veritabanını tutar, yerel ağda yayın yapar ve tüm kasa işlemlerini yönetir."
    ),
    WAITER(
        title = "Garson Terminali",
        badge = "GARSON",
        description = "Ana kasaya bağlanarak masaları görür, sipariş alır ve mutfağa iletir."
    ),
    KITCHEN(
        title = "Mutfak Takip Ekranı (KDS)",
        badge = "MUTFAK",
        description = "Gelen sipariş fişlerini aşçı ekranında gösterir, hazırlık durumunu yönetir."
    )
}

enum class ConnectionStatus {
    DISCONNECTED,
    SEARCHING,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class NetworkPacket(
    val action: String,
    val role: String = "",
    val senderName: String = "",
    val payload: String = ""
)

data class FullSyncPayload(
    val restaurantName: String,
    val tables: List<RestaurantTable>,
    val categories: List<MenuCategory>,
    val menuItems: List<MenuItem>,
    val activeTickets: List<KitchenTicketEntity>,
    val activeOrderItems: List<OrderItem> = emptyList(),
    val restaurantSubtitle: String = ""
)

data class AddOrderPayload(
    val tableId: Int,
    val tableName: String,
    val waiterName: String = "Garson",
    val items: List<KitchenTicketItemDto>
)

data class UpdateTicketStatusPayload(
    val ticketId: String,
    val status: String // PENDING, PREPARING, READY, COMPLETED
)
