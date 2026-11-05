package com.xubopos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xubopos.network.DeviceRole
import com.xubopos.ui.components.DeviceRoleDialog
import com.xubopos.ui.screens.KitchenScreen
import com.xubopos.ui.screens.ManagementScreen
import com.xubopos.ui.screens.TableDetailScreen
import com.xubopos.ui.screens.TablesScreen
import com.xubopos.ui.theme.Amber500
import com.xubopos.ui.theme.Amber600
import com.xubopos.ui.theme.AppTheme
import com.xubopos.ui.theme.Emerald600
import com.xubopos.ui.theme.Slate100
import com.xubopos.ui.theme.Slate500
import com.xubopos.ui.theme.Slate700
import com.xubopos.ui.viewmodel.AppScreen
import com.xubopos.ui.viewmodel.POSViewModel
import com.xubopos.update.UpdateStatus

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                POSApp()
            }
        }
    }
}

@Composable
fun POSApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: POSViewModel = viewModel()
    val selectedTableId by viewModel.selectedTableId.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val deviceRole by viewModel.deviceRole.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val resolvedServerHost by viewModel.resolvedServerHost.collectAsState()
    val connectedClientCount by viewModel.connectedClientCount.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()

    var showRoleDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> viewModel.onAppBackgrounded()
                Lifecycle.Event.ON_RESUME -> viewModel.onAppResumed()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Uygulama açıldığında GitHub üzerinden yeni sürüm denetle
    LaunchedEffect(Unit) {
        viewModel.checkForUpdates(BuildConfig.VERSION_NAME)
    }

    BackHandler(enabled = selectedTableId != null || (currentScreen != AppScreen.TABLES && deviceRole != DeviceRole.KITCHEN)) {
        if (selectedTableId != null) {
            viewModel.selectTable(null)
        } else {
            viewModel.navigateTo(AppScreen.TABLES)
        }
    }

    val screenState = Pair(currentScreen, selectedTableId)

    AnimatedContent(
        targetState = screenState,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            fadeIn(animationSpec = tween(220)).togetherWith(fadeOut(animationSpec = tween(180)))
        },
        label = "ScreenTransition"
    ) { (screen, tableId) ->
        when {
            tableId != null -> {
                TableDetailScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.selectTable(null) }
                )
            }
            screen == AppScreen.MANAGEMENT -> {
                ManagementScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(AppScreen.TABLES) },
                    onOpenRoleSettings = { showRoleDialog = true }
                )
            }
            screen == AppScreen.KITCHEN || deviceRole == DeviceRole.KITCHEN -> {
                KitchenScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(AppScreen.TABLES) },
                    onOpenRoleSettings = { showRoleDialog = true }
                )
            }
            else -> {
                TablesScreen(
                    viewModel = viewModel,
                    onTableClick = { table -> viewModel.selectTable(table.id) },
                    onNavigateManagement = { viewModel.navigateTo(AppScreen.MANAGEMENT) },
                    onNavigateKitchen = { viewModel.navigateTo(AppScreen.KITCHEN) },
                    onOpenRoleSettings = { showRoleDialog = true }
                )
            }
        }
    }

    if (showRoleDialog) {
        DeviceRoleDialog(
            currentRole = deviceRole,
            connectionStatus = connectionStatus,
            serverHost = resolvedServerHost,
            connectedClientCount = connectedClientCount,
            onDismiss = { showRoleDialog = false },
            onSelectRole = { newRole ->
                viewModel.switchDeviceRole(newRole)
            },
            onConnectManualIp = { ip ->
                viewModel.connectManualIp(ip)
            }
        )
    }

    // --- OTOMATİK GÜNCELLEME DİYALOGLARI ---
    when (val state = updateStatus) {
        is UpdateStatus.UpdateAvailable -> {
            val info = state.info
            AlertDialog(
                onDismissRequest = { viewModel.resetUpdateStatus() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = Amber500
                    )
                },
                title = {
                    Text(
                        text = "Yeni Güncelleme Mevcut! (${info.latestVersion})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Xubopos'un yeni bir sürümü yayınlandı.\nMevcut Sürüm: v${BuildConfig.VERSION_NAME}",
                            fontSize = 13.sp,
                            color = Slate700
                        )
                        if (info.releaseNotes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate100,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 120.dp)
                            ) {
                                Text(
                                    text = info.releaseNotes,
                                    fontSize = 11.sp,
                                    color = Slate700,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        Text(
                            text = "Boyut: ${"%.1f".format(info.apkSize / (1024.0 * 1024.0))} MB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate500
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.downloadAndInstallUpdate(context, info.downloadUrl)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Şimdi Güncelle", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.resetUpdateStatus() }) {
                        Text("Daha Sonra")
                    }
                }
            )
        }

        is UpdateStatus.Downloading -> {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = "Güncelleme İndiriliyor...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { state.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Amber500
                        )
                        Text(
                            text = "%${state.progressPercent} • ${"%.1f".format(state.downloadedBytes / 1048576.0)} MB / ${"%.1f".format(state.totalBytes / 1048576.0)} MB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                    }
                },
                confirmButton = {}
            )
        }

        is UpdateStatus.ReadyToInstall -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetUpdateStatus() },
                title = {
                    Text(
                        text = "İndirme Tamamlandı",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Text(
                        text = "Yeni APK başarıyla indirildi. Yükleme işlemini başlatmak için onaylayın.",
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.installDownloadedApk(context, state.apkFile)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Şimdi Yükle", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.resetUpdateStatus() }) {
                        Text("Kapat")
                    }
                }
            )
        }

        else -> {}
    }
}
