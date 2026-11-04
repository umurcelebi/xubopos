package com.xubopos.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE menu_items ADD COLUMN hasDailyLimit INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE menu_items ADD COLUMN dailyLimit INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE menu_items ADD COLUMN remainingStock INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "restaurant_pos_db"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
