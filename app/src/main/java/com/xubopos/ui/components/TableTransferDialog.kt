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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.xubopos.data.model.RestaurantTable
import com.xubopos.ui.theme.Amber50
import com.xubopos.ui.theme.Amber500
import com.xubopos.ui.theme.Amber700
import com.xubopos.ui.theme.Emerald50
import com.xubopos.ui.theme.Emerald500
import com.xubopos.ui.theme.Emerald700
import com.xubopos.ui.theme.PureWhite
import com.xubopos.ui.theme.Slate500
import com.xubopos.ui.theme.Slate900

@Composable
fun TableTransferDialog(
    currentTable: RestaurantTable,
    allTables: List<RestaurantTable>,
    onDismiss: () -> Unit,
    onSelectTargetTable: (Int) -> Unit
) {
    val targetTables = allTables.filter { it.id != currentTable.id }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
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
                                text = "Masa Taşı / Birleştir",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900
                            )
                            Text(
                                text = "${currentTable.name} adisyonunu aktaracağınız masayı seçin",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(targetTables, key = { it.id }) { table ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectTargetTable(table.id) },
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
                                    text = if (table.isOccupied) {
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
