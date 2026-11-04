package com.xubopos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xubopos.BuildConfig
import com.xubopos.data.model.DetailedSalesReport
import com.xubopos.data.model.MenuCategory
import com.xubopos.data.model.MenuItem
import com.xubopos.data.model.ReportPeriod
import com.xubopos.data.model.RestaurantTable
import com.xubopos.update.UpdateStatus
import java.io.File
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
import com.xubopos.ui.theme.Rose50
import com.xubopos.ui.theme.Rose500
import com.xubopos.ui.theme.Rose600
import com.xubopos.ui.theme.Sky100
import com.xubopos.ui.theme.Sky50
import com.xubopos.ui.theme.Sky600
import com.xubopos.ui.theme.Sky700
import com.xubopos.ui.theme.Slate100
import com.xubopos.ui.theme.Slate200
import com.xubopos.ui.theme.Slate400
import com.xubopos.ui.theme.Slate50
import com.xubopos.ui.theme.Slate500
import com.xubopos.ui.theme.Slate700
import com.xubopos.ui.theme.Slate800
import com.xubopos.ui.theme.Slate900
import com.xubopos.ui.viewmodel.POSViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ManagementScreen(
    viewModel: POSViewModel,
    onBack: () -> Unit,
    onOpenRoleSettings: () -> Unit = {}
) {
    val tables by viewModel.allTables.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val menuItems by viewModel.allMenuItems.collectAsState()
    val selectedPeriod by viewModel.selectedReportPeriod.collectAsState()
    val detailedReport by viewModel.detailedReport.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val restaurantName by viewModel.restaurantName.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Slate50,
        topBar = {
            Surface(
                color = Slate900,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
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
                                Text(
                                    text = "Yönetim & Raporlama Merkezi",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PureWhite
                                )
                                Text(
                                    text = "Menü, masa düzeni ve detaylı ciro analizleri",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                            }
                        }
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Slate900,
                        contentColor = Amber500
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestaurantMenu,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Menü",
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedTab == 0) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TableRestaurant,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Masalar",
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedTab == 1) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = {
                                selectedTab = 2
                                viewModel.refreshDetailedReport()
                            },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Raporlar",
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedTab == 2) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "İşletme",
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedTab == 3) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> MenuManagementTab(
                    categories = categories,
                    menuItems = menuItems,
                    onAddCategory = { name -> viewModel.addCategory(name) },
                    onUpdateCategory = viewModel::updateCategory,
                    onDeleteCategory = viewModel::deleteCategory,
                    onAddProduct = viewModel::addCustomProduct,
                    onUpdateProduct = viewModel::updateProduct,
                    onDeleteProduct = viewModel::deleteProduct
                )

                1 -> TableManagementTab(
                    tables = tables,
                    onAddTable = viewModel::addTable,
                    onUpdateTable = viewModel::updateTable,
                    onDeleteTable = viewModel::deleteTable
                )

                2 -> SalesReportTab(
                    selectedPeriod = selectedPeriod,
                    report = detailedReport,
                    onSelectPeriod = viewModel::selectReportPeriod
                )

                3 -> RestaurantSettingsTab(
                    currentName = restaurantName,
                    onSave = { name ->
                        viewModel.updateRestaurantInfo(name)
                    },
                    onOpenRoleSettings = onOpenRoleSettings,
                    updateStatus = updateStatus,
                    onCheckForUpdates = { viewModel.checkForUpdates(BuildConfig.VERSION_NAME) },
                    onDownloadAndInstall = { url -> viewModel.downloadAndInstallUpdate(context, url) },
                    onInstallDownloadedApk = { file -> viewModel.installDownloadedApk(context, file) }
                )
            }
        }
    }
}

