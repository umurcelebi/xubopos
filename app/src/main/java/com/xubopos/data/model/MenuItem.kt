package com.xubopos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_categories")
data class MenuCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val iconName: String,
    val sortOrder: Int = 0
)

@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoryId: Int,
    val name: String,
    val description: String = "",
    val price: Double,
    val isAvailable: Boolean = true,
    val hasDailyLimit: Boolean = false,
    val dailyLimit: Int = 0,
    val remainingStock: Int = 0
)
