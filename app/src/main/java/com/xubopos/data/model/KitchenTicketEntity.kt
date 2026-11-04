package com.xubopos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "kitchen_tickets")
data class KitchenTicketEntity(
    @PrimaryKey
    val ticketId: String,
    val tableId: Int,
    val tableName: String,
    val waiterName: String = "Garson",
    val createdAt: Long = System.currentTimeMillis(),
    val itemsJson: String,
    val status: String = "PENDING" // PENDING, PREPARING, READY, COMPLETED
) {
    fun getItems(): List<KitchenTicketItemDto> {
        return try {
            val type = object : TypeToken<List<KitchenTicketItemDto>>() {}.type
            Gson().fromJson(itemsJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

data class KitchenTicketItemDto(
    val id: Long = 0L,
    val itemName: String,
    val categoryName: String = "",
    val quantity: Int = 1,
    val note: String = "",
    val status: String = "PENDING"
)
