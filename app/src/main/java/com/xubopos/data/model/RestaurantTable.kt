package com.xubopos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurant_tables")
data class RestaurantTable(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val zone: String,
    val capacity: Int = 4,
    val isOccupied: Boolean = false,
    val currentBillId: Long? = null,
    val occupiedSince: Long? = null,
    val currentTotalAmount: Double = 0.0,
    val currentPaidAmount: Double = 0.0
) {
    val isPartiallyPaid: Boolean
        get() = isOccupied && currentPaidAmount > 0.0 && currentPaidAmount < currentTotalAmount

    val remainingAmount: Double
        get() = (currentTotalAmount - currentPaidAmount).coerceAtLeast(0.0)
}
