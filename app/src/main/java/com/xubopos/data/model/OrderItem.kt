package com.xubopos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val billId: Long,
    val menuItemId: Int,
    val itemName: String,
    val categoryName: String,
    val unitPrice: Double,
    val quantity: Int = 1,
    val paidQuantity: Int = 0,
    val note: String = "",
    val addedAt: Long = System.currentTimeMillis()
) {
    val subtotal: Double
        get() = unitPrice * quantity

    val unpaidQuantity: Int
        get() = (quantity - paidQuantity).coerceAtLeast(0)
}
