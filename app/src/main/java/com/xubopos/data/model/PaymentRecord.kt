package com.xubopos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_records")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val billId: Long,
    val tableName: String,
    val amount: Double,
    val paymentMethod: String,
    val note: String = "",
    val paidAt: Long = System.currentTimeMillis()
)
