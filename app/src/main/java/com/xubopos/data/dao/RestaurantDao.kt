package com.xubopos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.xubopos.data.model.KitchenTicketEntity
import com.xubopos.data.model.MenuCategory
import com.xubopos.data.model.MenuItem
import com.xubopos.data.model.OrderBill
import com.xubopos.data.model.OrderItem
import com.xubopos.data.model.PaymentMethodBreakdown
import com.xubopos.data.model.PaymentRecord
import com.xubopos.data.model.ProductSalesBreakdown
import com.xubopos.data.model.RestaurantTable
import com.xubopos.data.model.TopSellingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurant_tables ORDER BY id ASC")
    fun getAllTables(): Flow<List<RestaurantTable>>

    @Query("SELECT * FROM restaurant_tables WHERE id = :tableId")
    suspend fun getTableById(tableId: Int): RestaurantTable?

    @Query("SELECT COUNT(*) FROM restaurant_tables")
    suspend fun getTableCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTables(tables: List<RestaurantTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: RestaurantTable): Long

    @Update
    suspend fun updateTable(table: RestaurantTable)

    @Delete
    suspend fun deleteTable(table: RestaurantTable)

    @Query("SELECT * FROM menu_categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<MenuCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<MenuCategory>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: MenuCategory): Long

    @Update
    suspend fun updateCategory(category: MenuCategory)

    @Delete
    suspend fun deleteCategory(category: MenuCategory)

    @Query("SELECT * FROM menu_items ORDER BY categoryId ASC, name ASC")
    fun getAllMenuItems(): Flow<List<MenuItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItem): Long

    @Update
    suspend fun updateMenuItem(item: MenuItem)

    @Query("SELECT * FROM menu_items WHERE id = :id")
    suspend fun getMenuItemById(id: Int): MenuItem?

    @Query("UPDATE menu_items SET remainingStock = dailyLimit WHERE hasDailyLimit = 1")
    suspend fun resetAllDailyStocks()

    @Query("UPDATE menu_items SET remainingStock = :newStock WHERE id = :id")
    suspend fun updateRemainingStock(id: Int, newStock: Int)

    @Delete
    suspend fun deleteMenuItem(item: MenuItem)

    @Query("DELETE FROM menu_items WHERE categoryId = :categoryId")
    suspend fun deleteMenuItemsByCategory(categoryId: Int)

    @Query("SELECT * FROM order_bills WHERE id = :billId")
    fun getBillFlow(billId: Long): Flow<OrderBill?>

    @Query("SELECT * FROM order_bills WHERE id = :billId")
    suspend fun getBillById(billId: Long): OrderBill?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: OrderBill): Long

    @Update
    suspend fun updateBill(bill: OrderBill)

    @Query("SELECT * FROM order_bills WHERE status = 'OPEN'")
    fun getOpenBills(): Flow<List<OrderBill>>

    @Query("SELECT * FROM order_bills WHERE status = 'CLOSED' ORDER BY closedAt DESC")
    fun getClosedBills(): Flow<List<OrderBill>>

    @Query("SELECT * FROM order_items WHERE billId = :billId ORDER BY addedAt ASC")
    fun getOrderItemsForBill(billId: Long): Flow<List<OrderItem>>

    @Query("SELECT * FROM order_items WHERE billId IN (SELECT id FROM order_bills WHERE status = 'OPEN')")
    fun getActiveOrderItems(): Flow<List<OrderItem>>

    @Query("SELECT * FROM menu_items")
    suspend fun getAllMenuItemsSync(): List<MenuItem>

    @Query("SELECT * FROM order_items WHERE billId = :billId ORDER BY addedAt ASC")
    suspend fun getOrderItemsForBillSync(billId: Long): List<OrderItem>

    @Query("SELECT * FROM order_items WHERE billId = :billId AND menuItemId = :menuItemId AND note = :note LIMIT 1")
    suspend fun findExistingOrderItem(billId: Long, menuItemId: Int, note: String): OrderItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(item: OrderItem): Long

    @Update
    suspend fun updateOrderItem(item: OrderItem)

    @Delete
    suspend fun deleteOrderItem(item: OrderItem)

    @Query("DELETE FROM order_items WHERE billId = :billId")
    suspend fun deleteAllItemsForBill(billId: Long)

    @Query("SELECT * FROM payment_records WHERE billId = :billId ORDER BY paidAt DESC")
    fun getPaymentsForBill(billId: Long): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records ORDER BY paidAt DESC LIMIT 50")
    fun getAllRecentPayments(): Flow<List<PaymentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecord): Long

    @Query("SELECT itemName, categoryName, SUM(quantity) as totalQty, SUM(unitPrice * quantity) as totalSales FROM order_items GROUP BY menuItemId ORDER BY totalQty DESC LIMIT 8")
    fun getTopSellingItems(): Flow<List<TopSellingItem>>

    @Query("SELECT * FROM payment_records WHERE paidAt >= :startTime ORDER BY paidAt DESC")
    suspend fun getPaymentsSince(startTime: Long): List<PaymentRecord>

    @Query("SELECT paymentMethod, SUM(amount) as totalAmount, COUNT(*) as transactionCount FROM payment_records WHERE paidAt >= :startTime GROUP BY paymentMethod ORDER BY totalAmount DESC")
    suspend fun getPaymentBreakdownSince(startTime: Long): List<PaymentMethodBreakdown>

    @Query(
        """
        SELECT oi.itemName as itemName,
               oi.categoryName as categoryName,
               SUM(oi.quantity) as totalQty,
               oi.unitPrice as unitPrice,
               SUM(oi.unitPrice * oi.quantity) as totalRevenue
        FROM order_items oi
        INNER JOIN order_bills ob ON oi.billId = ob.id
        WHERE ob.createdAt >= :startTime AND ob.status != 'CANCELLED'
        GROUP BY oi.menuItemId, oi.itemName, oi.unitPrice
        ORDER BY totalRevenue DESC
        """
    )
    suspend fun getProductBreakdownSince(startTime: Long): List<ProductSalesBreakdown>

    @Query("SELECT COUNT(*) FROM order_bills WHERE status = 'CLOSED' AND closedAt >= :startTime")
    suspend fun getClosedBillCountSince(startTime: Long): Int

    @Query("SELECT * FROM kitchen_tickets WHERE status != 'COMPLETED' ORDER BY createdAt ASC")
    fun getActiveKitchenTickets(): Flow<List<KitchenTicketEntity>>

    @Query("SELECT * FROM kitchen_tickets ORDER BY createdAt DESC LIMIT 100")
    fun getAllKitchenTickets(): Flow<List<KitchenTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKitchenTicket(ticket: KitchenTicketEntity)

    @Update
    suspend fun updateKitchenTicket(ticket: KitchenTicketEntity)

    @Query("UPDATE kitchen_tickets SET status = :status WHERE ticketId = :ticketId")
    suspend fun updateKitchenTicketStatus(ticketId: String, status: String)

    @Query("DELETE FROM kitchen_tickets WHERE tableId = :tableId")
    suspend fun deleteKitchenTicketsForTable(tableId: Int)
}
