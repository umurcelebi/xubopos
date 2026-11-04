package com.xubopos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_bills")
data class OrderBill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val tableId: Int,
    val tableName: String,
    val status: String = "OPEN",
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null
) {
    val remainingAmount: Double
        get() = (totalAmount - discountAmount - paidAmount).coerceAtLeast(0.0)

    val isFullyPaid: Boolean
        get() = totalAmount > 0.0 && remainingAmount <= 0.01
}
