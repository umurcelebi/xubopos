package com.xubopos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xubopos.data.model.DailyReport
import com.xubopos.ui.theme.Amber50
import com.xubopos.ui.theme.Amber700
import com.xubopos.ui.theme.Emerald50
import com.xubopos.ui.theme.Emerald600
import com.xubopos.ui.theme.Emerald700
import com.xubopos.ui.theme.PureWhite
import com.xubopos.ui.theme.Sky50
import com.xubopos.ui.theme.Sky700
import com.xubopos.ui.theme.Slate100
import com.xubopos.ui.theme.Slate200
import com.xubopos.ui.theme.Slate500
import com.xubopos.ui.theme.Slate800
import com.xubopos.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyReportDialog(
    report: DailyReport,
    onDismiss: () -> Unit
) {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(24.dp),
            color = PureWhite
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
                            text = "Gün Sonu & Kasa Özeti",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900
                        )
                        Text(
                            text = "Anlık tahsilat ve satış istatistikleri",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMiniCard(
                        title = "Toplam Ciro",
                        value = "₺${"%.2f".format(report.totalRevenue)}",
                        subtitle = "${report.closedOrderCount} Kapalı Adisyon",
                        icon = Icons.Default.TrendingUp,
                        bgColor = Emerald50,
                        textColor = Emerald700,
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        title = "Açık Masalar",
                        value = "₺${"%.2f".format(report.openTablesTotal)}",
                        subtitle = "${report.openOrderCount} Aktif Masa",
                        icon = Icons.Default.AccountBalanceWallet,
                        bgColor = Amber50,
                        textColor = Amber700,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMiniCard(
                        title = "Nakit Tahsilat",
                        value = "₺${"%.2f".format(report.cashTotal)}",
                        subtitle = "Kasa Nakit",
                        icon = Icons.Default.Payments,
                        bgColor = Slate100,
                        textColor = Slate800,
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        title = "Kart / POS",
                        value = "₺${"%.2f".format(report.cardTotal)}",
                        subtitle = "Banka POS",
                        icon = Icons.Default.CreditCard,
                        bgColor = Sky50,
                        textColor = Sky700,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (report.topItems.isNotEmpty()) {
                        item {
                            Text(
                                text = "En Çok Satan Ürünler",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                        }
                        items(report.topItems) { top ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Slate100
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = top.itemName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Slate900
                                        )
                                        Text(
                                            text = "${top.totalQty} adet satıldı",
                                            fontSize = 11.sp,
                                            color = Slate500
                                        )
                                    }
                                    Text(
                                        text = "₺${"%.2f".format(top.totalSales)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Slate900
                                    )
                                }
                            }
                        }
                    }

                    if (report.recentPayments.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Son Alınan Ödemeler",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                        }
                        items(report.recentPayments) { payment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${payment.tableName} • ${payment.paymentMethod}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Slate900
                                    )
                                    Text(
                                        text = timeFormatter.format(Date(payment.paidAt)) +
                                            if (payment.note.isNotBlank()) " (${payment.note})" else "",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                                Text(
                                    text = "+₺${"%.2f".format(payment.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Emerald600
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMiniCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.75f)
            )
        }
    }
}
