package com.xubopos.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.xubopos.data.repository.BackupSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.SwitchDefaults
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
import com.xubopos.ui.theme.Rose100
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
    val managementPin by viewModel.managementPin.collectAsState()
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
                    onAddProduct = { catId, name, price, desc, hasDailyLimit, limit ->
                        viewModel.addCustomProduct(catId, name, price, desc, hasDailyLimit, limit)
                    },
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
                    restaurantName = restaurantName,
                    selectedPeriod = selectedPeriod,
                    report = detailedReport,
                    tables = tables,
                    onSelectPeriod = viewModel::selectReportPeriod,
                    onCloseDay = viewModel::closeDay
                )

                3 -> {
                    val pendingRestoreSummary by viewModel.pendingRestoreSummary.collectAsState()
                    RestaurantSettingsTab(
                        currentName = restaurantName,
                        currentPin = managementPin,
                        onSave = { name ->
                            viewModel.updateRestaurantInfo(name)
                        },
                        onSavePin = { pin ->
                            viewModel.setManagementPin(pin)
                        },
                        onExportFullBackupToUri = { uri ->
                            viewModel.exportFullBackupToUri(context, uri)
                        },
                        onExportMenuBackupToUri = { uri ->
                            viewModel.exportMenuBackupToUri(context, uri)
                        },
                        onShareBackup = { isFull ->
                            viewModel.shareBackupFile(context, isFull)
                        },
                        onSelectBackupFile = { uri ->
                            viewModel.readBackupFromUri(context, uri)
                        },
                        pendingRestoreSummary = pendingRestoreSummary,
                        onConfirmRestore = { summary ->
                            viewModel.confirmRestoreBackup(summary)
                        },
                        onDismissRestore = {
                            viewModel.clearPendingRestore()
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
}

@Composable
private fun MenuManagementTab(
    categories: List<MenuCategory>,
    menuItems: List<MenuItem>,
    onAddCategory: (String) -> Unit,
    onUpdateCategory: (MenuCategory) -> Unit,
    onDeleteCategory: (MenuCategory) -> Unit,
    onAddProduct: (Int, String, Double, String, Boolean, Int) -> Unit,
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
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = Amber600,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Kategoriler (${categories.size})",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Slate900
                        )
                    }
                    FilledActionButton(
                        text = "Kategori Ekle",
                        onClick = { showAddCategoryDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == 0,
                            onClick = { selectedCategoryId = 0 },
                            label = {
                                Text(
                                    text = "Tümü (${menuItems.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
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
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 3.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Amber50
                            ) {
                                Text(
                                    text = categoryName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Amber700,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            if (product.hasDailyLimit) {
                                val isSoldOut = product.remainingStock <= 0
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSoldOut) Rose50 else Sky50
                                ) {
                                    Text(
                                        text = if (isSoldOut) "Tükendi" else "Kota: ${product.remainingStock}/${product.dailyLimit}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSoldOut) Rose600 else Sky700,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Slate900,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (product.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
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
        var hasDailyLimit by remember { mutableStateOf(editingProduct?.hasDailyLimit ?: false) }
        var dailyLimitText by remember {
            mutableStateOf(editingProduct?.dailyLimit?.takeIf { it > 0 }?.toString() ?: "15")
        }

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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Günlük Porsiyon / Kota Limiti",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "Günü kapatıldığında bu sayıya tazelenir",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                        Switch(
                            checked = hasDailyLimit,
                            onCheckedChange = { hasDailyLimit = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = Amber600
                            )
                        )
                    }

                    if (hasDailyLimit) {
                        OutlinedTextField(
                            value = dailyLimitText,
                            onValueChange = { dailyLimitText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Günlük Adet / Porsiyon (Örn: 15)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = priceText.replace(",", ".").toDoubleOrNull()
                        if (name.isNotBlank() && price != null && price > 0) {
                            val current = editingProduct
                            val limit = if (hasDailyLimit) dailyLimitText.toIntOrNull() ?: 15 else 0
                            if (current == null) {
                                onAddProduct(catId, name.trim(), price, desc.trim(), hasDailyLimit, limit)
                            } else {
                                val newRemaining = if (hasDailyLimit) {
                                    if (!current.hasDailyLimit || current.dailyLimit != limit) limit else current.remainingStock
                                } else 0
                                onUpdateProduct(
                                    current.copy(
                                        categoryId = catId,
                                        name = name.trim(),
                                        price = price,
                                        description = desc.trim(),
                                        hasDailyLimit = hasDailyLimit,
                                        dailyLimit = limit,
                                        remainingStock = newRemaining
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = "Masa & Salon Düzeni",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900
                    )
                    Text(
                        text = "Masa ve salon bölgelerini yönetin",
                        fontSize = 12.sp,
                        color = Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(
                    onClick = {
                        editingTable = null
                        showTableModal = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Yeni Masa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
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
    restaurantName: String,
    selectedPeriod: ReportPeriod,
    report: DetailedSalesReport,
    tables: List<RestaurantTable>,
    onSelectPeriod: (ReportPeriod) -> Unit,
    onCloseDay: () -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()) }
    var showCloseDayDialog by remember { mutableStateOf(false) }
    var showOpenTablesWarningDialog by remember { mutableStateOf(false) }

    val shareSalesReport = {
        val sb = StringBuilder()
        sb.appendLine("📊 $restaurantName - Satış Raporu (${selectedPeriod.label})")
        sb.appendLine("Tarih: ${dateFormatter.format(Date())}")
        sb.appendLine("--------------------------------")
        sb.appendLine("💰 Toplam Ciro: ₺${"%.2f".format(report.totalRevenue)}")
        sb.appendLine("🧾 Kapanan Masa: ${report.closedBillCount} (Ort: ₺${"%.2f".format(report.averageBillAmount)})")
        sb.appendLine("💳 Toplam Tahsilat İşlemi: ${report.totalTransactions}")
        if (report.paymentBreakdown.isNotEmpty()) {
            sb.appendLine("\n📌 Ödeme Dağılımı:")
            report.paymentBreakdown.forEach { pm ->
                sb.appendLine("• ${pm.paymentMethod}: ₺${"%.2f".format(pm.totalAmount)} (${pm.transactionCount} işlem)")
            }
        }
        if (report.productBreakdown.isNotEmpty()) {
            sb.appendLine("\n🍽️ Ürün Satış Özeti:")
            report.productBreakdown.take(25).forEach { prod ->
                sb.appendLine("• ${prod.itemName}: ${prod.totalQty} Adet = ₺${"%.2f".format(prod.totalRevenue)}")
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$restaurantName - ${selectedPeriod.label} Raporu")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(Intent.createChooser(intent, "Raporu Paylaş"))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Amber500,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Günü Kapat & Stokları Yenile",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Z-Raporu devrini yapar, günlük sayılı porsiyonları tazeler.",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { shareSalesReport() },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Paylaş",
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paylaş",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        Button(
                            onClick = {
                                val hasOpenTables = tables.any { it.isOccupied }
                                if (hasOpenTables) {
                                    showOpenTablesWarningDialog = true
                                } else {
                                    showCloseDayDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Günü Kapat",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

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

    if (showCloseDayDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDayDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Amber600,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Günü Kapatmak İstiyor musunuz?",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Text(
                    text = "Bugünkü ciro (₺${"%.2f".format(report.totalRevenue)}) devredilecek, rapor sıfırlanıp yeni güne aktarılacak ve sayılı ürünlerin günlük stok kotaları tazelenecektir."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCloseDay()
                        showCloseDayDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber600)
                ) {
                    Text("Evet, Günü Kapat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDayDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    if (showOpenTablesWarningDialog) {
        val openTables = tables.filter { it.isOccupied }
        AlertDialog(
            onDismissRequest = { showOpenTablesWarningDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.TableRestaurant,
                    contentDescription = null,
                    tint = Rose600,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Açık Masa Var - Gün Kapatılamaz",
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Günü kapatabilmek için önce tüm açık masaların hesaplarının tahsil edilip kapatılması veya iptal edilmesi gerekmektedir.",
                        fontSize = 13.sp,
                        color = Slate700
                    )
                    Text(
                        text = "Açık Masalar (${openTables.size} Masa):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Rose600
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Rose50,
                        border = BorderStroke(1.dp, Rose100),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(openTables, key = { it.id }) { tbl ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "• ${tbl.name} (${tbl.zone})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "₺${"%.2f".format(tbl.remainingAmount)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = Amber700
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showOpenTablesWarningDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                ) {
                    Text("Anladım")
                }
            }
        )
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
    currentPin: String = "",
    onSave: (String) -> Unit,
    onSavePin: (String) -> Unit = {},
    onExportFullBackupToUri: (Uri) -> Unit = {},
    onExportMenuBackupToUri: (Uri) -> Unit = {},
    onShareBackup: (Boolean) -> Unit = {},
    onSelectBackupFile: (Uri) -> Unit = {},
    pendingRestoreSummary: BackupSummary? = null,
    onConfirmRestore: (BackupSummary) -> Unit = {},
    onDismissRestore: () -> Unit = {},
    onOpenRoleSettings: () -> Unit = {},
    updateStatus: UpdateStatus = UpdateStatus.Idle,
    onCheckForUpdates: () -> Unit = {},
    onDownloadAndInstall: (String) -> Unit = {},
    onInstallDownloadedApk: (File) -> Unit = {}
) {
    val context = LocalContext.current
    var nameInput by remember(currentName) { mutableStateOf(currentName) }
    var pinInput by remember(currentPin) { mutableStateOf(currentPin) }

    val exportFullBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { onExportFullBackupToUri(it) }
    }

    val exportMenuBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { onExportMenuBackupToUri(it) }
    }

    val openBackupFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onSelectBackupFile(it) }
    }

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
                        Text(
                            text = "İşletme Adı",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Restoran / İşletme Adı") },
                        placeholder = { Text("Örn: Maya&Masa, Xubopos Bistro vb.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Rose50,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Rose600,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Yönetici PIN Kilidi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900
                            )
                            Text(
                                text = if (currentPin.isNotBlank()) "Aktif (Yönetim ekranı şifre korumalı)" else "Kapalı (Şifresiz erişim)",
                                fontSize = 12.sp,
                                color = if (currentPin.isNotBlank()) Emerald600 else Slate500,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInput = it },
                        label = { Text("4 Haneli PIN Kodu (Kaldırmak için boş bırakın)") },
                        placeholder = { Text("Örn: 1234") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (pinInput.length == 4 || pinInput.isEmpty()) {
                                    onSavePin(pinInput)
                                }
                            },
                            enabled = pinInput.length == 4 || pinInput.isEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (pinInput.isEmpty()) "PIN Kilidini Kaldır" else "PIN Kodunu Kaydet", fontWeight = FontWeight.Bold)
                        }
                        if (currentPin.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    pinInput = ""
                                    onSavePin("")
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Sıfırla", color = Rose600, fontWeight = FontWeight.Bold)
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
                Column(modifier = Modifier.padding(18.dp)) {
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
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = Emerald600,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Veri Yedekleme & Geri Yükleme",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900
                            )
                            Text(
                                text = "Geçmiş satışlar, adisyonlar, masalar ve menüyü dosya olarak yedekleyin",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. BÖLÜM: TAM SİSTEM YEDEĞİ (ÖNERİLEN)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Slate50,
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1. Tam Sistem Yedeği (Önerilen)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Slate900
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Emerald100
                                ) {
                                    Text(
                                        text = "Tüm Satışlar Dahil",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald700,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tüm geçmiş adisyonlar, ödemeler, masalar, mutfak fişleri ve menüyü eksiksiz kapsar.",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                        val cleanName = currentName.replace(Regex("[^a-zA-Z0-9_]"), "_")
                                        exportFullBackupLauncher.launch("${cleanName}_tam_yedek_$timeStamp.json")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Dosyaya Kaydet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { onShareBackup(true) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Dosyayı Paylaş", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. BÖLÜM: YALNIZCA MENÜ YEDEĞİ
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Slate50,
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "2. Yalnızca Menü Yedeği",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sadece menü kategorilerini ve ürünleri kapsar; geçmiş satışları içermez.",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                        val cleanName = currentName.replace(Regex("[^a-zA-Z0-9_]"), "_")
                                        exportMenuBackupLauncher.launch("${cleanName}_menu_yedek_$timeStamp.json")
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Menüyü Kaydet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { onShareBackup(false) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Menüyü Paylaş", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. BÖLÜM: GERİ YÜKLEME BUTONU
                    Button(
                        onClick = {
                            openBackupFileLauncher.launch(arrayOf("application/json", "*/*"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yedek Dosyası Seç & Geri Yükle", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
    }

    if (pendingRestoreSummary != null) {
        val summary = pendingRestoreSummary
        AlertDialog(
            onDismissRequest = onDismissRestore,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (summary.isFullBackup) Icons.Default.Warning else Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = if (summary.isFullBackup) Amber600 else Emerald600
                    )
                    Text(
                        text = if (summary.isFullBackup) "Tam Sistem Yedeğini Geri Yükle" else "Menü Yedeğini Geri Yükle",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Slate100,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("İşletme:", fontSize = 12.sp, color = Slate500)
                                Text(
                                    summary.restaurantName.ifBlank { "Xubopos" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Slate900
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Yedek Tarihi:", fontSize = 12.sp, color = Slate500)
                                val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(summary.createdAt))
                                Text(dateStr, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate900)
                            }
                            HorizontalDivider(color = Slate200)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Menü:", fontSize = 12.sp, color = Slate500)
                                Text("${summary.categoryCount} Kategori, ${summary.menuItemCount} Ürün", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate900)
                            }
                            if (summary.isFullBackup) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Masalar:", fontSize = 12.sp, color = Slate500)
                                    Text("${summary.tableCount} Masa", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate900)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Adisyon & Kalemler:", fontSize = 12.sp, color = Slate500)
                                    Text("${summary.billCount} Adisyon (${summary.orderItemCount} Kalem)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate900)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tahsilat İşlemleri:", fontSize = 12.sp, color = Slate500)
                                    Text("${summary.paymentCount} Ödeme Kaydı", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate900)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Kayıtlı Toplam Ciro:", fontSize = 12.sp, color = Slate500)
                                    Text(
                                        "₺${String.format(Locale.getDefault(), "%,.2f", summary.totalRevenue)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = Emerald700
                                    )
                                }
                            }
                        }
                    }

                    if (summary.isFullBackup) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Amber50,
                            border = BorderStroke(1.dp, Amber100),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Amber600,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "DİKKAT: Tam sistem yedeği geri yüklendiğinde mevcut cihazdaki tüm veriler temizlenecek ve yerini bu yedekteki veriler alacaktır.",
                                    fontSize = 11.sp,
                                    color = Amber700,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Yedekteki kategoriler ve ürünler mevcut menünüze eklenecektir.",
                            fontSize = 12.sp,
                            color = Slate700
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onConfirmRestore(summary) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (summary.isFullBackup) Amber600 else Emerald600
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (summary.isFullBackup) "Onayla ve Geri Yükle" else "İçe Aktar",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRestore) {
                    Text("Vazgeç", color = Slate500)
                }
            }
        )
    }
}
