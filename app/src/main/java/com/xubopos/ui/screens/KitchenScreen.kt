package com.xubopos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.xubopos.data.model.KitchenTicketEntity
import com.xubopos.network.ConnectionStatus
import com.xubopos.network.DeviceRole
import com.xubopos.network.KitchenAudioHelper
import com.xubopos.ui.components.formatElapsedMinutes
import com.xubopos.ui.components.formatOrderTime
import com.xubopos.ui.components.formatOrderTimeWithElapsed
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
fun KitchenScreen(
    viewModel: POSViewModel,
    onBack: () -> Unit = {},
    onOpenRoleSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val tickets by viewModel.activeKitchenTickets.collectAsState()
    val role by viewModel.deviceRole.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val connectedClientsCount by viewModel.connectedClientCount.collectAsState()
    val serverHostName by viewModel.resolvedServerHost.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    var selectedFilter by remember { mutableStateOf("Tümü") }
    var soundEnabled by remember { mutableStateOf(true) }
    var previousTicketCount by remember { mutableStateOf(tickets.size) }
    var isLandscapeLocked by remember {
        mutableStateOf(activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
    }

    val currentTimeMillis by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            delay(5000L)
            value = System.currentTimeMillis()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(tickets.size) {
        if (tickets.size > previousTicketCount) {
            if (soundEnabled) {
                KitchenAudioHelper.playKitchenAlert(context)
            }
            snackbarHostState.showSnackbar("Yeni sipariş geldi!")
        }
        previousTicketCount = tickets.size
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val filteredTickets = remember(tickets, selectedFilter) {
        when (selectedFilter) {
            "Bekleyenler" -> tickets.filter { it.status == "PENDING" }
            "Hazırlananlar" -> tickets.filter { it.status == "PREPARING" }
            "Hazır Olanlar" -> tickets.filter { it.status == "READY" }
            "İçecekler / Bar" -> tickets.filter { ticket ->
                ticket.getItems().any {
                    it.categoryName.contains("İçecek", ignoreCase = true) ||
                    it.categoryName.contains("Bar", ignoreCase = true)
                }
            }
            "Mutfak / Yemek" -> tickets.filter { ticket ->
                ticket.getItems().any {
                    !it.categoryName.contains("İçecek", ignoreCase = true)
                }
            }
            else -> tickets
        }
    }

    val pendingCount = tickets.count { it.status == "PENDING" }
    val preparingCount = tickets.count { it.status == "PREPARING" }
    val readyCount = tickets.count { it.status == "READY" }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Slate900,
        topBar = {
            Surface(
                color = Slate900,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            if (role == DeviceRole.SERVER) {
                                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Geri",
                                        tint = PureWhite
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Amber500,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = Slate900,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Mutfak (KDS)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PureWhite
                                )
                                Text(
                                    text = "${tickets.size} Aktif Sipariş Fişi",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            ConnectionStatusPill(
                                role = role,
                                status = connectionStatus,
                                connectedClientsCount = connectedClientsCount,
                                serverHost = serverHostName,
                                onClick = onOpenRoleSettings
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Ses butonu
                            IconButton(
                                onClick = { soundEnabled = !soundEnabled },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (soundEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                    contentDescription = "Zil Sesi",
                                    tint = if (soundEnabled) Amber500 else Slate500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Tablet Yatay Mod Kilit Butonu
                            IconButton(
                                onClick = {
                                    activity?.let {
                                        if (isLandscapeLocked) {
                                            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                            isLandscapeLocked = false
                                        } else {
                                            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                            isLandscapeLocked = true
                                        }
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenRotation,
                                    contentDescription = "Yatay Ekran Modu",
                                    tint = if (isLandscapeLocked) Amber500 else PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // BELİRGİN MOD DEĞİŞTİR BUTONU
                            Button(
                                onClick = onOpenRoleSettings,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Amber500,
                                    contentColor = Slate900
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = Slate900,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Mod Değiştir",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate900
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val filters = listOf(
                            "Tümü (${tickets.size})",
                            "Bekleyenler ($pendingCount)",
                            "Hazırlananlar ($preparingCount)",
                            "Hazır Olanlar ($readyCount)",
                            "Mutfak / Yemek",
                            "İçecekler / Bar"
                        )
                        items(filters) { filterLabel ->
                            val cleanFilter = filterLabel.substringBefore(" (")
                            val isSelected = selectedFilter == cleanFilter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = cleanFilter },
                                label = {
                                    Text(
                                        text = filterLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Amber500,
                                    selectedLabelColor = Slate900,
                                    containerColor = Slate800,
                                    labelColor = PureWhite
                                ),
                                border = null
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate900)
        ) {
            if (filteredTickets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Slate800,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalDining,
                                    contentDescription = null,
                                    tint = Slate500,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Bekleyen sipariş bulunmuyor",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Garsonlar sipariş girdikçe fişler buraya canlı düşecektir",
                            fontSize = 13.sp,
                            color = Slate400
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 310.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredTickets, key = { it.ticketId }) { ticket ->
                        KitchenTicketCard(
                            ticket = ticket,
                            currentTime = currentTimeMillis,
                            onUpdateStatus = { newStatus ->
                                viewModel.updateKitchenTicketStatus(ticket.ticketId, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KitchenTicketCard(
    ticket: KitchenTicketEntity,
    currentTime: Long,
    onUpdateStatus: (String) -> Unit
) {
    val items = remember(ticket.itemsJson) { ticket.getItems() }
    val itemCompletedStates = remember { mutableStateMapOf<Long, Boolean>() }
    val coroutineScope = rememberCoroutineScope()

    var isCompleting by remember { mutableStateOf(false) }

    val completionScale by animateFloatAsState(
        targetValue = if (isCompleting) 0.88f else 1f,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "completionScale"
    )
    val completionAlpha by animateFloatAsState(
        targetValue = if (isCompleting) 0f else 1f,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "completionAlpha"
    )

    val elapsedMinutes = ((currentTime - ticket.createdAt) / 60000L).coerceAtLeast(0L)
    val isUrgent = elapsedMinutes >= 15L
    val isWarning = elapsedMinutes in 8L..14L
    val isNewlyArrived = (currentTime - ticket.createdAt) < 45000L

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val status = ticket.status

    val statusBadgeBg = when (status) {
        "PREPARING" -> Sky100
        "READY" -> Emerald100
        else -> if (isUrgent) Rose100 else Amber100
    }
    val statusBadgeText = when (status) {
        "PREPARING" -> Sky700
        "READY" -> Emerald700
        else -> if (isUrgent) Rose600 else Amber700
    }
    val statusLabel = when (status) {
        "PREPARING" -> "Hazırlanıyor"
        "READY" -> "Hazır / Servis Bekliyor"
        else -> if (isUrgent) "Gecikiyor!" else "Yeni Sipariş"
    }

    val cardBorderColor = when {
        isCompleting -> Emerald500
        status == "READY" -> Emerald500
        status == "PREPARING" -> Sky600
        isUrgent -> Rose600
        isNewlyArrived -> Amber500.copy(alpha = pulseAlpha)
        else -> Amber500
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(completionScale)
            .alpha(completionAlpha),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (isCompleting) Emerald50 else PureWhite),
        border = BorderStroke(if (isNewlyArrived || isUrgent) 3.dp else 2.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = ticket.tableName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Slate900
                        )
                        if (isNewlyArrived) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Amber500.copy(alpha = pulseAlpha)
                            ) {
                                Text(
                                    text = "YENİ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Slate900,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Fiş #${ticket.ticketId} • ${ticket.waiterName}",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusBadgeBg
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusBadgeText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            isUrgent -> Rose50
                            isWarning -> Amber50
                            else -> Slate100
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = when {
                                    isUrgent -> Rose600
                                    isWarning -> Amber700
                                    else -> Slate600
                                },
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${formatOrderTime(ticket.createdAt)} • ${if (elapsedMinutes == 0L) "Yeni" else "${elapsedMinutes} dk"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isUrgent -> Rose600
                                    isWarning -> Amber700
                                    else -> Slate700
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    val isChecked = itemCompletedStates[item.id] == true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isChecked) Slate100 else Slate50)
                            .clickable {
                                itemCompletedStates[item.id] = !isChecked
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isChecked) Slate300 else Amber500
                            ) {
                                Text(
                                    text = "${item.quantity}x",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isChecked) Slate700 else Slate900,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = item.itemName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChecked) Slate400 else Slate900,
                                    textDecoration = if (isChecked) TextDecoration.LineThrough else null
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${if (elapsedMinutes == 0L) "Yeni" else "${elapsedMinutes} dk önce"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = when {
                                            isUrgent -> Rose600
                                            isWarning -> Amber700
                                            else -> Slate500
                                        }
                                    )
                                    if (item.note.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Amber100
                                        ) {
                                            Text(
                                                text = "Not: ${item.note}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Amber700,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isChecked,
                            enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.5f),
                            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.5f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Hazır",
                                tint = Emerald600,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(10.dp))

            when (status) {
                "PENDING" -> {
                    Button(
                        onClick = { onUpdateStatus("PREPARING") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber600)
                    ) {
                        Icon(imageVector = Icons.Default.LocalDining, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HAZIRLANMAYA BAŞLA",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }

                "PREPARING" -> {
                    Button(
                        onClick = { onUpdateStatus("READY") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SİPARİŞ HAZIR! (ZİL ÇAL)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }

                "READY" -> {
                    Button(
                        onClick = {
                            if (!isCompleting) {
                                isCompleting = true
                                coroutineScope.launch {
                                    delay(300L)
                                    onUpdateStatus("COMPLETED")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800)
                    ) {
                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCompleting) "TAMAMLANDI ✓" else "TESLİM EDİLDİ (TAMAMLA)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusPill(
    role: DeviceRole,
    status: ConnectionStatus,
    connectedClientsCount: Int,
    serverHost: String?,
    onClick: (() -> Unit)? = null
) {
    val (bgColor, dotColor, text) = when {
        role == DeviceRole.SERVER -> {
            Triple(
                Slate800,
                Emerald500,
                if (connectedClientsCount > 0) "Kasa • $connectedClientsCount Cihaz Bağlı" else "Kasa • Cihaz Bekleniyor"
            )
        }
        status == ConnectionStatus.CONNECTED -> {
            Triple(
                Emerald50,
                Emerald600,
                "Kasaya Bağlı (${serverHost ?: "Yerel Ağ"})"
            )
        }
        status == ConnectionStatus.SEARCHING -> {
            Triple(
                Amber100,
                Amber600,
                "Kasa Aranıyor (mDNS)..."
            )
        }
        status == ConnectionStatus.CONNECTING -> {
            Triple(
                Sky100,
                Sky600,
                "Bağlanılıyor..."
            )
        }
        else -> {
            Triple(
                Rose100,
                Rose600,
                "Bağlantı Yok"
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = if (onClick != null) {
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
        } else Modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (role == DeviceRole.SERVER) PureWhite else Slate800
            )
            if (onClick != null) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Rol / Ağ Ayarları",
                    tint = dotColor,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