@Composable
private fun MenuManagementTab(
    categories: List<MenuCategory>,
    menuItems: List<MenuItem>,
    onAddCategory: (String) -> Unit,
    onUpdateCategory: (MenuCategory) -> Unit,
    onDeleteCategory: (MenuCategory) -> Unit,
    onAddProduct: (Int, String, Double, String) -> Unit,
    onUpdateProduct: (MenuItem) -> Unit,
    onDeleteProduct: (MenuItem) -> Unit
) {
    var selectedCategoryId by remember(categories) {
        mutableIntStateOf(categories.firstOrNull()?.id ?: 0)
    }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<MenuCategory?>(null) }
    var showProductModal by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<MenuItem?>(null) }

    val filteredProducts = remember(menuItems, selectedCategoryId) {
        if (selectedCategoryId == 0) menuItems else menuItems.filter { it.categoryId == selectedCategoryId }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = Amber600
                            )
                            Column {
                                Text(
                                    text = "Menü Kategorileri (${categories.size})",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = "Kategoriye dokunarak filtreleyin, düzenlemek için kaleme basın",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }
                        FilledActionButton(
                            text = "Kategori Ekle",
                            onClick = { showAddCategoryDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCategoryId == 0,
                                onClick = { selectedCategoryId = 0 },
                                label = {
                                    Text(
                                        text = "Tümü (${menuItems.size})",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Slate900,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                        items(categories, key = { it.id }) { cat ->
                            val count = menuItems.count { it.categoryId == cat.id }
                            val isSelected = selectedCategoryId == cat.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryId = cat.id },
                                label = {
                                    Text(
                                        text = "${cat.name} ($count)",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { editingCategory = cat },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Düzenle",
                                            modifier = Modifier.size(13.dp),
                                            tint = if (isSelected) Amber500 else Slate500
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Slate900,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ürün Listesi (${filteredProducts.size} Ürün)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900
                )
                Button(
                    onClick = {
                        editingProduct = null
                        showProductModal = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Yeni Ürün Ekle", fontWeight = FontWeight.Bold)
                }
            }
        }

        items(filteredProducts, key = { it.id }) { product ->
            val categoryName = categories.find { it.id == product.categoryId }?.name ?: "Genel"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (product.isAvailable) PureWhite else Slate100
                ),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Slate900
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Amber50
                            ) {
                                Text(
                                    text = categoryName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Amber700,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (product.description.isNotBlank()) {
                            Text(
                                text = product.description,
                                fontSize = 12.sp,
                                color = Slate500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₺${"%.2f".format(product.price)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Emerald700
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Switch(
                            checked = product.isAvailable,
                            onCheckedChange = { checked ->
                                onUpdateProduct(product.copy(isAvailable = checked))
                            }
                        )
                        IconButton(
                            onClick = {
                                editingProduct = product
                                showProductModal = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Düzenle",
                                tint = Sky600
                            )
                        }
                        IconButton(onClick = { onDeleteProduct(product) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = Rose500
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Yeni Kategori Ekle", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Kategori Adı (Örn: Kahvaltılar)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddCategory(name.trim())
                            showAddCategoryDialog = false
                        }
                    }
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    editingCategory?.let { cat ->
        var name by remember(cat) { mutableStateOf(cat.name) }
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Kategoriyi Düzenle", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Kategori Adı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = {
                            onDeleteCategory(cat)
                            if (selectedCategoryId == cat.id) selectedCategoryId = 0
                            editingCategory = null
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose600),
                        border = BorderStroke(1.dp, Rose600),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kategoriyi ve Ürünlerini Sil")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onUpdateCategory(cat.copy(name = name.trim()))
                            editingCategory = null
                        }
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) {
                    Text("Kapat")
                }
            }
        )
    }

    if (showProductModal) {
        val initialCatId = editingProduct?.categoryId
            ?: if (selectedCategoryId != 0) selectedCategoryId
            else (categories.firstOrNull()?.id ?: 1)

        var catId by remember { mutableIntStateOf(initialCatId) }
        var name by remember { mutableStateOf(editingProduct?.name ?: "") }
        var priceText by remember {
            mutableStateOf(
                editingProduct?.price?.let { "%.2f".format(it).replace(",", ".") } ?: ""
            )
        }
        var desc by remember { mutableStateOf(editingProduct?.description ?: "") }

        AlertDialog(
            onDismissRequest = { showProductModal = false },
            title = {
                Text(
                    text = if (editingProduct == null) "Yeni Ürün Ekle" else "Ürünü Düzenle",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Kategori Seçin:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories, key = { it.id }) { c ->
                            FilterChip(
                                selected = catId == c.id,
                                onClick = { catId = c.id },
                                label = { Text(c.name, fontSize = 12.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ürün Adı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Birim Fiyatı (₺)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Açıklama / İçindekiler (Opsiyonel)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = priceText.replace(",", ".").toDoubleOrNull()
                        if (name.isNotBlank() && price != null && price > 0) {
                            val current = editingProduct
                            if (current == null) {
                                onAddProduct(catId, name.trim(), price, desc.trim())
                            } else {
                                onUpdateProduct(
                                    current.copy(
                                        categoryId = catId,
                                        name = name.trim(),
                                        price = price,
                                        description = desc.trim()
                                    )
                                )
                            }
                            showProductModal = false
                        }
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProductModal = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
private fun TableManagementTab(
    tables: List<RestaurantTable>,
    onAddTable: (String, String, Int) -> Unit,
    onUpdateTable: (RestaurantTable, String, String, Int) -> Unit,
    onDeleteTable: (RestaurantTable) -> Unit
) {
    val zones = remember(tables) {
        listOf("Tümü") + tables.map { it.zone }.distinct()
    }
    var selectedZone by remember { mutableStateOf("Tümü") }
    var showTableModal by remember { mutableStateOf(false) }
    var editingTable by remember { mutableStateOf<RestaurantTable?>(null) }

    val filteredTables = remember(tables, selectedZone) {
        if (selectedZone == "Tümü") tables else tables.filter { it.zone == selectedZone }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Masa & Salon Düzeni",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900
                    )
                    Text(
                        text = "Yeni masa veya bölge ekleyin, kapasiteleri güncelleyin",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
                Button(
                    onClick = {
                        editingTable = null
                        showTableModal = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Yeni Masa", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(zones) { z ->
                    FilterChip(
                        selected = selectedZone == z,
                        onClick = { selectedZone = z },
                        label = { Text(z, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate900,
                            selectedLabelColor = PureWhite
                        )
                    )
                }
            }
        }

        items(filteredTables, key = { it.id }) { table ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(
                    1.dp,
                    if (table.isOccupied) Amber500 else Slate200
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (table.isOccupied) Amber100 else Emerald100,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TableRestaurant,
                                    contentDescription = null,
                                    tint = if (table.isOccupied) Amber700 else Emerald700
                                )
                            }
                        }
                        Column {
                            Text(
                                text = table.name,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Slate900
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = table.zone,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Sky700
                                )
                                Text(text = "•", color = Slate400)
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Slate500
                                )
                                Text(
                                    text = "${table.capacity} Kişilik",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                editingTable = table
                                showTableModal = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Düzenle",
                                tint = Sky600
                            )
                        }
                        IconButton(
                            onClick = { onDeleteTable(table) },
                            enabled = !table.isOccupied
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = if (table.isOccupied) Slate200 else Rose500
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTableModal) {
        val existingZones = tables.map { it.zone }.distinct()
            .ifEmpty { listOf("Ana Salon", "Bahçe", "Teras", "VIP Loca") }
        var name by remember { mutableStateOf(editingTable?.name ?: "") }
        var zone by remember {
            mutableStateOf(
                editingTable?.zone
                    ?: if (selectedZone != "Tümü") selectedZone
                    else existingZones.first()
            )
        }
        var capacityText by remember {
            mutableStateOf((editingTable?.capacity ?: 4).toString())
        }

        AlertDialog(
            onDismissRequest = { showTableModal = false },
            title = {
                Text(
                    text = if (editingTable == null) "Yeni Masa Ekle" else "Masayı Düzenle",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Masa Adı (Örn: Bahçe 6)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = zone,
                        onValueChange = { zone = it },
                        label = { Text("Bölge / Salon Adı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(existingZones) { z ->
                            FilterChip(
                                selected = zone == z,
                                onClick = { zone = z },
                                label = { Text(z, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = capacityText,
                        onValueChange = { capacityText = it },
                        label = { Text("Kişi Kapasitesi") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cap = capacityText.toIntOrNull() ?: 4
                        if (name.isNotBlank() && zone.isNotBlank()) {
                            val current = editingTable
                            if (current == null) {
                                onAddTable(name.trim(), zone.trim(), cap)
                            } else {
                                onUpdateTable(current, name.trim(), zone.trim(), cap)
                            }
                            showTableModal = false
                        }
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTableModal = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
private fun SalesReportTab(
    selectedPeriod: ReportPeriod,
    report: DetailedSalesReport,
    onSelectPeriod: (ReportPeriod) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ReportPeriod.entries) { period ->
                    val isSelected = selectedPeriod == period
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectPeriod(period) },
                        label = {
                            Text(
                                text = period.label,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate900,
                            selectedLabelColor = PureWhite,
                            containerColor = PureWhite
                        )
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReportKpiCard(
                    title = "Toplam Ciro (${selectedPeriod.label})",
                    value = "₺${"%.2f".format(report.totalRevenue)}",
                    subtitle = "${report.totalTransactions} Tahsilat İşlemi",
                    icon = Icons.Default.TrendingUp,
                    bgColor = Emerald50,
                    textColor = Emerald700,
                    modifier = Modifier.weight(1f)
                )
                ReportKpiCard(
                    title = "Kapanan Adisyon",
                    value = "${report.closedBillCount} Masa",
                    subtitle = "Ort: ₺${"%.2f".format(report.averageBillAmount)}",
                    icon = Icons.Default.ReceiptLong,
                    bgColor = Amber50,
                    textColor = Amber700,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ödeme Yöntemi Dağılımı",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (report.paymentBreakdown.isEmpty()) {
                        Text(
                            text = "Bu dönemde henüz ödeme kaydı bulunmuyor.",
                            fontSize = 13.sp,
                            color = Slate500
                        )
                    } else {
                        report.paymentBreakdown.forEach { pm ->
                            val ratio = if (report.totalRevenue > 0) {
                                (pm.totalAmount / report.totalRevenue).toFloat().coerceIn(0f, 1f)
                            } else {
                                0f
                            }
                            val isCash = pm.paymentMethod == "NAKİT"
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
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
                                            imageVector = if (isCash) Icons.Default.Payments else Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = if (isCash) Emerald600 else Sky600,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "${pm.paymentMethod} (${pm.transactionCount} işlem)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Slate900
                                        )
                                    }
                                    Text(
                                        text = "₺${"%.2f".format(pm.totalAmount)} (%${(ratio * 100).toInt()})",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = if (isCash) Emerald700 else Sky700
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (isCash) Emerald500 else Sky600,
                                    trackColor = Slate100
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Amber600
                            )
                            Text(
                                text = "Ürün Bazlı Satış Detayı (${report.productBreakdown.size} Çeşit)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (report.productBreakdown.isEmpty()) {
                        Text(
                            text = "Bu dönemde satılan ürün kaydı bulunmuyor.",
                            fontSize = 13.sp,
                            color = Slate500
                        )
                    } else {
                        val maxRevenue = report.productBreakdown.maxOfOrNull { it.totalRevenue } ?: 1.0
                        report.productBreakdown.forEachIndexed { index, item ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = Slate100,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
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
                                        Surface(
                                            shape = CircleShape,
                                            color = Slate900
                                        ) {
                                            Text(
                                                text = "#${index + 1}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PureWhite,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = item.itemName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Slate900
                                        )
                                    }
                                    Text(
                                        text = "${item.categoryName} • Birim: ₺${"%.2f".format(item.unitPrice)}",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = {
                                            (item.totalRevenue / maxRevenue).toFloat().coerceIn(0.05f, 1f)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = Amber500,
                                        trackColor = Slate100
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Amber50
                                    ) {
                                        Text(
                                            text = "${item.totalQty} Adet",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Amber700,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "₺${"%.2f".format(item.totalRevenue)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Slate900
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (report.paymentHistory.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tahsilat Geçmişi (Son İşlemler)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        report.paymentHistory.take(30).forEachIndexed { idx, payment ->
                            if (idx > 0) {
                                HorizontalDivider(
                                    color = Slate100,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${payment.tableName} • ${payment.paymentMethod}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Slate900
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(payment.paidAt)) +
                                            if (payment.note.isNotBlank()) " • ${payment.note}" else "",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                                Text(
                                    text = "+₺${"%.2f".format(payment.amount)}",
                                    fontWeight = FontWeight.ExtraBold,
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
private fun ReportKpiCard(
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
        shape = RoundedCornerShape(18.dp),
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun FilledActionButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RestaurantSettingsTab(
    currentName: String,
    onSave: (String) -> Unit,
    onOpenRoleSettings: () -> Unit = {},
    updateStatus: UpdateStatus = UpdateStatus.Idle,
    onCheckForUpdates: () -> Unit = {},
    onDownloadAndInstall: (String) -> Unit = {},
    onInstallDownloadedApk: (File) -> Unit = {}
) {
    var nameInput by remember(currentName) { mutableStateOf(currentName) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Amber50,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = Amber600,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "İşletme & Restoran Kimliği",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900
                            )
                            Text(
                                text = "Ana ekranda ve raporlarda görünen işletme adını belirleyin",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Restoran / İşletme Adı") },
                        placeholder = { Text("Örn: Maya&Masa, Xubopos Bistro vb.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Hızlı Seçenekler / Öneriler:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val presets = listOf("Maya&Masa", "Xubopos", "Xubopos Cafe", "Adisyon POS")
                        items(presets) { preset ->
                            FilterChip(
                                selected = nameInput == preset,
                                onClick = { nameInput = preset },
                                label = { Text(preset, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                onSave(nameInput.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Değişiklikleri Kaydet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Sky50,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = Sky600,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Yerel Ağ & Çoklu Cihaz Modu",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Garson veya Mutfak (KDS) terminallerini yapılandırın",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenRoleSettings,
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cihaz Çalışma Rolünü Değiştir (Kasa / Garson / Mutfak)")
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Emerald50,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = null,
                                        tint = Emerald600,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Uygulama Güncellemesi",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Mevcut Sürüm: v${BuildConfig.VERSION_NAME} (Yapı ${BuildConfig.VERSION_CODE})",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate100
                        ) {
                            Text(
                                text = "GitHub",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when (val state = updateStatus) {
                        is UpdateStatus.Checking -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Amber500
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "GitHub'da yeni sürüm denetleniyor...",
                                    fontSize = 13.sp,
                                    color = Slate700
                                )
                            }
                        }

                        is UpdateStatus.UpdateAvailable -> {
                            val info = state.info
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Amber50,
                                border = BorderStroke(1.dp, Amber500.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            tint = Amber600,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Yeni Sürüm Hazır: ${info.latestVersion}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Amber700
                                        )
                                    }
                                    if (info.releaseNotes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = info.releaseNotes,
                                            fontSize = 12.sp,
                                            color = Slate700,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { onDownloadAndInstall(info.downloadUrl) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Yeni Sürümü İndir ve Kur (${"%.1f".format(info.apkSize / 1048576.0)} MB)",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        is UpdateStatus.Downloading -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { state.progressPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = Amber500
                                )
                                Text(
                                    text = "İndiriliyor: %${state.progressPercent} • ${"%.1f".format(state.downloadedBytes / 1048576.0)} MB / ${"%.1f".format(state.totalBytes / 1048576.0)} MB",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate700
                                )
                            }
                        }

                        is UpdateStatus.ReadyToInstall -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Emerald50,
                                border = BorderStroke(1.dp, Emerald500),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "✓ Güncelleme Paketi İndirildi!",
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald700
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { onInstallDownloadedApk(state.apkFile) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Paketi Şimdi Yükle", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        is UpdateStatus.UpToDate -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Emerald50,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Emerald600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Uygulamanız en son sürümde (v${BuildConfig.VERSION_NAME})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald700
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = onCheckForUpdates,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Tekrar Denetle")
                            }
                        }

                        is UpdateStatus.Error -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Rose50,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Hata: ${state.message}",
                                    fontSize = 12.sp,
                                    color = Rose600,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = onCheckForUpdates,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Yeniden Dene")
                            }
                        }

                        else -> {
                            Button(
                                onClick = onCheckForUpdates,
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Güncellemeleri Denetle")
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CANLI ÖNİZLEME (ANA EKRAN ÜST BAR)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Amber500,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PointOfSale,
                                    contentDescription = null,
                                    tint = Slate900,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Text(
                            text = nameInput.ifBlank { "Xubopos" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )
                    }
                }
            }
        }
    }
}
