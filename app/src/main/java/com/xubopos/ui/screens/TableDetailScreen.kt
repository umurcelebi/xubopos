package com.xubopos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import com.xubopos.data.model.KitchenTicketEntity
import com.xubopos.ui.components.formatElapsedMinutes
import com.xubopos.ui.components.formatOrderTimeWithElapsed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xubopos.data.model.MenuCategory
import com.xubopos.data.model.MenuItem
import com.xubopos.data.model.OrderBill
import com.xubopos.data.model.OrderItem
import com.xubopos.data.model.PaymentRecord
import com.xubopos.network.DeviceRole
import com.xubopos.ui.components.PaymentDialog
import com.xubopos.ui.components.TableTransferDialog
import com.xubopos.ui.theme.Amber100
import com.xubopos.ui.theme.Amber50
import com.xubopos.ui.theme.Amber500
import com.xubopos.ui.theme.Amber600
import com.xubopos.ui.theme.Amber700
import com.xubopos.ui.theme.Emerald100
import com.xubopos.ui.theme.Emerald50
import com.xubopos.ui.theme.Emerald500
import com.xubopos.ui.theme.Emerald600
import com.xubopos.ui.theme.Emerald700
import com.xubopos.ui.theme.PureWhite
import com.xubopos.ui.theme.Rose100
import com.xubopos.ui.theme.Rose50
import com.xubopos.ui.theme.Rose600
import com.xubopos.ui.theme.Sky100
import com.xubopos.ui.theme.Sky50
import com.xubopos.ui.theme.Sky600
import com.xubopos.ui.theme.Sky700
import com.xubopos.ui.theme.Slate100
import com.xubopos.ui.theme.Slate200
import com.xubopos.ui.theme.Slate300
import com.xubopos.ui.theme.Slate400
import com.xubopos.ui.theme.Slate50
import com.xubopos.ui.theme.Slate500
import com.xubopos.ui.theme.Slate600
import com.xubopos.ui.theme.Slate700
import com.xubopos.ui.theme.Slate800
import com.xubopos.ui.theme.Slate900
import com.xubopos.ui.viewmodel.POSViewModel

