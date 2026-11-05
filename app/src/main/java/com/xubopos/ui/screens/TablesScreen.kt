package com.xubopos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xubopos.data.model.RestaurantTable
import com.xubopos.ui.components.DailyReportDialog
import com.xubopos.ui.components.MenuManagementDialog
import com.xubopos.ui.components.TableCard
import com.xubopos.ui.theme.Amber500
import com.xubopos.ui.theme.Amber600
import com.xubopos.ui.theme.Emerald500
import com.xubopos.ui.theme.PureWhite
import com.xubopos.ui.theme.Sky500
import com.xubopos.ui.theme.Sky700
import com.xubopos.ui.theme.Slate100
import com.xubopos.ui.theme.Slate200
import com.xubopos.ui.theme.Slate400
import com.xubopos.ui.theme.Slate50
import com.xubopos.ui.theme.Slate700
import com.xubopos.ui.theme.Slate800
import com.xubopos.ui.theme.Slate900
import com.xubopos.ui.viewmodel.POSViewModel

import com.xubopos.network.DeviceRole

@Composable
fun TablesScreen(
    viewModel: POSViewModel,
    onTableClick: (RestaurantTable) -> Unit,
    onNavigateManagement: () -> Unit = {},
    onNavigateKitchen: () -> Unit = {},
    onOpenRoleSettings: () -> Unit = {}
) {
    val tables by viewModel.allTables.collectAsState()
    val selectedZone by viewModel.selectedZone.collectAsState()
    val report by viewModel.dailyReport.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val menuItems by viewModel.allMenuItems.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val restaurantName by viewModel.restaurantName.collectAsState()
    val deviceRole by viewModel.deviceRole.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val connectedClientCount by viewModel.connectedClientCount.collectAsState()
    val resolvedServerHost by viewModel.resolvedServerHost.collectAsState()
    val activeTickets by viewModel.activeKitchenTickets.collectAsState()
    val activeTableLocks by viewModel.activeTableLocks.collectAsState()
    val myDeviceId = viewModel.deviceId

    var showReportDialog by remember { mutableStateOf(false) }
    var showMenuDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val zones = remember(tables) {
        listOf("Tümü", "Dolu Masalar") + tables.map { it.zone }.distinct()
    }

    val filteredTables = remember(tables, selectedZone) {
        when (selectedZone) {
            "Tümü" -> tables
            "Dolu Masalar" -> tables.filter { it.isOccupied }
            else -> tables.filter { it.zone == selectedZone }
        }
    }

    val occupiedCount = tables.count { it.isOccupied }
    val freeCount = tables.size - occupiedCount
    val activeBillsSum = tables.filter { it.isOccupied }.sumOf { it.remainingAmount }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Slate50
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Surface(
                color = Slate900,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // 1. SATIR: Sol: Restoran Adı (Büyük ve Dinamik Ölçeklenen) | Sağ: Cihaz Modu Rozeti
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(end = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            var titleFontSize by remember(restaurantName, maxWidth) { mutableStateOf(26.sp) }

                            Text(
                                text = restaurantName,
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.ExtraBold,
                                color = PureWhite,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                onTextLayout = { textLayoutResult ->
                                    if (textLayoutResult.hasVisualOverflow && titleFontSize > 13.sp) {
                                        titleFontSize = (titleFontSize.value - 1.5f).sp
                                    }
                                }
                            )
                        }

                        // Cihaz Modu Rozeti (Garson ve Mutfak Modunda Tıklanabilir)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when (deviceRole) {
                                DeviceRole.SERVER -> Amber500
                                DeviceRole.WAITER -> Sky500
                                DeviceRole.KITCHEN -> Emerald500
                            },
                            modifier = if (deviceRole != DeviceRole.SERVER) {
                                Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onOpenRoleSettings() }
                            } else Modifier
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = "Ağ Modu",
                                    tint = Slate900,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = when (deviceRole) {
                                        DeviceRole.SERVER -> "KASA (SUNUCU)"
                                        DeviceRole.WAITER -> "GARSON MODU"
                                        DeviceRole.KITCHEN -> "MUTFAK (MOD DEĞİŞTİR)"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Slate900
                                )
                            }
                        }
                    }

                    // 2. SATIR: Mutfak, Yönetim, Kasa Butonları (Dikey modda ekrana dengeli yayılır)
                    if (deviceRole == DeviceRole.KITCHEN || deviceRole == DeviceRole.SERVER) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (deviceRole == DeviceRole.KITCHEN) {
                                FilledTonalButton(
                                    onClick = onNavigateKitchen,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (activeTickets.isNotEmpty()) Amber600 else Slate800,
                                        contentColor = PureWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = "Mutfak",
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Mutfak" + if (activeTickets.isNotEmpty()) " (${activeTickets.size})" else "",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }

                            if (deviceRole == DeviceRole.SERVER) {
                                FilledTonalButton(
                                    onClick = onNavigateManagement,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Slate800,
                                        contentColor = PureWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Yönetim",
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Yönetim", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }

                                FilledTonalButton(
                                    onClick = { showReportDialog = true },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Slate800,
                                        contentColor = PureWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = "Kasa Raporu",
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kasa", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeaderStatPill(
                            label = "Dolu Masa",
                            value = "$occupiedCount / ${tables.size}",
                            dotColor = Amber500,
                            modifier = Modifier.weight(1f)
                        )
                        HeaderStatPill(
                            label = "Müsait",
                            value = "$freeCount Masa",
                            dotColor = Emerald500,
                            modifier = Modifier.weight(1f)
                        )
                        HeaderStatPill(
                            label = "Açık Hesap",
                            value = "₺${"%.0f".format(activeBillsSum)}",
                            dotColor = Sky500,
                            modifier = Modifier.weight(1.2f)
                        )
                        HeaderStatPill(
                            label = "Günlük Ciro",
                            value = "₺${"%.0f".format(report.totalRevenue)}",
                            dotColor = Emerald500,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhite)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(zones) { zone ->
                    val isSelected = selectedZone == zone
                    val zoneCount = when (zone) {
                        "Tümü" -> tables.size
                        "Dolu Masalar" -> occupiedCount
                        else -> tables.count { it.zone == zone }
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectZone(zone) },
                        label = {
                            Text(
                                text = "$zone ($zoneCount)",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate900,
                            selectedLabelColor = PureWhite,
                            containerColor = Slate100,
                            labelColor = Slate700
                        ),
                        border = null
                    )
                }
            }

            if (filteredTables.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.TableRestaurant,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Bu filtrede masa bulunmuyor",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTables, key = { it.id }) { table ->
                        val lockEntry = activeTableLocks[table.id]
                        val lockedByOther = if (lockEntry != null && lockEntry.deviceId != myDeviceId) lockEntry.deviceName else null
                        TableCard(
                            table = table,
                            lockedBy = lockedByOther,
                            onClick = { onTableClick(table) }
                        )
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        DailyReportDialog(
            report = report,
            onDismiss = { showReportDialog = false }
        )
    }

    if (showMenuDialog) {
        MenuManagementDialog(
            categories = categories,
            menuItems = menuItems,
            onDismiss = { showMenuDialog = false },
            onAddItem = { catId, name, price, desc ->
                viewModel.addCustomProduct(catId, name, price, desc)
            },
            onDeleteItem = { item ->
                viewModel.deleteProduct(item)
            }
        )
    }
}

@Composable
private fun HeaderStatPill(
    label: String,
    value: String,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Slate800
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Slate400,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PureWhite,
                maxLines = 1
            )
        }
    }
}
