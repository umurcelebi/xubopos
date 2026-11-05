package com.xubopos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.xubopos.data.model.OrderItem
import com.xubopos.data.model.RestaurantTable
import com.xubopos.ui.theme.Amber50
import com.xubopos.ui.theme.Amber500
import com.xubopos.ui.theme.Amber700
import com.xubopos.ui.theme.Emerald50
import com.xubopos.ui.theme.Emerald500
import com.xubopos.ui.theme.Emerald700
import com.xubopos.ui.theme.PureWhite
import com.xubopos.ui.theme.Slate100
import com.xubopos.ui.theme.Slate200
import com.xubopos.ui.theme.Slate500
import com.xubopos.ui.theme.Slate700
import com.xubopos.ui.theme.Slate900

@Composable
fun TableTransferDialog(
    currentTable: RestaurantTable,
    allTables: List<RestaurantTable>,
    orderItems: List<OrderItem> = emptyList(),
    onDismiss: () -> Unit,
    onSelectTargetTable: (Int) -> Unit,
    onTransferSelectedItems: (Int, Map<Long, Int>) -> Unit = { _, _ -> }
) {
    val targetTables = allTables.filter { it.id != currentTable.id }
    val unpaidItems = remember(orderItems) { orderItems.filter { it.unpaidQuantity > 0 } }
    var selectedTab by remember { mutableIntStateOf(0) }
    val selectedQuantities = remember { mutableStateMapOf<Long, Int>() }
    val totalSelectedQty = selectedQuantities.values.sum()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(24.dp),
            color = PureWhite
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = Amber700
                        )
                        Column {
                            Text(
                                text = "Masa / Ürün Aktar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900
                            )
                            Text(
                                text = "${currentTable.name} adisyonunu veya seçili ürünleri taşıyın",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                if (unpaidItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Slate100,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = "Tüm Masayı Taşı",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = "Seçili Ürünleri Taşı",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                if (selectedTab == 1 && unpaidItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "1. Aktarılacak ürünleri ve adetlerini seçin ($totalSelectedQty adet seçildi):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(unpaidItems, key = { it.id }) { item ->
                            val count = selectedQuantities[item.id] ?: 0
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (count > 0) Amber50 else Slate100,
                                border = BorderStroke(1.dp, if (count > 0) Amber500 else Slate200)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.itemName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900
                                        )
                                        Text(
                                            text = "Mevcut: ${item.unpaidQuantity} adet",
                                            fontSize = 11.sp,
                                            color = Slate500
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (count > 0) selectedQuantities[item.id] = count - 1
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
                                            text = "$count",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = Slate900
                                        )
                                        IconButton(
                                            onClick = {
                                                if (count < item.unpaidQuantity) {
                                                    selectedQuantities[item.id] = count + 1
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "2. Ürünlerin aktarılacağı hedef masaya dokunun:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(targetTables, key = { it.id }) { table ->
                        val isEnabled = selectedTab == 0 || totalSelectedQty > 0
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = isEnabled) {
                                    if (selectedTab == 0) {
                                        onSelectTargetTable(table.id)
                                    } else {
                                        val map = selectedQuantities.filterValues { it > 0 }.toMap()
                                        if (map.isNotEmpty()) {
                                            onTransferSelectedItems(table.id, map)
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (table.isOccupied) Amber50 else Emerald50
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (table.isOccupied) Amber500 else Emerald500
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = table.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = table.zone,
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (selectedTab == 1) {
                                        if (totalSelectedQty > 0) "$totalSelectedQty Ürünü Buraya Aktar" else "Önce Ürün Seçin"
                                    } else if (table.isOccupied) {
                                        "Birleştir (₺${"%.0f".format(table.remainingAmount)})"
                                    } else {
                                        "Boş Masaya Taşı"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (table.isOccupied) Amber700 else Emerald700
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
