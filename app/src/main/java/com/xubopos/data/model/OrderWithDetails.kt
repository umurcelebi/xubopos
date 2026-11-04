package com.xubopos.data.model

data class OrderWithDetails(
    val bill: OrderBill,
    val items: List<OrderItem>,
    val payments: List<PaymentRecord>
)

data class DailyReport(
    val totalRevenue: Double = 0.0,
    val cashTotal: Double = 0.0,
    val cardTotal: Double = 0.0,
    val closedOrderCount: Int = 0,
    val openOrderCount: Int = 0,
    val openTablesTotal: Double = 0.0,
    val topItems: List<TopSellingItem> = emptyList(),
    val recentPayments: List<PaymentRecord> = emptyList()
)

data class TopSellingItem(
    val itemName: String,
    val categoryName: String,
    val totalQty: Int,
    val totalSales: Double
)

enum class ReportPeriod(val label: String) {
    TODAY("Bugün"),
    WEEK("Bu Hafta"),
    MONTH("Bu Ay"),
    YEAR("Bu Yıl"),
    ALL("Tüm Zamanlar")
}

data class PaymentMethodBreakdown(
    val paymentMethod: String,
    val totalAmount: Double,
    val transactionCount: Int
)

data class ProductSalesBreakdown(
    val itemName: String,
    val categoryName: String,
    val totalQty: Int,
    val unitPrice: Double,
    val totalRevenue: Double
)

data class DetailedSalesReport(
    val period: ReportPeriod = ReportPeriod.TODAY,
    val totalRevenue: Double = 0.0,
    val totalTransactions: Int = 0,
    val closedBillCount: Int = 0,
    val averageBillAmount: Double = 0.0,
    val paymentBreakdown: List<PaymentMethodBreakdown> = emptyList(),
    val productBreakdown: List<ProductSalesBreakdown> = emptyList(),
    val paymentHistory: List<PaymentRecord> = emptyList()
)
