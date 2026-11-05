package com.xubopos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xubopos.data.model.OrderBill
import com.xubopos.data.model.OrderItem
import com.xubopos.data.model.PaymentRecord
import com.xubopos.ui.theme.Amber100
import com.xubopos.ui.theme.Amber50
import com.xubopos.ui.theme.Amber600
import com.xubopos.ui.theme.Amber700
import com.xubopos.ui.theme.Emerald100
import com.xubopos.ui.theme.Emerald50
import com.xubopos.ui.theme.Emerald600
import com.xubopos.ui.theme.Emerald700
import com.xubopos.ui.theme.PureWhite
import com.xubopos.ui.theme.Rose600
import com.xubopos.ui.theme.Sky100
import com.xubopos.ui.theme.Sky600
import com.xubopos.ui.theme.Sky700
import com.xubopos.ui.theme.Slate100
import com.xubopos.ui.theme.Slate200
import com.xubopos.ui.theme.Slate400
import com.xubopos.ui.theme.Slate500
import com.xubopos.ui.theme.Slate700
import com.xubopos.ui.theme.Slate800
import com.xubopos.ui.theme.Slate900

@Composable
fun PaymentDialog(
    tableName: String,
    bill: OrderBill,
    orderItems: List<OrderItem>,
    payments: List<PaymentRecord>,
    onDismiss: () -> Unit,
    onApplyDiscount: (Double) -> Unit,
    onProcessPayment: (Double, String, String, Boolean, Map<Long, Int>) -> Unit
) {
    val remaining = bill.remainingAmount
    var selectedTab by remember { mutableIntStateOf(0) }
    var customAmountText by remember(remaining) {
        mutableStateOf("%.2f".format(remaining).replace(",", "."))
    }
    var splitCount by remember { mutableIntStateOf(2) }
    var discountInput by remember {
        mutableStateOf(if (bill.discountAmount > 0) "%.0f".format(bill.discountAmount) else "")
    }
    var showDiscountRow by remember { mutableStateOf(false) }

    val selectedItemCounts = remember { mutableStateMapOf<Long, Int>() }
    val selectedItemsTotal = orderItems.sumOf { item ->
        val count = selectedItemCounts[item.id] ?: 0
        count * item.unitPrice
    }

    val amountToPay = when (selectedTab) {
        0 -> remaining
        1 -> selectedItemsTotal.coerceAtMost(remaining)
        2 -> (remaining / splitCount).coerceAtMost(remaining)
        else -> (customAmountText.replace(",", ".").toDoubleOrNull() ?: 0.0).coerceAtMost(remaining)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(24.dp),
            color = PureWhite,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$tableName • Tahsilat & Hesap",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900
                        )
                        Text(
                            text = "Parçalı, ürün bazlı veya tam ödeme alabilirsiniz",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Slate100,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Toplam Adisyon", fontSize = 11.sp, color = Slate500)
                            Text(
                                text = "₺${"%.2f".format(bill.totalAmount)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                        }
                        if (bill.discountAmount > 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "İndirim", fontSize = 11.sp, color = Rose600)
                                Text(
                                    text = "-₺${"%.2f".format(bill.discountAmount)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Rose600
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Ödenen", fontSize = 11.sp, color = Emerald700)
                            Text(
                                text = "₺${"%.2f".format(bill.paidAmount)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald700
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Kalan Tutar", fontSize = 11.sp, color = Amber700)
                            Text(
                                text = "₺${"%.2f".format(remaining)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Amber700
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = showDiscountRow,
                        onClick = { showDiscountRow = !showDiscountRow },
                        label = {
                            Text(
                                text = if (bill.discountAmount > 0) {
                                    "İndirim: ₺${"%.0f".format(bill.discountAmount)}"
                                } else {
                                    "İndirim Uygula"
                                },
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Discount,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    if (payments.isNotEmpty()) {
                        Text(
                            text = "${payments.size} ara ödeme alındı",
                            fontSize = 12.sp,
                            color = Sky700,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (showDiscountRow) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text("İndirim Tutarı (₺)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val disc = discountInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                                onApplyDiscount(disc)
                                showDiscountRow = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800)
                        ) {
                            Text("Uygula")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val tabs = listOf("Tamamı", "Ürün Seç", "Kişi Böl", "Özel Tutar")
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Slate100,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    when (selectedTab) {
                        0 -> {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Emerald50,
                                border = BorderStroke(1.dp, Emerald100),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Kalan Tüm Hesabı Kapat",
                                        fontSize = 14.sp,
                                        color = Emerald700,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "₺${"%.2f".format(remaining)}",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Emerald700
                                    )
                                    Text(
                                        text = "Ödeme tamamlandığında masa otomatik olarak boşaltılır.",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                            }
                        }

                        1 -> {
                            Text(
                                text = "Ödemek istediğiniz ürünleri seçin:",
                                fontSize = 12.sp,
                                color = Slate500,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 210.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(orderItems, key = { it.id }) { item ->
                                    val unpaidQty = (item.quantity - item.paidQuantity).coerceAtLeast(0)
                                    val selectedQty = selectedItemCounts[item.id] ?: 0
                                    val isFullyPaidItem = unpaidQty == 0

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when {
                                            isFullyPaidItem -> Emerald50.copy(alpha = 0.6f)
                                            selectedQty > 0 -> Amber50
                                            else -> Slate100
                                        },
                                        border = BorderStroke(
                                            1.dp,
                                            when {
                                                isFullyPaidItem -> Emerald100
                                                selectedQty > 0 -> Amber600
                                                else -> Slate200
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = !isFullyPaidItem) {
                                                val current = selectedItemCounts[item.id] ?: 0
                                                selectedItemCounts[item.id] =
                                                    if (current < unpaidQty) current + 1 else 0
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = item.itemName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = if (isFullyPaidItem) Emerald700 else Slate900
                                                    )
                                                    if (item.paidQuantity > 0) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = Emerald100
                                                        ) {
                                                            Text(
                                                                text = "${item.paidQuantity}/${item.quantity} Ödendi",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Emerald700,
                                                                modifier = Modifier.padding(
                                                                    horizontal = 6.dp,
                                                                    vertical = 2.dp
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = if (isFullyPaidItem) {
                                                        "Tamamı ödendi"
                                                    } else {
                                                        "₺${"%.2f".format(item.unitPrice)} x Kalan $unpaidQty adet"
                                                    },
                                                    fontSize = 11.sp,
                                                    color = if (isFullyPaidItem) Emerald600 else Slate500
                                                )
                                            }
                                            if (isFullyPaidItem) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Ödendi",
                                                    tint = Emerald600,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            if (selectedQty > 0) {
                                                                selectedItemCounts[item.id] = selectedQty - 1
                                                            }
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Remove,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = "$selectedQty / $unpaidQty",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.sp,
                                                        color = if (selectedQty > 0) Amber700 else Slate700
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            if (selectedQty < unpaidQty) {
                                                                selectedItemCounts[item.id] = selectedQty + 1
                                                            }
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Kalan tutarı kaç kişiye bölmek istiyorsunuz?",
                                    fontSize = 13.sp,
                                    color = Slate700
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf(2, 3, 4, 5, 6).forEach { count ->
                                        FilterChip(
                                            selected = splitCount == count,
                                            onClick = { splitCount = count },
                                            label = {
                                                Text(
                                                    text = "$count Kişi",
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Amber100,
                                                selectedLabelColor = Amber700
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Kişi Başı: ₺${"%.2f".format(remaining / splitCount)}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Sky700
                                )
                            }
                        }

                        3 -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = customAmountText,
                                    onValueChange = { customAmountText = it },
                                    label = { Text("Tahsil Edilecek Tutar (₺)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(50.0, 100.0, 200.0, 500.0).forEach { quick ->
                                        if (quick <= remaining) {
                                            OutlinedButton(
                                                onClick = {
                                                    customAmountText = "%.0f".format(quick)
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("₺${quick.toInt()}", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Şu An Tahsil Edilecek:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                    Text(
                        text = "₺${"%.2f".format(amountToPay)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900
                    )
                }

                }

                Spacer(modifier = Modifier.height(12.dp))

                val paymentNote = when (selectedTab) {
                    1 -> "Ürün Bazlı Ödeme"
                    2 -> "1/$splitCount Kişi Başı Ödeme"
                    3 -> "Kısmi Tahsilat"
                    else -> "Tam Kapama"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (amountToPay > 0) {
                                val paidMap = if (selectedTab == 1) {
                                    selectedItemCounts.filterValues { it > 0 }.toMap()
                                } else {
                                    emptyMap()
                                }
                                onProcessPayment(amountToPay, "NAKİT", paymentNote, true, paidMap)
                                selectedItemCounts.clear()
                            }
                        },
                        enabled = amountToPay > 0.0,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald600,
                            contentColor = PureWhite,
                            disabledContainerColor = Slate200,
                            disabledContentColor = Slate400
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = PureWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NAKİT AL",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = PureWhite
                        )
                    }

                    Button(
                        onClick = {
                            if (amountToPay > 0) {
                                val paidMap = if (selectedTab == 1) {
                                    selectedItemCounts.filterValues { it > 0 }.toMap()
                                } else {
                                    emptyMap()
                                }
                                onProcessPayment(amountToPay, "KART", paymentNote, true, paidMap)
                                selectedItemCounts.clear()
                            }
                        },
                        enabled = amountToPay > 0.0,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Sky600,
                            contentColor = PureWhite,
                            disabledContainerColor = Slate200,
                            disabledContentColor = Slate400
                        )
                    ) {
                        Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = PureWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "KART / POS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = PureWhite
                        )
                    }
                }
            }
        }
    }
}
