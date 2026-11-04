package com.xubopos.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.xubopos.data.dao.RestaurantDao
import com.xubopos.data.model.KitchenTicketEntity
import com.xubopos.data.model.MenuCategory
import com.xubopos.data.model.MenuItem
import com.xubopos.data.model.OrderBill
import com.xubopos.data.model.OrderItem
import com.xubopos.data.model.PaymentRecord
import com.xubopos.data.model.RestaurantTable

@Database(
    entities = [
        RestaurantTable::class,
        MenuCategory::class,
        MenuItem::class,
        OrderBill::class,
        OrderItem::class,
        PaymentRecord::class,
        KitchenTicketEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "restaurant_pos_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
