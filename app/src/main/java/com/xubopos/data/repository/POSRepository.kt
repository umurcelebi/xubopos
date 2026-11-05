package com.xubopos.data.repository

import com.google.gson.Gson
import com.xubopos.data.dao.RestaurantDao
import com.xubopos.data.model.DailyReport
import com.xubopos.data.model.DetailedSalesReport
import com.xubopos.data.model.KitchenTicketEntity
import com.xubopos.data.model.KitchenTicketItemDto
import com.xubopos.data.model.MenuCategory
import com.xubopos.data.model.MenuItem
import com.xubopos.data.model.OrderBill
import com.xubopos.data.model.OrderItem
import com.xubopos.data.model.PaymentRecord
import com.xubopos.data.model.ReportPeriod
import com.xubopos.data.model.RestaurantTable
import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class POSRepository(private val dao: RestaurantDao) {
    val allTables: Flow<List<RestaurantTable>> = dao.getAllTables()
    val allCategories: Flow<List<MenuCategory>> = dao.getAllCategories()
    val allMenuItems: Flow<List<MenuItem>> = dao.getAllMenuItems()
    val openBills: Flow<List<OrderBill>> = dao.getOpenBills()
    val closedBills: Flow<List<OrderBill>> = dao.getClosedBills()
    val recentPayments: Flow<List<PaymentRecord>> = dao.getAllRecentPayments()
    val activeKitchenTickets: Flow<List<KitchenTicketEntity>> = dao.getActiveKitchenTickets()
    val activeOrderItems: Flow<List<OrderItem>> = dao.getActiveOrderItems()

    suspend fun getAllMenuItemsSync(): List<MenuItem> = dao.getAllMenuItemsSync()

    fun getBillFlow(billId: Long): Flow<OrderBill?> = dao.getBillFlow(billId)

    fun getOrderItemsForBill(billId: Long): Flow<List<OrderItem>> = dao.getOrderItemsForBill(billId)

    fun getPaymentsForBill(billId: Long): Flow<List<PaymentRecord>> = dao.getPaymentsForBill(billId)

    fun getDailyReportFlow(lastCloseTimeFlow: Flow<Long>): Flow<DailyReport> {
        return combine(
            dao.getClosedBills(),
            dao.getOpenBills(),
            dao.getAllRecentPayments(),
            dao.getTopSellingItems(),
            lastCloseTimeFlow
        ) { closed, open, payments, topItems, lastClose ->
            val calendarStartOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val effectiveStartTime = maxOf(calendarStartOfDay, lastClose)

            val todaysPayments = payments.filter { it.paidAt >= effectiveStartTime }
            val todaysClosed = closed.filter { (it.closedAt ?: 0L) >= effectiveStartTime }

            val cashTotal = todaysPayments
                .filter { it.paymentMethod == "NAKİT" }
                .sumOf { it.amount }
            val cardTotal = todaysPayments
                .filter { it.paymentMethod == "KART" }
                .sumOf { it.amount }
            val totalRevenue = todaysPayments.sumOf { it.amount }
            val openTablesTotal = open.sumOf { it.remainingAmount }

            DailyReport(
                totalRevenue = totalRevenue,
                cashTotal = cashTotal,
                cardTotal = cardTotal,
                closedOrderCount = todaysClosed.size,
                openOrderCount = open.size,
                openTablesTotal = openTablesTotal,
                topItems = topItems,
                recentPayments = todaysPayments.take(15)
            )
        }
    }

    suspend fun getDetailedReport(period: ReportPeriod, lastDayCloseTime: Long = 0L): DetailedSalesReport {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = when (period) {
            ReportPeriod.TODAY -> maxOf(cal.timeInMillis, lastDayCloseTime)
            ReportPeriod.WEEK -> {
                cal.add(Calendar.DAY_OF_YEAR, -6)
                cal.timeInMillis
            }
            ReportPeriod.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.timeInMillis
            }
            ReportPeriod.YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.timeInMillis
            }
            ReportPeriod.ALL -> 0L
        }

        val payments = dao.getPaymentsSince(startTime)
        val paymentBreakdown = dao.getPaymentBreakdownSince(startTime)
        val productBreakdown = dao.getProductBreakdownSince(startTime)
        val closedBillCount = dao.getClosedBillCountSince(startTime)
        val totalRevenue = payments.sumOf { it.amount }
        val averageBillAmount = if (closedBillCount > 0) {
            totalRevenue / closedBillCount
        } else if (payments.isNotEmpty()) {
            totalRevenue / payments.size
        } else {
            0.0
        }

        return DetailedSalesReport(
            period = period,
            totalRevenue = totalRevenue,
            totalTransactions = payments.size,
            closedBillCount = closedBillCount,
            averageBillAmount = averageBillAmount,
            paymentBreakdown = paymentBreakdown,
            productBreakdown = productBreakdown,
            paymentHistory = payments
        )
    }

    suspend fun addMenuCategory(name: String, iconName: String) {
        dao.insertCategory(
            MenuCategory(
                name = name.trim(),
                iconName = iconName,
                sortOrder = (System.currentTimeMillis() % 10000).toInt()
            )
        )
    }

    suspend fun updateMenuCategory(category: MenuCategory) {
        dao.updateCategory(category)
    }

    suspend fun deleteMenuCategory(category: MenuCategory) {
        dao.deleteMenuItemsByCategory(category.id)
        dao.deleteCategory(category)
    }

    suspend fun addNewTable(name: String, zone: String, capacity: Int) {
        dao.insertTable(
            RestaurantTable(
                name = name.trim(),
                zone = zone.trim(),
                capacity = capacity.coerceIn(1, 24)
            )
        )
    }

    suspend fun updateTableInfo(table: RestaurantTable, name: String, zone: String, capacity: Int) {
        dao.updateTable(
            table.copy(
                name = name.trim(),
                zone = zone.trim(),
                capacity = capacity.coerceIn(1, 24)
            )
        )
    }

    suspend fun deleteTableIfEmpty(table: RestaurantTable): Boolean {
        if (table.isOccupied) return false
        dao.deleteTable(table)
        return true
    }

    suspend fun addMenuItemToBill(tableId: Int, menuItem: MenuItem, categoryName: String, note: String = ""): Boolean {
        val freshItem = dao.getMenuItemById(menuItem.id) ?: menuItem
        if (freshItem.hasDailyLimit && freshItem.remainingStock <= 0) {
            return false
        }

        val table = dao.getTableById(tableId) ?: return false
        var billId = table.currentBillId
        val now = System.currentTimeMillis()

        if (!table.isOccupied || billId == null) {
            billId = dao.insertBill(
                OrderBill(
                    tableId = table.id,
                    tableName = table.name,
                    status = "OPEN",
                    totalAmount = 0.0,
                    paidAmount = 0.0,
                    createdAt = now
                )
            )
            dao.updateTable(
                table.copy(
                    isOccupied = true,
                    currentBillId = billId,
                    occupiedSince = now,
                    currentTotalAmount = 0.0,
                    currentPaidAmount = 0.0
                )
            )
        }

        val existing = dao.findExistingOrderItem(billId, menuItem.id, note)
        if (existing != null) {
            dao.updateOrderItem(existing.copy(quantity = existing.quantity + 1))
        } else {
            dao.insertOrderItem(
                OrderItem(
                    billId = billId,
                    menuItemId = menuItem.id,
                    itemName = menuItem.name,
                    categoryName = categoryName,
                    unitPrice = menuItem.price,
                    quantity = 1,
                    note = note
                )
            )
        }

        if (freshItem.hasDailyLimit) {
            dao.updateRemainingStock(freshItem.id, (freshItem.remainingStock - 1).coerceAtLeast(0))
        }

        recalculateBillAndTable(billId, tableId)
        return true
    }

    suspend fun incrementOrderItem(tableId: Int, item: OrderItem): Boolean {
        item.menuItemId?.let { mId ->
            val mItem = dao.getMenuItemById(mId)
            if (mItem != null && mItem.hasDailyLimit) {
                if (mItem.remainingStock <= 0) return false
                dao.updateRemainingStock(mId, (mItem.remainingStock - 1).coerceAtLeast(0))
            }
        }
        dao.updateOrderItem(item.copy(quantity = item.quantity + 1))
        recalculateBillAndTable(item.billId, tableId)
        return true
    }

    suspend fun decrementOrderItem(tableId: Int, item: OrderItem) {
        val minKeep = item.paidQuantity.coerceAtLeast(0)
        if (item.quantity > 1 && item.quantity > minKeep) {
            dao.updateOrderItem(item.copy(quantity = item.quantity - 1))
            item.menuItemId?.let { mId ->
                dao.getMenuItemById(mId)?.let { m ->
                    if (m.hasDailyLimit) {
                        dao.updateRemainingStock(mId, (m.remainingStock + 1).coerceAtMost(m.dailyLimit))
                    }
                }
            }
        } else if (item.paidQuantity == 0) {
            dao.deleteOrderItem(item)
            item.menuItemId?.let { mId ->
                dao.getMenuItemById(mId)?.let { m ->
                    if (m.hasDailyLimit) {
                        dao.updateRemainingStock(mId, (m.remainingStock + 1).coerceAtMost(m.dailyLimit))
                    }
                }
            }
        }
        recalculateBillAndTable(item.billId, tableId)
    }

    suspend fun removeOrderItem(tableId: Int, item: OrderItem) {
        val returnedQuantity = (item.quantity - item.paidQuantity).coerceAtLeast(0)
        if (item.paidQuantity > 0) {
            dao.updateOrderItem(item.copy(quantity = item.paidQuantity))
        } else {
            dao.deleteOrderItem(item)
        }
        if (returnedQuantity > 0) {
            item.menuItemId?.let { mId ->
                dao.getMenuItemById(mId)?.let { m ->
                    if (m.hasDailyLimit) {
                        dao.updateRemainingStock(mId, (m.remainingStock + returnedQuantity).coerceAtMost(m.dailyLimit))
                    }
                }
            }
        }
        recalculateBillAndTable(item.billId, tableId)
    }

    suspend fun updateOrderItemNote(item: OrderItem, newNote: String) {
        dao.updateOrderItem(item.copy(note = newNote))
    }

    suspend fun decrementOrderItemById(tableId: Int, itemId: Long) {
        val item = dao.getOrderItemById(itemId) ?: return
        decrementOrderItem(tableId, item)
    }

    suspend fun removeOrderItemById(tableId: Int, itemId: Long) {
        val item = dao.getOrderItemById(itemId) ?: return
        removeOrderItem(tableId, item)
    }

    suspend fun updateOrderItemNoteById(itemId: Long, newNote: String) {
        val item = dao.getOrderItemById(itemId) ?: return
        updateOrderItemNote(item, newNote)
    }

    suspend fun applyDiscountToBill(tableId: Int, discountAmount: Double) {
        val table = dao.getTableById(tableId) ?: return
        val billId = table.currentBillId ?: return
        val bill = dao.getBillById(billId) ?: return
        val maxAllowedDiscount = (bill.totalAmount - bill.paidAmount).coerceAtLeast(0.0)
        val clampedDiscount = discountAmount.coerceIn(0.0, maxAllowedDiscount)
        dao.updateBill(bill.copy(discountAmount = clampedDiscount))
        recalculateBillAndTable(billId, tableId)
    }

    suspend fun refundPayment(tableId: Int, payment: PaymentRecord) {
        val table = dao.getTableById(tableId) ?: return
        val billId = table.currentBillId ?: return
        val bill = dao.getBillById(billId) ?: return
        dao.deletePayment(payment)
        val newPaid = (bill.paidAmount - payment.amount).coerceAtLeast(0.0)
        dao.updateBill(bill.copy(paidAmount = newPaid))
        recalculateBillAndTable(billId, tableId)
    }

    suspend fun togglePaymentMethod(payment: PaymentRecord) {
        val nextMethod = if (payment.paymentMethod == "NAKİT") "KART" else "NAKİT"
        dao.updatePayment(payment.copy(paymentMethod = nextMethod))
    }

    suspend fun processPayment(
        tableId: Int,
        amount: Double,
        paymentMethod: String,
        note: String = "",
        closeTableIfPaid: Boolean = true,
        paidItemQuantities: Map<Long, Int> = emptyMap()
    ): Boolean {
        val table = dao.getTableById(tableId) ?: return false
        val billId = table.currentBillId ?: return false
        val bill = dao.getBillById(billId) ?: return false
        val actualPayment = amount.coerceAtMost(bill.remainingAmount)
        if (actualPayment <= 0.0) return false

        if (paidItemQuantities.isNotEmpty()) {
            val currentItems = dao.getOrderItemsForBillSync(billId)
            for (orderItem in currentItems) {
                val addPaid = paidItemQuantities[orderItem.id] ?: 0
                if (addPaid > 0) {
                    val newPaid = (orderItem.paidQuantity + addPaid).coerceAtMost(orderItem.quantity)
                    dao.updateOrderItem(orderItem.copy(paidQuantity = newPaid))
                }
            }
        }

        dao.insertPayment(
            PaymentRecord(
                billId = billId,
                tableName = table.name,
                amount = actualPayment,
                paymentMethod = paymentMethod,
                note = note,
                paidAt = System.currentTimeMillis()
            )
        )

        val newPaidAmount = bill.paidAmount + actualPayment
        val updatedBill = bill.copy(paidAmount = newPaidAmount)
        val isAllPaid = updatedBill.remainingAmount <= 0.01

        if (isAllPaid && closeTableIfPaid) {
            dao.deleteKitchenTicketsForTable(tableId)
            dao.updateBill(
                updatedBill.copy(
                    status = "CLOSED",
                    closedAt = System.currentTimeMillis()
                )
            )
            dao.updateTable(
                table.copy(
                    isOccupied = false,
                    currentBillId = null,
                    occupiedSince = null,
                    currentTotalAmount = 0.0,
                    currentPaidAmount = 0.0
                )
            )
            return true
        } else {
            dao.updateBill(updatedBill)
            dao.updateTable(table.copy(currentPaidAmount = newPaidAmount))
            return false
        }
    }

    suspend fun cancelAndClearTable(tableId: Int) {
        dao.deleteKitchenTicketsForTable(tableId)
        val table = dao.getTableById(tableId) ?: return
        val billId = table.currentBillId
        if (billId != null) {
            // İptal edilen masadaki ödenmemiş ürünlerin günlük stok kotalarını iade et
            val currentItems = dao.getOrderItemsForBillSync(billId)
            for (item in currentItems) {
                val unpaidQty = (item.quantity - item.paidQuantity).coerceAtLeast(0)
                if (unpaidQty > 0) {
                    item.menuItemId?.let { mId ->
                        dao.getMenuItemById(mId)?.let { m ->
                            if (m.hasDailyLimit) {
                                dao.updateRemainingStock(mId, (m.remainingStock + unpaidQty).coerceAtMost(m.dailyLimit))
                            }
                        }
                    }
                }
            }
            dao.deleteAllItemsForBill(billId)
            val bill = dao.getBillById(billId)
            if (bill != null) {
                dao.updateBill(
                    bill.copy(
                        status = "CANCELLED",
                        closedAt = System.currentTimeMillis()
                    )
                )
            }
        }
        dao.updateTable(
            table.copy(
                isOccupied = false,
                currentBillId = null,
                occupiedSince = null,
                currentTotalAmount = 0.0,
                currentPaidAmount = 0.0
            )
        )
    }

    suspend fun transferTable(fromTableId: Int, toTableId: Int) {
        if (fromTableId == toTableId) return
        val fromTable = dao.getTableById(fromTableId) ?: return
        val toTable = dao.getTableById(toTableId) ?: return
        val fromBillId = fromTable.currentBillId ?: return
        val fromBill = dao.getBillById(fromBillId) ?: return

        if (!toTable.isOccupied || toTable.currentBillId == null) {
            dao.updateBill(
                fromBill.copy(
                    tableId = toTable.id,
                    tableName = toTable.name
                )
            )
            dao.transferPaymentsToBill(fromBillId, fromBillId, toTable.name)
            dao.updateTable(
                toTable.copy(
                    isOccupied = true,
                    currentBillId = fromBillId,
                    occupiedSince = fromTable.occupiedSince ?: System.currentTimeMillis(),
                    currentTotalAmount = fromTable.currentTotalAmount,
                    currentPaidAmount = fromTable.currentPaidAmount
                )
            )
            dao.updateTable(
                fromTable.copy(
                    isOccupied = false,
                    currentBillId = null,
                    occupiedSince = null,
                    currentTotalAmount = 0.0,
                    currentPaidAmount = 0.0
                )
            )
            // Mutfak fişini yeni masaya taşı
            val fromTickets = dao.getActiveKitchenTicketsForTable(fromTableId)
            for (tkt in fromTickets) {
                dao.updateKitchenTicket(tkt.copy(tableId = toTable.id, tableName = toTable.name))
            }
        } else {
            val toBillId = toTable.currentBillId
            val toBill = dao.getBillById(toBillId) ?: return
            val fromItems = dao.getOrderItemsForBillSync(fromBillId)
            for (item in fromItems) {
                val existing = dao.findExistingOrderItem(toBillId, item.menuItemId, item.note)
                if (existing != null) {
                    dao.updateOrderItem(
                        existing.copy(
                            quantity = existing.quantity + item.quantity,
                            paidQuantity = existing.paidQuantity + item.paidQuantity
                        )
                    )
                    dao.deleteOrderItem(item)
                } else {
                    dao.updateOrderItem(item.copy(billId = toBillId))
                }
            }
            dao.transferPaymentsToBill(fromBillId, toBillId, toTable.name)
            dao.updateBill(
                toBill.copy(
                    paidAmount = toBill.paidAmount + fromBill.paidAmount,
                    discountAmount = toBill.discountAmount + fromBill.discountAmount
                )
            )
            dao.updateBill(
                fromBill.copy(
                    status = "MERGED",
                    closedAt = System.currentTimeMillis()
                )
            )
            dao.updateTable(
                fromTable.copy(
                    isOccupied = false,
                    currentBillId = null,
                    occupiedSince = null,
                    currentTotalAmount = 0.0,
                    currentPaidAmount = 0.0
                )
            )
            // Eski masanın aktif mutfak fişi varsa yeni masanın ürünleriyle güncelle veya taşı
            val fromTickets = dao.getActiveKitchenTicketsForTable(fromTableId)
            val toTickets = dao.getActiveKitchenTicketsForTable(toTable.id)
            if (fromTickets.isNotEmpty()) {
                if (toTickets.isEmpty()) {
                    val primary = fromTickets.first()
                    dao.updateKitchenTicket(primary.copy(tableId = toTable.id, tableName = toTable.name))
                    for (extra in fromTickets.drop(1)) {
                        dao.updateKitchenTicketStatus(extra.ticketId, "COMPLETED")
                    }
                } else {
                    dao.deleteKitchenTicketsForTable(fromTableId)
                    val mergedItems = dao.getOrderItemsForBillSync(toBillId).map {
                        KitchenTicketItemDto(
                            id = it.id,
                            menuItemId = it.menuItemId,
                            itemName = it.itemName,
                            categoryName = it.categoryName,
                            quantity = it.quantity,
                            note = it.note
                        )
                    }
                    createKitchenTicket(toTable.id, toTable.name, "Kasa", mergedItems)
                }
            }
            recalculateBillAndTable(toBillId, toTable.id)
        }
    }

    suspend fun transferSelectedItems(
        fromTableId: Int,
        toTableId: Int,
        selectedQuantities: Map<Long, Int>
    ) {
        if (fromTableId == toTableId || selectedQuantities.isEmpty()) return
        val fromTable = dao.getTableById(fromTableId) ?: return
        val toTable = dao.getTableById(toTableId) ?: return
        val fromBillId = fromTable.currentBillId ?: return

        val now = System.currentTimeMillis()
        var toBillId = toTable.currentBillId
        if (!toTable.isOccupied || toBillId == null) {
            toBillId = dao.insertBill(
                OrderBill(
                    tableId = toTable.id,
                    tableName = toTable.name,
                    status = "OPEN",
                    totalAmount = 0.0,
                    paidAmount = 0.0,
                    createdAt = now
                )
            )
            dao.updateTable(
                toTable.copy(
                    isOccupied = true,
                    currentBillId = toBillId,
                    occupiedSince = now,
                    currentTotalAmount = 0.0,
                    currentPaidAmount = 0.0
                )
            )
        }

        val fromItems = dao.getOrderItemsForBillSync(fromBillId)
        for (item in fromItems) {
            val moveQty = (selectedQuantities[item.id] ?: 0).coerceIn(0, item.unpaidQuantity)
            if (moveQty <= 0) continue

            // Hedef masaya ekle
            val existingInTarget = dao.findExistingOrderItem(toBillId, item.menuItemId, item.note)
            if (existingInTarget != null) {
                dao.updateOrderItem(existingInTarget.copy(quantity = existingInTarget.quantity + moveQty))
            } else {
                dao.insertOrderItem(
                    OrderItem(
                        billId = toBillId,
                        menuItemId = item.menuItemId,
                        itemName = item.itemName,
                        categoryName = item.categoryName,
                        unitPrice = item.unitPrice,
                        quantity = moveQty,
                        note = item.note
                    )
                )
            }

            // Kaynak masadan düş veya sil
            val remainingQty = item.quantity - moveQty
            if (remainingQty <= 0) {
                dao.deleteOrderItem(item)
            } else {
                dao.updateOrderItem(item.copy(quantity = remainingQty))
            }
        }

        recalculateBillAndTable(fromBillId, fromTableId)
        recalculateBillAndTable(toBillId, toTableId)
    }

    suspend fun addCustomMenuItem(
        categoryId: Int,
        name: String,
        price: Double,
        description: String = "",
        hasDailyLimit: Boolean = false,
        dailyLimit: Int = 0
    ) {
        dao.insertMenuItem(
            MenuItem(
                categoryId = categoryId,
                name = name,
                description = description,
                price = price,
                isAvailable = true,
                hasDailyLimit = hasDailyLimit,
                dailyLimit = dailyLimit,
                remainingStock = if (hasDailyLimit) dailyLimit else 0
            )
        )
    }

    suspend fun resetDailyStockLimits() {
        dao.resetAllDailyStocks()
    }

    suspend fun updateMenuItem(item: MenuItem) {
        dao.updateMenuItem(item)
    }

    suspend fun deleteMenuItem(item: MenuItem) {
        dao.deleteMenuItem(item)
    }

    suspend fun getActiveOrderItemsForTable(tableId: Int): List<OrderItem> {
        val table = dao.getTableById(tableId) ?: return emptyList()
        val billId = table.currentBillId ?: return emptyList()
        return dao.getOrderItemsForBillSync(billId)
    }

    suspend fun createKitchenTicket(
        tableId: Int,
        tableName: String,
        waiterName: String,
        items: List<KitchenTicketItemDto>
    ): KitchenTicketEntity? {
        if (items.isEmpty()) {
            dao.deleteKitchenTicketsForTable(tableId)
            return null
        }
        val existingTickets = dao.getActiveKitchenTicketsForTable(tableId)
        val currentTime = System.currentTimeMillis()

        if (existingTickets.isNotEmpty()) {
            val primaryTicket = existingTickets.first()
            val prevItems = primaryTicket.getItems()

            val diffedItems = items.map { newItem ->
                val matchedPrev = prevItems.find {
                    (it.id != 0L && it.id == newItem.id) ||
                        (it.itemName == newItem.itemName && it.note == newItem.note)
                }
                if (matchedPrev == null) {
                    newItem.copy(isNew = true, addedQuantity = newItem.quantity)
                } else if (newItem.quantity > matchedPrev.quantity) {
                    newItem.copy(isNew = true, addedQuantity = newItem.quantity - matchedPrev.quantity)
                } else {
                    newItem.copy(isNew = false, addedQuantity = 0)
                }
            }

            val itemsJson = Gson().toJson(diffedItems)
            val updated = primaryTicket.copy(
                tableName = tableName,
                waiterName = waiterName,
                createdAt = currentTime,
                itemsJson = itemsJson,
                status = "PENDING"
            )
            dao.updateKitchenTicket(updated)

            // Varsa diğer mükerrer aktif fişleri sonlandır
            if (existingTickets.size > 1) {
                for (extra in existingTickets.drop(1)) {
                    dao.updateKitchenTicketStatus(extra.ticketId, "COMPLETED")
                }
            }
            return updated
        } else {
            val itemsJson = Gson().toJson(items)
            val ticketId = "TKT-${(currentTime % 100000)}"
            val entity = KitchenTicketEntity(
                ticketId = ticketId,
                tableId = tableId,
                tableName = tableName,
                waiterName = waiterName,
                createdAt = currentTime,
                itemsJson = itemsJson,
                status = "PENDING"
            )
            dao.insertKitchenTicket(entity)
            return entity
        }
    }

    suspend fun getKitchenTicketById(ticketId: String): KitchenTicketEntity? {
        return dao.getKitchenTicketById(ticketId)
    }

    suspend fun updateKitchenTicketStatus(ticketId: String, status: String) {
        dao.updateKitchenTicketStatus(ticketId, status)
    }

    suspend fun deleteKitchenTicketsForTable(tableId: Int) {
        dao.deleteKitchenTicketsForTable(tableId)
    }

    suspend fun exportMenuJson(): String {
        val categories = dao.getAllCategoriesSync()
        val items = dao.getAllMenuItemsSync()
        val backup = MenuBackupPayload(categories = categories, menuItems = items)
        return Gson().toJson(backup)
    }

    suspend fun importMenuJson(json: String): Boolean {
        return try {
            val backup = Gson().fromJson(json, MenuBackupPayload::class.java) ?: return false
            if (backup.categories.isEmpty() && backup.menuItems.isEmpty()) return false
            if (backup.categories.isNotEmpty()) {
                dao.insertCategories(backup.categories)
            }
            if (backup.menuItems.isNotEmpty()) {
                dao.insertMenuItems(backup.menuItems)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun recalculateBillAndTable(billId: Long, tableId: Int) {
        val items = dao.getOrderItemsForBillSync(billId)
        val bill = dao.getBillById(billId) ?: return
        val table = dao.getTableById(tableId) ?: return

        if (items.isEmpty()) {
            dao.deleteKitchenTicketsForTable(tableId)
            if (bill.paidAmount <= 0.0) {
                dao.updateBill(
                    bill.copy(
                        totalAmount = 0.0,
                        status = "CANCELLED",
                        closedAt = System.currentTimeMillis()
                    )
                )
                dao.updateTable(
                    table.copy(
                        isOccupied = false,
                        currentBillId = null,
                        occupiedSince = null,
                        currentTotalAmount = 0.0,
                        currentPaidAmount = 0.0
                    )
                )
                return
            } else {
                dao.updateBill(bill.copy(totalAmount = 0.0))
                dao.updateTable(
                    table.copy(
                        currentTotalAmount = 0.0,
                        currentPaidAmount = bill.paidAmount
                    )
                )
                return
            }
        }

        val rawTotal = items.sumOf { it.subtotal }
        val effectiveTotal = (rawTotal - bill.discountAmount).coerceAtLeast(0.0)
        dao.updateBill(bill.copy(totalAmount = rawTotal))
        dao.updateTable(
            table.copy(
                isOccupied = true,
                currentBillId = billId,
                currentTotalAmount = effectiveTotal,
                currentPaidAmount = bill.paidAmount
            )
        )
    }

    suspend fun seedInitialDataIfNeeded() {
        if (dao.getTableCount() > 0) return

        val tables = buildList {
            for (i in 1..8) {
                add(RestaurantTable(name = "Salon $i", zone = "Ana Salon", capacity = if (i % 2 == 0) 4 else 2))
            }
            for (i in 1..5) {
                add(RestaurantTable(name = "Bahçe $i", zone = "Bahçe", capacity = 4))
            }
            for (i in 1..4) {
                add(RestaurantTable(name = "Teras $i", zone = "Teras", capacity = 6))
            }
            for (i in 1..3) {
                add(RestaurantTable(name = "VIP $i", zone = "VIP Loca", capacity = 8))
            }
        }
        dao.insertTables(tables)

        val categories = listOf(
            MenuCategory(id = 1, name = "Ana Yemekler", iconName = "dinner", sortOrder = 1),
            MenuCategory(id = 2, name = "Burger & Pizza", iconName = "fastfood", sortOrder = 2),
            MenuCategory(id = 3, name = "Başlangıç & Salata", iconName = "salad", sortOrder = 3),
            MenuCategory(id = 4, name = "Sıcak İçecekler", iconName = "coffee", sortOrder = 4),
            MenuCategory(id = 5, name = "Soğuk İçecekler", iconName = "drink", sortOrder = 5),
            MenuCategory(id = 6, name = "Tatlılar", iconName = "dessert", sortOrder = 6)
        )
        dao.insertCategories(categories)

        val menuItems = listOf(
            MenuItem(categoryId = 1, name = "Adana Kebap", description = "Közlenmiş biber, domates ve lavaş ile", price = 380.0),
            MenuItem(categoryId = 1, name = "Kuzu Şiş", description = "Özel marine edilmiş kuzu lokum", price = 440.0),
            MenuItem(categoryId = 1, name = "Karışık Izgara", description = "Adana, pirzola, kanat, köfte", price = 620.0),
            MenuItem(categoryId = 1, name = "Kasap Köfte", description = "Pilav ve patates kızartması eşliğinde", price = 340.0),
            MenuItem(categoryId = 1, name = "Kremalı Mantarlı Tavuk", description = "Penne makarna ve taze baharatlar ile", price = 310.0),
            MenuItem(categoryId = 1, name = "Dana Antrikot (250gr)", description = "Fırın bebek patates ve demi-glace sos", price = 580.0),
            MenuItem(categoryId = 2, name = "Trüflü Smash Burger", description = "Çift köfte, cheddar, karamelize soğan, patates", price = 330.0),
            MenuItem(categoryId = 2, name = "Klasik Cheeseburger", description = "160gr dana burger, turşu, özel sos", price = 290.0),
            MenuItem(categoryId = 2, name = "Margherita Pizza", description = "Mozzarella, fesleğen, İtalyan domates sos", price = 310.0),
            MenuItem(categoryId = 2, name = "Dört Peynirli Pizza", description = "Mozzarella, rokfor, parmesan, tulum", price = 360.0),
            MenuItem(categoryId = 2, name = "Kavurmalı Odun Pizza", description = "Roka ve parmesan dilimleri ile", price = 410.0),
            MenuItem(categoryId = 3, name = "Günün Çorbası", description = "Kızarmış tereyağlı kruton ile", price = 110.0),
            MenuItem(categoryId = 3, name = "Sezar Salata (Tavuklu)", description = "Izgara tavuk, parmesan, kruton", price = 250.0),
            MenuItem(categoryId = 3, name = "Tulum Peynirli Bahçe Salata", description = "Ceviz, nar ekşisi, taze yeşillik", price = 230.0),
            MenuItem(categoryId = 3, name = "Baharatlı Patates Sepeti", description = "Cheddar sos ve acı mayonez ile", price = 145.0),
            MenuItem(categoryId = 3, name = "Paçanga Böreği (2 Adet)", description = "Pastırmalı ve kaşarlı sıcak başlangıç", price = 175.0),
            MenuItem(categoryId = 4, name = "Demleme Çay", description = "İnce belli bardak", price = 35.0),
            MenuItem(categoryId = 4, name = "Fincan Çay", description = "Büyük fincan", price = 55.0),
            MenuItem(categoryId = 4, name = "Türk Kahvesi", description = "Çifte kavrulmuş lokum ile", price = 85.0),
            MenuItem(categoryId = 4, name = "Americano", description = "100% Arabica çekirdek", price = 105.0),
            MenuItem(categoryId = 4, name = "Cappuccino / Latte", description = "Taze süt köpüğü ile", price = 120.0),
            MenuItem(categoryId = 5, name = "Ev Yapımı Limonata", description = "Taze nane yaprakları ile", price = 95.0),
            MenuItem(categoryId = 5, name = "Yayık Ayran", description = "Bakır maşrapada bol köpüklü", price = 65.0),
            MenuItem(categoryId = 5, name = "Kola / Fanta / Sprite", description = "330ml Kutu", price = 75.0),
            MenuItem(categoryId = 5, name = "Taze Sıkma Portakal Suyu", description = "Doğal 300ml", price = 115.0),
            MenuItem(categoryId = 5, name = "Cam Şişe Su (330ml)", description = "Doğal kaynak suyu", price = 30.0),
            MenuItem(categoryId = 5, name = "Sade Maden Suyu", description = "Limon dilimi ile", price = 45.0),
            MenuItem(categoryId = 6, name = "San Sebastian Cheesecake", description = "Belçika çikolata sosu ile", price = 210.0),
            MenuItem(categoryId = 6, name = "Fıstıklı Katmer / Künefe", description = "Kaymak eşliğinde sıcak servis", price = 240.0),
            MenuItem(categoryId = 6, name = "Fırın Sütlaç", description = "Bol fındıklı taş fırın sütlacı", price = 150.0),
            MenuItem(categoryId = 6, name = "Sıcak Çikolatalı Sufle", description = "Vanilyalı dondurma topu ile", price = 195.0)
        )
        dao.insertMenuItems(menuItems)
    }
}

data class MenuBackupPayload(
    val categories: List<MenuCategory> = emptyList(),
    val menuItems: List<MenuItem> = emptyList()
)