@Composable
fun TableDetailScreen(
    viewModel: POSViewModel,
    onBack: () -> Unit
) {
    val table by viewModel.selectedTable.collectAsState()
    val bill by viewModel.currentBill.collectAsState()
    val orderItems by viewModel.currentOrderItems.collectAsState()
    val payments by viewModel.currentPayments.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val menuItems by viewModel.filteredMenuItems.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val searchQuery by viewModel.menuSearchQuery.collectAsState()
    val allTables by viewModel.allTables.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val deviceRole by viewModel.deviceRole.collectAsState()
    val activeKitchenTickets by viewModel.activeKitchenTickets.collectAsState()

    var mobileTab by remember { mutableIntStateOf(0) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }
    var editingNoteItem by remember { mutableStateOf<OrderItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val currentTable = table ?: return
    val totalItemCount = orderItems.sumOf { it.quantity }
    val remainingAmount = bill?.remainingAmount ?: 0.0

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Slate50,
        topBar = {
            Surface(
                color = Slate900,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = PureWhite
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = currentTable.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PureWhite
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Slate800
                                ) {
                                    Text(
                                        text = currentTable.zone,
                                        fontSize = 11.sp,
                                        color = Amber500,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (currentTable.isOccupied) {
                                    "$totalItemCount ürün • Kalan: ₺${"%.2f".format(remainingAmount)}"
                                } else {
                                    "Müsait Masa • Sipariş eklemek için menüden ürün seçin"
                                },
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    }

                    if (currentTable.isOccupied) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { showTransferDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Masa Taşı / Birleştir",
                                    tint = Amber500
                                )
                            }
                            IconButton(onClick = { showCancelConfirm = true }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Adisyonu İptal Et",
                                    tint = Rose600
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 700.dp
            if (isWideScreen) {
                Row(modifier = Modifier.fillMaxSize()) {
                    MenuCatalogSection(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        searchQuery = searchQuery,
                        menuItems = menuItems,
                        orderItems = orderItems,
                        onSelectCategory = viewModel::selectCategory,
                        onSearchChange = viewModel::updateSearchQuery,
                        onAddMenuItem = { viewModel.addItemToCurrentTable(it) },
                        modifier = Modifier
                            .weight(1.35f)
                            .fillMaxHeight()
                    )
                    BillTicketSection(
                        bill = bill,
                        orderItems = orderItems,
                        payments = payments,
                        kitchenTickets = activeKitchenTickets.filter { it.tableId == currentTable.id },
                        isServer = deviceRole == DeviceRole.SERVER,
                        onIncrement = viewModel::incrementItem,
                        onDecrement = viewModel::decrementItem,
                        onEditNote = { editingNoteItem = it },
                        onOpenPayment = { showPaymentDialog = true },
                        onSendKitchen = { viewModel.sendOrderToKitchen(currentTable.id) },
                        onQuickPayAll = { method ->
                            if (remainingAmount > 0) {
                                viewModel.makePayment(
                                    amount = remainingAmount,
                                    method = method,
                                    note = "Hızlı Kapama",
                                    closeTableIfPaid = true,
                                    onTableClosed = onBack
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                        selectedTabIndex = mobileTab,
                        containerColor = PureWhite,
                        contentColor = Slate900
                    ) {
                        Tab(
                            selected = mobileTab == 0,
                            onClick = { mobileTab = 0 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestaurantMenu,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Menü & Ürün Ekle",
                                        fontWeight = if (mobileTab == 0) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = mobileTab == 1,
                            onClick = { mobileTab = 1 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (totalItemCount > 0) {
                                                Badge(
                                                    containerColor = Amber600,
                                                    contentColor = PureWhite
                                                ) {
                                                    Text("$totalItemCount")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Adisyon (₺${"%.0f".format(remainingAmount)})",
                                        fontWeight = if (mobileTab == 1) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                }
                            }
                        )
                    }

                    if (mobileTab == 0) {
                        Column(modifier = Modifier.weight(1f)) {
                            MenuCatalogSection(
                                categories = categories,
                                selectedCategoryId = selectedCategoryId,
                                searchQuery = searchQuery,
                                menuItems = menuItems,
                                orderItems = orderItems,
                                onSelectCategory = viewModel::selectCategory,
                                onSearchChange = viewModel::updateSearchQuery,
                                onAddMenuItem = { viewModel.addItemToCurrentTable(it) },
                                modifier = Modifier.weight(1f)
                            )

                            if (orderItems.isNotEmpty()) {
                                Surface(
                                    color = Slate900,
                                    shadowElevation = 12.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { mobileTab = 1 }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .navigationBarsPadding()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Amber500
                                            ) {
                                                Text(
                                                    text = "$totalItemCount Ürün",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp,
                                                    color = Slate900,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = "Kalan Hesap",
                                                    fontSize = 11.sp,
                                                    color = Slate400
                                                )
                                                Text(
                                                    text = "₺${"%.2f".format(remainingAmount)}",
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = PureWhite
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { showPaymentDialog = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Ödeme Al", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        BillTicketSection(
                            bill = bill,
                            orderItems = orderItems,
                            payments = payments,
                            kitchenTickets = activeKitchenTickets.filter { it.tableId == currentTable.id },
                            isServer = deviceRole == DeviceRole.SERVER,
                            onIncrement = viewModel::incrementItem,
                            onDecrement = viewModel::decrementItem,
                            onEditNote = { editingNoteItem = it },
                            onOpenPayment = { showPaymentDialog = true },
                            onSendKitchen = { viewModel.sendOrderToKitchen(currentTable.id) },
                            onQuickPayAll = { method ->
                                if (remainingAmount > 0) {
                                    viewModel.makePayment(
                                        amount = remainingAmount,
                                        method = method,
                                        note = "Hızlı Kapama",
                                        closeTableIfPaid = true,
                                        onTableClosed = onBack
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    val activeBill = bill
    if (showPaymentDialog && activeBill != null) {
        PaymentDialog(
            tableName = currentTable.name,
            bill = activeBill,
            orderItems = orderItems,
            payments = payments,
            onDismiss = { showPaymentDialog = false },
            onApplyDiscount = { disc -> viewModel.applyDiscount(disc) },
            onProcessPayment = { amount, method, note, closeIfPaid, paidMap ->
                viewModel.makePayment(
                    amount = amount,
                    method = method,
                    note = note,
                    closeTableIfPaid = closeIfPaid,
                    paidItemQuantities = paidMap,
                    onTableClosed = {
                        showPaymentDialog = false
                        onBack()
                    }
                )
            }
        )
    }

    if (showTransferDialog) {
        TableTransferDialog(
            currentTable = currentTable,
            allTables = allTables,
            onDismiss = { showTransferDialog = false },
            onSelectTargetTable = { targetId ->
                viewModel.transferCurrentTable(targetId) {
                    showTransferDialog = false
                }
            }
        )
    }

    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text("Adisyonu İptal Et", fontWeight = FontWeight.Bold) },
            text = {
                Text("${currentTable.name} masasındaki tüm siparişleri iptal edip masayı boşaltmak istediğinize emin misiniz?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirm = false
                        viewModel.cancelTableOrder { onBack() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                ) {
                    Text("Evet, İptal Et")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    editingNoteItem?.let { item ->
        var noteText by remember(item) { mutableStateOf(item.note) }
        val quickNotes = listOf("Az Pişmiş", "İyi Pişmiş", "Soğansız", "Acısız", "Bol Soslu", "Paket")

        AlertDialog(
            onDismissRequest = { editingNoteItem = null },
            title = {
                Text(
                    text = "${item.itemName} • Sipariş Notu",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Mutfak / Servis Notu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(quickNotes) { qNote ->
                            FilterChip(
                                selected = noteText.contains(qNote),
                                onClick = {
                                    noteText = if (noteText.isBlank()) qNote else "$noteText, $qNote"
                                },
                                label = { Text(qNote, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateItemNote(item, noteText.trim())
                        editingNoteItem = null
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNoteItem = null }) {
                    Text("Kapat")
                }
            }
        )
    }
}

@Composable
private fun MenuCatalogSection(
    categories: List<MenuCategory>,
    selectedCategoryId: Int,
    searchQuery: String,
    menuItems: List<MenuItem>,
    orderItems: List<OrderItem>,
    onSelectCategory: (Int) -> Unit,
    onSearchChange: (String) -> Unit,
    onAddMenuItem: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemCountMap = remember(orderItems) {
        orderItems.groupBy { it.menuItemId }.mapValues { entry ->
            entry.value.sumOf { it.quantity }
        }
    }

    Column(
        modifier = modifier
            .background(Slate50)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Ürün veya açıklama ara...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate500)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Temizle")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PureWhite,
                unfocusedContainerColor = PureWhite,
                focusedBorderColor = Amber600,
                unfocusedBorderColor = Slate200
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == 0,
                    onClick = { onSelectCategory(0) },
                    label = {
                        Text(
                            text = "Tümü",
                            fontWeight = if (selectedCategoryId == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Slate900,
                        selectedLabelColor = PureWhite,
                        containerColor = PureWhite
                    )
                )
            }
            items(categories, key = { it.id }) { category ->
                val isSelected = selectedCategoryId == category.id
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategory(category.id) },
                    label = {
                        Text(
                            text = category.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Slate900,
                        selectedLabelColor = PureWhite,
                        containerColor = PureWhite
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(menuItems, key = { it.id }) { product ->
                val inBillCount = itemCountMap[product.id] ?: 0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onAddMenuItem(product) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (inBillCount > 0) Amber50 else PureWhite
                    ),
                    border = BorderStroke(
                        if (inBillCount > 0) 1.5.dp else 1.dp,
                        if (inBillCount > 0) Amber500 else Slate200
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (inBillCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = Amber600,
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Text(
                                        text = "${inBillCount}x",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PureWhite,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (product.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = product.description,
                                fontSize = 11.sp,
                                color = Slate500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₺${"%.2f".format(product.price)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Amber700
                            )
                            Surface(
                                shape = CircleShape,
                                color = Slate100,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Ekle",
                                        tint = Slate800,
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
}

@Composable
private fun BillTicketSection(
    bill: OrderBill?,
    orderItems: List<OrderItem>,
    payments: List<PaymentRecord>,
    kitchenTickets: List<KitchenTicketEntity> = emptyList(),
    isServer: Boolean = true,
    onIncrement: (OrderItem) -> Unit,
    onDecrement: (OrderItem) -> Unit,
    onEditNote: (OrderItem) -> Unit,
    onOpenPayment: () -> Unit,
    onQuickPayAll: (String) -> Unit,
    onSendKitchen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentTime by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            delay(10000L)
            value = System.currentTimeMillis()
        }
    }

    Surface(
        modifier = modifier,
        color = PureWhite,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
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
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = Slate800
                    )
                    Text(
                        text = "Masa Adisyonu",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900
                    )
                }
                if (bill != null) {
                    Text(
                        text = "#${bill.id}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate400
                    )
                }
            }

            // Mutfak Takip Durumu Özeti
            if (kitchenTickets.isNotEmpty()) {
                val latestTicket = kitchenTickets.first()
                val ticketStatusLabel = when (latestTicket.status) {
                    "READY" -> "Mutfakta Hazır! (Servis Bekliyor)"
                    "PREPARING" -> "Mutfakta Hazırlanıyor..."
                    else -> "Mutfak Sırasında Bekliyor"
                }
                val ticketBgColor = when (latestTicket.status) {
                    "READY" -> Emerald50
                    "PREPARING" -> Sky50
                    else -> Amber50
                }
                val ticketBorderColor = when (latestTicket.status) {
                    "READY" -> Emerald500
                    "PREPARING" -> Sky600
                    else -> Amber500
                }
                val ticketTextColor = when (latestTicket.status) {
                    "READY" -> Emerald700
                    "PREPARING" -> Sky700
                    else -> Amber700
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ticketBgColor,
                    border = BorderStroke(1.dp, ticketBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = ticketTextColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = ticketStatusLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ticketTextColor
                            )
                        }
                        Text(
                            text = formatElapsedMinutes(latestTicket.createdAt) + " önce",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate200)

            if (orderItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = Slate300,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Adisyon henüz boş",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate500
                        )
                        Text(
                            text = "Menüden ürünlere dokunarak sipariş ekleyin",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(orderItems, key = { it.id }) { item ->
                        val isFullyPaid = item.paidQuantity >= item.quantity && item.quantity > 0
                        val isPartiallyPaid = item.paidQuantity in 1 until item.quantity

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isFullyPaid -> Emerald50
                                isPartiallyPaid -> Sky50
                                else -> Slate50
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isFullyPaid -> Emerald500.copy(alpha = 0.5f)
                                    isPartiallyPaid -> Sky600.copy(alpha = 0.4f)
                                    else -> Slate200
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
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
                                                fontSize = 14.sp,
                                                color = if (isFullyPaid) Emerald700 else Slate900
                                            )
                                            if (isFullyPaid) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Emerald100
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            tint = Emerald700,
                                                            modifier = Modifier.size(11.dp)
                                                        )
                                                        Text(
                                                            text = "Ödendi",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Emerald700
                                                        )
                                                    }
                                                }
                                            } else if (isPartiallyPaid) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Sky100
                                                ) {
                                                    Text(
                                                        text = "${item.paidQuantity}/${item.quantity} Ödendi",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Sky700,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "₺${"%.2f".format(item.unitPrice)} x ${item.quantity} = ₺${"%.2f".format(item.subtotal)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isFullyPaid) Emerald600 else Amber700
                                        )

                                        val itemElapsedMinutes = ((currentTime - item.addedAt) / 60000L).coerceAtLeast(0L)
                                        val isItemDelayed = itemElapsedMinutes >= 15L
                                        val isItemWarning = itemElapsedMinutes in 8L..14L
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 3.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(5.dp),
                                                color = when {
                                                    isItemDelayed -> Rose50
                                                    isItemWarning -> Amber100
                                                    else -> Slate100
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AccessTime,
                                                        contentDescription = null,
                                                        tint = when {
                                                            isItemDelayed -> Rose600
                                                            isItemWarning -> Amber700
                                                            else -> Slate500
                                                        },
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Text(
                                                        text = formatOrderTimeWithElapsed(item.addedAt),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when {
                                                            isItemDelayed -> Rose600
                                                            isItemWarning -> Amber700
                                                            else -> Slate700
                                                        }
                                                    )
                                                    if (isItemDelayed) {
                                                        Text(
                                                            text = "• Gecikiyor",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Rose600
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onEditNote(item) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (item.note.isBlank()) {
                                                    Icons.AutoMirrored.Filled.NoteAdd
                                                } else {
                                                    Icons.Default.EditNote
                                                },
                                                contentDescription = "Not",
                                                tint = if (item.note.isBlank()) Slate400 else Sky600,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        val canDecrement = item.quantity > item.paidQuantity
                                        Surface(
                                            shape = CircleShape,
                                            color = if (!canDecrement) {
                                                Slate100
                                            } else if (item.quantity == 1) {
                                                Rose50
                                            } else {
                                                Slate200
                                            },
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .clickable(enabled = canDecrement) { onDecrement(item) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Azalt",
                                                    tint = if (!canDecrement) {
                                                        Slate300
                                                    } else if (item.quantity == 1) {
                                                        Rose600
                                                    } else {
                                                        Slate800
                                                    },
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "${item.quantity}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = Slate900,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )

                                        Surface(
                                            shape = CircleShape,
                                            color = Amber100,
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .clickable { onIncrement(item) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Artır",
                                                    tint = Amber700,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (item.note.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Not: ${item.note}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Sky700
                                    )
                                }
                            }
                        }
                    }

                    if (payments.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Sky50,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Alınan Ara Ödemeler",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Sky700
                                    )
                                    payments.forEach { p ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${p.paymentMethod} (${p.note})",
                                                fontSize = 11.sp,
                                                color = Slate700
                                            )
                                            Text(
                                                text = "-₺${"%.2f".format(p.amount)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald700
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (bill != null && orderItems.isNotEmpty()) {
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ara Toplam", fontSize = 12.sp, color = Slate500)
                        Text(
                            text = "₺${"%.2f".format(bill.totalAmount)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate800
                        )
                    }
                    if (bill.discountAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("İndirim", fontSize = 12.sp, color = Rose600)
                            Text(
                                text = "-₺${"%.2f".format(bill.discountAmount)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Rose600
                            )
                        }
                    }
                    if (bill.paidAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Alınan Ara Ödeme", fontSize = 12.sp, color = Emerald700)
                            Text(
                                text = "-₺${"%.2f".format(bill.paidAmount)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald700
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KALAN TUTAR",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900
                        )
                        Text(
                            text = "₺${"%.2f".format(bill.remainingAmount)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Amber700
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onSendKitchen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber600)
                ) {
                    Icon(imageVector = Icons.Default.Restaurant, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MUTFAĞA GÖNDER (KDS)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }

                if (isServer) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onOpenPayment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ÖDEME AL / HESAP BÖL",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onQuickPayAll("NAKİT") },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Emerald600)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hızlı Nakit",
                                fontWeight = FontWeight.Bold,
                                color = Emerald700,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { onQuickPayAll("KART") },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Sky600)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Sky600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hızlı Kart",
                                fontWeight = FontWeight.Bold,
                                color = Sky700,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
