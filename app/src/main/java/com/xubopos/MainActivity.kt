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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xubopos.network.DeviceRole
import com.xubopos.ui.components.DeviceRoleDialog
import com.xubopos.ui.screens.KitchenScreen
import com.xubopos.ui.screens.ManagementScreen
import com.xubopos.ui.screens.TableDetailScreen
import com.xubopos.ui.screens.TablesScreen
import com.xubopos.ui.theme.AppTheme
import com.xubopos.ui.viewmodel.AppScreen
import com.xubopos.ui.viewmodel.POSViewModel

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
    val viewModel: POSViewModel = viewModel()
    val selectedTableId by viewModel.selectedTableId.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val deviceRole by viewModel.deviceRole.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val resolvedServerHost by viewModel.resolvedServerHost.collectAsState()
    val connectedClientCount by viewModel.connectedClientCount.collectAsState()

    var showRoleDialog by remember { mutableStateOf(false) }

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
                    onBack = { viewModel.navigateTo(AppScreen.TABLES) }
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
}
