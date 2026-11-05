package com.xubopos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xubopos.data.model.RestaurantTable
import com.xubopos.ui.theme.Amber100
import com.xubopos.ui.theme.Amber50
import com.xubopos.ui.theme.Amber500
import com.xubopos.ui.theme.Amber600
import com.xubopos.ui.theme.Amber700
import com.xubopos.ui.theme.Emerald100
import com.xubopos.ui.theme.Emerald500
import com.xubopos.ui.theme.Emerald700
import com.xubopos.ui.theme.PureWhite
import com.xubopos.ui.theme.Rose100
import com.xubopos.ui.theme.Rose50
import com.xubopos.ui.theme.Rose500
import com.xubopos.ui.theme.Rose700
import com.xubopos.ui.theme.Sky100
import com.xubopos.ui.theme.Sky50
import com.xubopos.ui.theme.Sky500
import com.xubopos.ui.theme.Sky700
import com.xubopos.ui.theme.Slate100
import com.xubopos.ui.theme.Slate200
import com.xubopos.ui.theme.Slate400
import com.xubopos.ui.theme.Slate500
import com.xubopos.ui.theme.Slate600
import com.xubopos.ui.theme.Slate800
import com.xubopos.ui.theme.Slate900

@Composable
fun TableCard(
    table: RestaurantTable,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    lockedBy: String? = null
) {
    val isOccupied = table.isOccupied
    val isPartial = table.isPartiallyPaid
    val isLocked = lockedBy != null

    val containerColor = when {
        isLocked -> Rose50
        isPartial -> Sky50
        isOccupied -> Amber50
        else -> PureWhite
    }
    val borderColor = when {
        isLocked -> Rose500
        isPartial -> Sky500
        isOccupied -> Amber500
        else -> Slate200
    }
    val badgeBg = when {
        isPartial -> Sky100
        isOccupied -> Amber100
        else -> Emerald100
    }
    val badgeText = when {
        isPartial -> Sky700
        isOccupied -> Amber700
        else -> Emerald700
    }
    val statusLabel = when {
        isPartial -> "Kısmi Ödeme"
        isOccupied -> "Dolu"
        else -> "Müsait"
    }
    val dotColor = when {
        isLocked -> Rose500
        isPartial -> Sky500
        isOccupied -> Amber500
        else -> Emerald500
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isLocked || isOccupied) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked || isOccupied) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Text(
                            text = table.zone,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (lockedBy != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Rose100
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Rose700,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "$lockedBy Açık",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Rose700,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeText,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = table.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column {
                HorizontalDivider(color = if (isOccupied) borderColor.copy(alpha = 0.35f) else Slate200)
                Spacer(modifier = Modifier.height(8.dp))

                if (isOccupied) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = if (isPartial) "Kalan Tutar" else "Toplam Hesap",
                                fontSize = 10.sp,
                                color = Slate500
                            )
                            Text(
                                text = "₺${"%.2f".format(table.remainingAmount)}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isPartial) Sky700 else Amber700
                            )
                        }
                        table.occupiedSince?.let { since ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Slate500,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = formatElapsedMinutes(since),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate500
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Masa Durumu",
                                fontSize = 10.sp,
                                color = Slate400
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${table.capacity} Kişilik",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate600
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatElapsedMinutes(sinceMillis: Long): String {
    val diffMinutes = ((System.currentTimeMillis() - sinceMillis) / 60000L).coerceAtLeast(0L)
    return when {
        diffMinutes < 1L -> "Yeni"
        diffMinutes < 60L -> "${diffMinutes} dk"
        else -> {
            val hours = diffMinutes / 60L
            val mins = diffMinutes % 60L
            "${hours} sa ${mins} dk"
        }
    }
}

fun formatOrderTime(sinceMillis: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(sinceMillis))
}

fun formatOrderTimeWithElapsed(sinceMillis: Long): String {
    val time = formatOrderTime(sinceMillis)
    val elapsed = formatElapsedMinutes(sinceMillis)
    return "$time ($elapsed)"
}
