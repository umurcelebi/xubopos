package com.xubopos.ui.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.xubopos.data.db.AppDatabase
import com.xubopos.data.model.DailyReport
import com.xubopos.data.model.DetailedSalesReport
import com.xubopos.data.model.KitchenTicketEntity
import com.xubopos.data.model.KitchenTicketItemDto
import com.xubopos.data.model.MenuCategory
import com.xubopos.data.model.MenuItem
import com.xubopos.data.model.OrderBill
import com.xubopos.data.model.OrderItem
import com.xubopos.data.model.PaymentRecord
import com.xubopos.data.model.ReportPeriod
import com.xubopos.data.model.RestaurantTable
import com.xubopos.data.repository.POSRepository
import com.xubopos.network.AddOrderPayload
import com.xubopos.network.ConnectionStatus
import com.xubopos.network.DeviceRole
import com.xubopos.network.FullSyncPayload
import com.xubopos.network.KitchenReadyAlertPayload
import com.xubopos.network.LocalPosClient
import com.xubopos.network.LocalPosServer
import com.xubopos.network.MenuSyncPayload
import com.xubopos.network.ModifyOrderItemPayload
import com.xubopos.network.NetworkPacket
import android.provider.Settings
import com.xubopos.network.PosNsdHelper
import com.xubopos.network.TableLockEntry
import com.xubopos.network.TableLockPayload
import com.xubopos.update.AppUpdateManager
import com.xubopos.update.UpdateInfo
import com.xubopos.update.UpdateStatus
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    TABLES,
    MANAGEMENT,
    KITCHEN
}

@OptIn(ExperimentalCoroutinesApi::class)
class POSViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: POSRepository
    private val prefs = application.getSharedPreferences("xubopos_prefs", Context.MODE_PRIVATE)

    private val _deviceRole = MutableStateFlow(
        try {
            DeviceRole.valueOf(prefs.getString("device_role", DeviceRole.SERVER.name) ?: DeviceRole.SERVER.name)
        } catch (_: Exception) {
            DeviceRole.SERVER
        }
    )
    val deviceRole: StateFlow<DeviceRole> = _deviceRole.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _connectedClientCount = MutableStateFlow(0)
    val connectedClientCount: StateFlow<Int> = _connectedClientCount.asStateFlow()

    private val _resolvedServerHost = MutableStateFlow<String?>(
        prefs.getString("last_server_host", null)
    )
    val resolvedServerHost: StateFlow<String?> = _resolvedServerHost.asStateFlow()

    private val _restaurantName = MutableStateFlow(
        prefs.getString("restaurant_name", "Xubopos") ?: "Xubopos"
    )
    val restaurantName: StateFlow<String> = _restaurantName.asStateFlow()

    private val _remoteTables = MutableStateFlow<List<RestaurantTable>>(emptyList())
    private val _remoteCategories = MutableStateFlow<List<MenuCategory>>(emptyList())
    private val _remoteMenuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    private val _remoteKitchenTickets = MutableStateFlow<List<KitchenTicketEntity>>(emptyList())
    private val _remoteActiveItems = MutableStateFlow<List<OrderItem>>(emptyList())

    private val _lastDayCloseTime = MutableStateFlow(prefs.getLong("last_day_close_time", 0L))

    val allTables: StateFlow<List<RestaurantTable>>
    val allCategories: StateFlow<List<MenuCategory>>
    val allMenuItems: StateFlow<List<MenuItem>>
    val activeKitchenTickets: StateFlow<List<KitchenTicketEntity>>
    val dailyReport: StateFlow<DailyReport>

    private val _currentScreen = MutableStateFlow(
        if (_deviceRole.value == DeviceRole.KITCHEN) AppScreen.KITCHEN else AppScreen.TABLES
    )
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedZone = MutableStateFlow("Tümü")
    val selectedZone: StateFlow<String> = _selectedZone.asStateFlow()

    private val _selectedTableId = MutableStateFlow<Int?>(null)
    val selectedTableId: StateFlow<Int?> = _selectedTableId.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow(0)
    val selectedCategoryId: StateFlow<Int> = _selectedCategoryId.asStateFlow()

    private val _menuSearchQuery = MutableStateFlow("")
    val menuSearchQuery: StateFlow<String> = _menuSearchQuery.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _selectedReportPeriod = MutableStateFlow(ReportPeriod.TODAY)
    val selectedReportPeriod: StateFlow<ReportPeriod> = _selectedReportPeriod.asStateFlow()

    private val _detailedReport = MutableStateFlow(DetailedSalesReport())
    val detailedReport: StateFlow<DetailedSalesReport> = _detailedReport.asStateFlow()

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    val selectedTable: StateFlow<RestaurantTable?>
    val currentBill: StateFlow<OrderBill?>
    val currentOrderItems: StateFlow<List<OrderItem>>
    val currentPayments: StateFlow<List<PaymentRecord>>
    val filteredMenuItems: StateFlow<List<MenuItem>>

    val deviceId: String = Settings.Secure.getString(
        application.contentResolver,
        Settings.Secure.ANDROID_ID
    ) ?: UUID.randomUUID().toString()

    val deviceName: String
        get() = when (_deviceRole.value) {
            DeviceRole.SERVER -> "Kasa"
            DeviceRole.WAITER -> "Garson (${Build.MODEL ?: "Terminal"})"
            DeviceRole.KITCHEN -> "Mutfak (${Build.MODEL ?: "Ekran"})"
        }

    private val _activeTableLocks = MutableStateFlow<Map<Int, TableLockEntry>>(emptyMap())
    val activeTableLocks: StateFlow<Map<Int, TableLockEntry>> = _activeTableLocks.asStateFlow()

    private val _isKitchenConnected = MutableStateFlow(false)
    val isKitchenConnected: StateFlow<Boolean> = _isKitchenConnected.asStateFlow()

    private val _managementPin = MutableStateFlow(prefs.getString("management_pin", "") ?: "")
    val managementPin: StateFlow<String> = _managementPin.asStateFlow()

    private val serverClientTableLocks = ConcurrentHashMap<org.java_websocket.WebSocket, MutableSet<Int>>()

    private var server: LocalPosServer? = null
    private var client: LocalPosClient? = null
    private var clientStatusJob: Job? = null
    private var autoReconnectJob: Job? = null
    private var nsdHelper: PosNsdHelper? = null
    private val serverBroadcastJobs = mutableListOf<Job>()
    private var isStartingServer = false
    private var isServerRunning = false
    private var lastConnectAttemptTime = 0L

    init {
        val dao = AppDatabase.getDatabase(application).restaurantDao()
        repository = POSRepository(dao)

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastReset = prefs.getString("last_stock_reset_date", "")
        if (lastReset != todayStr) {
            viewModelScope.launch {
                repository.resetDailyStockLimits()
                prefs.edit().putString("last_stock_reset_date", todayStr).apply()
            }
        }

        allTables = combine(_deviceRole, repository.allTables, _remoteTables) { role, local, remote ->
            if (role == DeviceRole.SERVER) local else remote
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allCategories = combine(_deviceRole, repository.allCategories, _remoteCategories) { role, local, remote ->
            if (role == DeviceRole.SERVER) local else remote
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allMenuItems = combine(_deviceRole, repository.allMenuItems, _remoteMenuItems) { role, local, remote ->
            if (role == DeviceRole.SERVER) local else remote
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activeKitchenTickets = combine(_deviceRole, repository.activeKitchenTickets, _remoteKitchenTickets) { role, local, remote ->
            val raw = if (role == DeviceRole.SERVER) local else remote
            raw.groupBy { it.tableId }
                .values
                .map { ticketGroup -> ticketGroup.maxByOrNull { it.createdAt } ?: ticketGroup.first() }
                .sortedBy { it.createdAt }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        dailyReport = repository.getDailyReportFlow(_lastDayCloseTime).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DailyReport()
        )

        selectedTable = combine(allTables, _selectedTableId) { tables, id ->
            tables.find { it.id == id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        currentBill = selectedTable.flatMapLatest { table ->
            val billId = table?.currentBillId
            if (billId != null) {
                if (_deviceRole.value == DeviceRole.SERVER) {
                    repository.getBillFlow(billId)
                } else {
                    flowOf(
                        OrderBill(
                            id = billId,
                            tableId = table.id,
                            tableName = table.name,
                            totalAmount = table.currentTotalAmount,
                            paidAmount = table.currentPaidAmount
                        )
                    )
                }
            } else {
                flowOf(null)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        currentOrderItems = combine(selectedTable, _deviceRole) { table, role ->
            Pair(table?.currentBillId, role)
        }.flatMapLatest { (billId, role) ->
            if (billId == null) {
                flowOf(emptyList())
            } else if (role == DeviceRole.SERVER) {
                repository.getOrderItemsForBill(billId)
            } else {
                combine(_remoteActiveItems, flowOf(billId)) { remoteItems, id ->
                    remoteItems.filter { it.billId == id }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        currentPayments = selectedTable.flatMapLatest { table ->
            val billId = table?.currentBillId
            if (billId != null && _deviceRole.value == DeviceRole.SERVER) {
                repository.getPaymentsForBill(billId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        filteredMenuItems = combine(allMenuItems, _selectedCategoryId, _menuSearchQuery) { items, catId, query ->
            items.filter { item ->
                val matchesCategory = catId == 0 || item.categoryId == catId
                val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
                matchesCategory && matchesQuery && item.isAvailable
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            refreshDetailedReport()
        }

        initNetworking()
    }

    private fun initNetworking() {
        stopNetworking()
        nsdHelper = PosNsdHelper(getApplication())

        when (_deviceRole.value) {
            DeviceRole.SERVER -> startServer()
            DeviceRole.WAITER, DeviceRole.KITCHEN -> {
                startClientDiscovery()
                val knownHost = _resolvedServerHost.value ?: prefs.getString("last_server_host", null)
                if (!knownHost.isNullOrBlank()) {
                    connectClientToHost(knownHost, 8887)
                }
                startAutoReconnectLoop()
            }
        }
    }

    private fun startServer(retryCount: Int = 0) {
        if (isStartingServer && retryCount == 0) return
        isStartingServer = true

        serverBroadcastJobs.forEach { it.cancel() }
        serverBroadcastJobs.clear()

        try {
            server?.stop(500)
        } catch (_: Exception) {}
        server = null

        // UDP Beacon Broadcaster'ı anında başlat
        nsdHelper?.startBeaconBroadcaster(8887)

        try {
            server = LocalPosServer(
                port = 8887,
                onServerStarted = {
                    isStartingServer = false
                    isServerRunning = true
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                    nsdHelper?.registerServer(8887)
                    Log.d("POSViewModel", "LocalPosServer started and registered on port 8887")
                },
                onServerError = { ex ->
                    Log.e("POSViewModel", "Local server error/bind failure: ${ex?.message}, retryCount: $retryCount")
                    isStartingServer = false
                    isServerRunning = false
                    if (retryCount < 5) {
                        viewModelScope.launch {
                            delay(1200L)
                            startServer(retryCount + 1)
                        }
                    } else {
                        _connectionStatus.value = ConnectionStatus.ERROR
                    }
                },
                onClientHandshake = { socket, packet ->
                    viewModelScope.launch {
                        val activeItems = repository.activeOrderItems.firstOrNull() ?: emptyList()
                        val isKit = (server?.isAnyKitchenClientConnected() == true) || (_deviceRole.value == DeviceRole.KITCHEN)
                        _isKitchenConnected.value = isKit
                        server?.broadcastKitchenStatus(isKit)

                        val tables = if (allTables.value.isNotEmpty()) allTables.value else repository.allTables.firstOrNull() ?: emptyList()
                        val categories = if (allCategories.value.isNotEmpty()) allCategories.value else repository.allCategories.firstOrNull() ?: emptyList()
                        val menuItems = if (allMenuItems.value.isNotEmpty()) allMenuItems.value else repository.allMenuItems.firstOrNull() ?: emptyList()
                        val tickets = if (activeKitchenTickets.value.isNotEmpty()) activeKitchenTickets.value else repository.activeKitchenTickets.firstOrNull() ?: emptyList()

                        val payload = FullSyncPayload(
                            restaurantName = _restaurantName.value,
                            tables = tables,
                            categories = categories,
                            menuItems = menuItems,
                            activeTickets = tickets,
                            activeOrderItems = activeItems,
                            tableLocks = _activeTableLocks.value.values.toList(),
                            isKitchenActive = isKit
                        )
                        server?.sendPacket(
                            socket,
                            NetworkPacket(action = "FULL_SYNC", payload = Gson().toJson(payload))
                        )
                    }
                },
                onAddOrderReceived = { addOrderPayload ->
                    viewModelScope.launch {
                        if (addOrderPayload.onlySendKitchen && addOrderPayload.items.isEmpty()) {
                            repository.deleteKitchenTicketsForTable(addOrderPayload.tableId)
                            _snackbarMessage.value = "${addOrderPayload.tableName} mutfak siparişi KDS'den kaldırıldı"
                            return@launch
                        }

                        if (!addOrderPayload.onlySendKitchen) {
                            val menuItemsList = repository.getAllMenuItemsSync()
                            for (item in addOrderPayload.items) {
                                val matchedItem = menuItemsList.find {
                                    (item.menuItemId != 0 && it.id == item.menuItemId) || it.name == item.itemName
                                } ?: MenuItem(name = item.itemName, price = 0.0, categoryId = 1)
                                for (q in 1..item.quantity) {
                                    repository.addMenuItemToBill(
                                        tableId = addOrderPayload.tableId,
                                        menuItem = matchedItem,
                                        categoryName = item.categoryName,
                                        note = item.note
                                    )
                                }
                            }
                        }
                        val activeItems = repository.getActiveOrderItemsForTable(addOrderPayload.tableId)
                        val dtoList = if (activeItems.isNotEmpty()) {
                            activeItems.map {
                                KitchenTicketItemDto(
                                    id = it.id,
                                    menuItemId = it.menuItemId,
                                    itemName = it.itemName,
                                    categoryName = it.categoryName,
                                    quantity = it.quantity,
                                    note = it.note
                                )
                            }
                        } else {
                            addOrderPayload.items
                        }

                        repository.createKitchenTicket(
                            tableId = addOrderPayload.tableId,
                            tableName = addOrderPayload.tableName,
                            waiterName = addOrderPayload.waiterName,
                            items = dtoList
                        )
                        _snackbarMessage.value = "${addOrderPayload.tableName} için yeni sipariş alındı"
                    }
                },
                onModifyOrderItemReceived = { modPayload ->
                    viewModelScope.launch {
                        when (modPayload.modifyAction) {
                            "DECREMENT" -> repository.decrementOrderItemById(modPayload.tableId, modPayload.itemId)
                            "REMOVE" -> repository.removeOrderItemById(modPayload.tableId, modPayload.itemId)
                            "UPDATE_NOTE" -> repository.updateOrderItemNoteById(modPayload.itemId, modPayload.note)
                        }
                    }
                },
                onUpdateTicketReceived = { updatePayload ->
                    viewModelScope.launch {
                        repository.updateKitchenTicketStatus(updatePayload.ticketId, updatePayload.status)
                        if (updatePayload.status == "READY") {
                            val ticket = repository.getKitchenTicketById(updatePayload.ticketId)
                            if (ticket != null) {
                                val alert = KitchenReadyAlertPayload(
                                    ticketId = ticket.ticketId,
                                    tableId = ticket.tableId,
                                    tableName = ticket.tableName
                                )
                                server?.broadcastKitchenReadyAlert(alert)
                                playReadyChime()
                                _snackbarMessage.value = "🔔 ${ticket.tableName} siparişi mutfakta HAZIR!"
                            }
                        }
                    }
                },
                onLockTableReceived = { payload, socket ->
                    serverClientTableLocks.getOrPut(socket) { mutableSetOf() }.add(payload.tableId)
                    val map = _activeTableLocks.value.toMutableMap()
                    map[payload.tableId] = TableLockEntry(payload.tableId, payload.deviceId, payload.deviceName)
                    _activeTableLocks.value = map
                    server?.broadcastTableLocks(map.values.toList())
                },
                onUnlockTableReceived = { payload, socket ->
                    serverClientTableLocks[socket]?.remove(payload.tableId)
                    val map = _activeTableLocks.value.toMutableMap()
                    if (map[payload.tableId]?.deviceId == payload.deviceId) {
                        map.remove(payload.tableId)
                        _activeTableLocks.value = map
                        server?.broadcastTableLocks(map.values.toList())
                    }
                },
                onClientDisconnected = { socket ->
                    val lockedTables = serverClientTableLocks.remove(socket)
                    if (!lockedTables.isNullOrEmpty()) {
                        val map = _activeTableLocks.value.toMutableMap()
                        for (tid in lockedTables) {
                            map.remove(tid)
                        }
                        _activeTableLocks.value = map
                        server?.broadcastTableLocks(map.values.toList())
                    }
                    val isKit = (server?.isAnyKitchenClientConnected() == true) || (_deviceRole.value == DeviceRole.KITCHEN)
                    _isKitchenConnected.value = isKit
                    server?.broadcastKitchenStatus(isKit)
                }
            ).apply {
                start()
            }

            serverBroadcastJobs.add(
                viewModelScope.launch {
                    server?.connectedClientCount?.collect { count ->
                        _connectedClientCount.value = count
                    }
                }
            )

            serverBroadcastJobs.add(
                viewModelScope.launch {
                    repository.allTables.collect { tables ->
                        server?.broadcastTables(tables)
                    }
                }
            )

            serverBroadcastJobs.add(
                viewModelScope.launch {
                    repository.activeKitchenTickets.collect { tickets ->
                        server?.broadcastTickets(tickets)
                    }
                }
            )

            serverBroadcastJobs.add(
                viewModelScope.launch {
                    repository.activeOrderItems.collect { items ->
                        server?.broadcastActiveItems(items)
                    }
                }
            )

            serverBroadcastJobs.add(
                viewModelScope.launch {
                    combine(repository.allCategories, repository.allMenuItems, _restaurantName) { cats, items, rName ->
                        MenuSyncPayload(categories = cats, menuItems = items, restaurantName = rName)
                    }.collect { menuPayload ->
                        server?.broadcastMenuSync(menuPayload)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("POSViewModel", "Failed to start local server, retryCount: $retryCount", e)
            isStartingServer = false
            isServerRunning = false
            if (retryCount < 5) {
                viewModelScope.launch {
                    delay(1200L)
                    startServer(retryCount + 1)
                }
            } else {
                _connectionStatus.value = ConnectionStatus.ERROR
            }
        }
    }

    private fun startClientDiscovery() {
        if (_connectionStatus.value == ConnectionStatus.DISCONNECTED || _connectionStatus.value == ConnectionStatus.ERROR) {
            _connectionStatus.value = ConnectionStatus.SEARCHING
        }
        nsdHelper?.startDiscovery { host, port ->
            if (_connectionStatus.value != ConnectionStatus.CONNECTED || _resolvedServerHost.value != host) {
                _resolvedServerHost.value = host
                connectClientToHost(host, port)
            }
        }
    }

    fun connectClientToHost(host: String, port: Int = 8887) {
        if (client?.isOpen == true && _resolvedServerHost.value == host && _connectionStatus.value == ConnectionStatus.CONNECTED) {
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastConnectAttemptTime < 3500L && _connectionStatus.value == ConnectionStatus.CONNECTING) {
            return
        }
        lastConnectAttemptTime = now

        try {
            try {
                client?.close()
            } catch (_: Exception) {}
            client = null

            _connectionStatus.value = ConnectionStatus.CONNECTING
            _resolvedServerHost.value = host
            prefs.edit().putString("last_server_host", host).apply()

            val uri = URI("ws://$host:$port")
            client = LocalPosClient(
                serverUri = uri,
                role = _deviceRole.value,
                deviceName = deviceName,
                deviceId = deviceId,
                onFullSyncReceived = { fullSync ->
                    _restaurantName.value = fullSync.restaurantName
                    _remoteTables.value = fullSync.tables
                    _remoteCategories.value = fullSync.categories
                    _remoteMenuItems.value = fullSync.menuItems
                    _remoteKitchenTickets.value = fullSync.activeTickets
                    _remoteActiveItems.value = fullSync.activeOrderItems
                    _activeTableLocks.value = fullSync.tableLocks.associateBy { it.tableId }
                    _isKitchenConnected.value = fullSync.isKitchenActive
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                },
                onTablesUpdateReceived = { tables ->
                    _remoteTables.value = tables
                },
                onKitchenTicketsUpdateReceived = { tickets ->
                    _remoteKitchenTickets.value = tickets
                },
                onActiveItemsUpdateReceived = { items ->
                    _remoteActiveItems.value = items
                },
                onTableLocksUpdateReceived = { locks ->
                    _activeTableLocks.value = locks.associateBy { it.tableId }
                },
                onKitchenStatusUpdateReceived = { isKit ->
                    _isKitchenConnected.value = isKit
                },
                onMenuSyncReceived = { menuSync ->
                    _remoteCategories.value = menuSync.categories
                    _remoteMenuItems.value = menuSync.menuItems
                    if (menuSync.restaurantName.isNotBlank()) {
                        _restaurantName.value = menuSync.restaurantName
                    }
                },
                onKitchenReadyAlertReceived = { alert ->
                    if (_deviceRole.value != DeviceRole.KITCHEN) {
                        playReadyChime()
                        _snackbarMessage.value = "🔔 ${alert.tableName} siparişi mutfakta HAZIR!"
                    }
                }
            ).apply {
                connect()
            }

            clientStatusJob?.cancel()
            clientStatusJob = viewModelScope.launch {
                client?.connectionStatus?.collect { status ->
                    _connectionStatus.value = status
                }
            }
        } catch (e: Exception) {
            Log.e("POSViewModel", "Failed to connect client to $host:$port", e)
            _connectionStatus.value = ConnectionStatus.ERROR
        }
    }

    private fun startAutoReconnectLoop() {
        autoReconnectJob?.cancel()
        autoReconnectJob = viewModelScope.launch {
            while (isActive) {
                delay(2500L)
                val role = _deviceRole.value
                if (role == DeviceRole.WAITER || role == DeviceRole.KITCHEN) {
                    val isConnected = client?.isOpen == true && _connectionStatus.value == ConnectionStatus.CONNECTED
                    val isConnecting = _connectionStatus.value == ConnectionStatus.CONNECTING &&
                        (System.currentTimeMillis() - lastConnectAttemptTime < 3500L)

                    if (!isConnected && !isConnecting) {
                        val targetHost = _resolvedServerHost.value ?: prefs.getString("last_server_host", null)
                        if (!targetHost.isNullOrBlank()) {
                            connectClientToHost(targetHost, 8887)
                        } else {
                            if (_connectionStatus.value != ConnectionStatus.SEARCHING) {
                                _connectionStatus.value = ConnectionStatus.SEARCHING
                            }
                        }
                    }
                }
            }
        }
    }

    fun onAppResumed() {
        when (_deviceRole.value) {
            DeviceRole.SERVER -> {
                if (!isServerRunning && !isStartingServer) {
                    startServer()
                }
            }
            DeviceRole.WAITER, DeviceRole.KITCHEN -> {
                if (client?.isOpen != true || _connectionStatus.value != ConnectionStatus.CONNECTED) {
                    val host = _resolvedServerHost.value ?: prefs.getString("last_server_host", null)
                    if (!host.isNullOrBlank()) {
                        connectClientToHost(host, 8887)
                    }
                    startClientDiscovery()
                    startAutoReconnectLoop()
                }
            }
        }
    }

    fun retryConnection() {
        val host = _resolvedServerHost.value ?: prefs.getString("last_server_host", null)
        if (!host.isNullOrBlank()) {
            connectClientToHost(host, 8887)
        }
        initNetworking()
    }

    fun switchDeviceRole(newRole: DeviceRole) {
        prefs.edit().putString("device_role", newRole.name).apply()
        _deviceRole.value = newRole
        _isKitchenConnected.value = (newRole == DeviceRole.KITCHEN)
        _activeTableLocks.value = emptyMap()
        _currentScreen.value = if (newRole == DeviceRole.KITCHEN) AppScreen.KITCHEN else AppScreen.TABLES
        _snackbarMessage.value = "Cihaz modu '${newRole.title}' olarak ayarlandı"
        initNetworking()
    }

    fun connectManualIp(ip: String) {
        connectClientToHost(ip, 8887)
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.MANAGEMENT) {
            refreshDetailedReport()
        }
    }

    fun selectReportPeriod(period: ReportPeriod) {
        _selectedReportPeriod.value = period
        refreshDetailedReport(period)
    }

    fun refreshDetailedReport(period: ReportPeriod = _selectedReportPeriod.value) {
        val lastClose = prefs.getLong("last_day_close_time", 0L)
        viewModelScope.launch {
            _detailedReport.value = repository.getDetailedReport(period, lastClose)
        }
    }

    fun closeDay() {
        val now = System.currentTimeMillis()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
        prefs.edit()
            .putLong("last_day_close_time", now)
            .putString("last_stock_reset_date", todayStr)
            .apply()
        _lastDayCloseTime.value = now
        viewModelScope.launch {
            repository.resetDailyStockLimits()
            refreshDetailedReport()
            _snackbarMessage.value = "Gün başarıyla kapatıldı! Günlük stok kotaları tazelendi ve Z-raporu devredildi."
        }
    }

    fun selectZone(zone: String) {
        _selectedZone.value = zone
    }

    fun selectTable(tableId: Int?): Boolean {
        if (tableId == null) {
            val currentId = _selectedTableId.value
            if (currentId != null) {
                releaseTableLock(currentId)
            }
            _selectedTableId.value = null
            _menuSearchQuery.value = ""
            return true
        }

        val currentLock = _activeTableLocks.value[tableId]
        if (currentLock != null && currentLock.deviceId != deviceId) {
            _snackbarMessage.value = "'${currentLock.deviceName}' cihazında masa açık!"
            return false
        }

        acquireTableLock(tableId)
        _selectedTableId.value = tableId
        _menuSearchQuery.value = ""
        return true
    }

    fun forceSelectTable(tableId: Int) {
        acquireTableLock(tableId)
        _selectedTableId.value = tableId
        _menuSearchQuery.value = ""
        _snackbarMessage.value = "Masa kilidi devralındı"
    }

    fun onAppBackgrounded() {
        val currentId = _selectedTableId.value
        if (currentId != null) {
            releaseTableLock(currentId)
            _selectedTableId.value = null
        }
    }

    fun acquireTableLock(tableId: Int) {
        val entry = TableLockEntry(tableId = tableId, deviceId = deviceId, deviceName = deviceName)
        val updated = _activeTableLocks.value.toMutableMap()
        updated[tableId] = entry
        _activeTableLocks.value = updated
        if (_deviceRole.value == DeviceRole.SERVER) {
            server?.broadcastTableLocks(updated.values.toList())
        } else {
            client?.sendLockTable(tableId, deviceId, deviceName)
        }
    }

    fun releaseTableLock(tableId: Int) {
        val currentLock = _activeTableLocks.value[tableId]
        if (currentLock?.deviceId == deviceId) {
            val updated = _activeTableLocks.value.toMutableMap()
            updated.remove(tableId)
            _activeTableLocks.value = updated
            if (_deviceRole.value == DeviceRole.SERVER) {
                server?.broadcastTableLocks(updated.values.toList())
            } else {
                client?.sendUnlockTable(tableId, deviceId, deviceName)
            }
        }
    }

    fun selectCategory(categoryId: Int) {
        _selectedCategoryId.value = categoryId
    }

    fun updateSearchQuery(query: String) {
        _menuSearchQuery.value = query
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun addItemToCurrentTable(menuItem: MenuItem, note: String = "") {
        val tableId = _selectedTableId.value ?: return
        val categoryName = allCategories.value.find { it.id == menuItem.categoryId }?.name ?: "Genel"

        if (menuItem.hasDailyLimit && menuItem.remainingStock <= 0) {
            _snackbarMessage.value = "'${menuItem.name}' için günlük porsiyon tükendi!"
            return
        }

        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                val ok = repository.addMenuItemToBill(tableId, menuItem, categoryName, note)
                if (!ok) {
                    _snackbarMessage.value = "'${menuItem.name}' için günlük porsiyon tükendi!"
                }
            }
        } else {
            val ticketItem = KitchenTicketItemDto(
                menuItemId = menuItem.id,
                itemName = menuItem.name,
                categoryName = categoryName,
                quantity = 1,
                note = note
            )
            val payload = AddOrderPayload(
                tableId = tableId,
                tableName = selectedTable.value?.name ?: "Masa $tableId",
                waiterName = "Garson",
                items = listOf(ticketItem),
                onlySendKitchen = false
            )
            client?.sendAddOrder(payload)
            _snackbarMessage.value = "'${menuItem.name}' mutfağa ve masaya eklendi"
        }
    }

    fun sendOrderToKitchen(tableId: Int) {
        if (!_isKitchenConnected.value) {
            return
        }
        val table = allTables.value.find { it.id == tableId } ?: return
        viewModelScope.launch {
            val items = currentOrderItems.value.ifEmpty {
                repository.getOrderItemsForBill(table.currentBillId ?: 0L).firstOrNull() ?: emptyList()
            }
            if (items.isNotEmpty()) {
                val dtoList = items.map {
                    KitchenTicketItemDto(
                        id = it.id,
                        menuItemId = it.menuItemId,
                        itemName = it.itemName,
                        categoryName = it.categoryName,
                        quantity = it.quantity,
                        note = it.note
                    )
                }
                if (_deviceRole.value == DeviceRole.SERVER) {
                    repository.createKitchenTicket(
                        tableId = table.id,
                        tableName = table.name,
                        waiterName = "Kasa",
                        items = dtoList
                    )
                } else {
                    val payload = AddOrderPayload(
                        tableId = table.id,
                        tableName = table.name,
                        waiterName = "Garson",
                        items = dtoList,
                        onlySendKitchen = true
                    )
                    client?.sendAddOrder(payload)
                }
                _snackbarMessage.value = "${table.name} siparişi mutfak ekranına iletildi!"
            } else {
                // Masada hiç ürün kalmadıysa KDS'deki açık fişi iptal et / sil
                if (_deviceRole.value == DeviceRole.SERVER) {
                    repository.deleteKitchenTicketsForTable(table.id)
                } else {
                    val payload = AddOrderPayload(
                        tableId = table.id,
                        tableName = table.name,
                        waiterName = "Garson",
                        items = emptyList(),
                        onlySendKitchen = true
                    )
                    client?.sendAddOrder(payload)
                }
                _snackbarMessage.value = "${table.name} mutfak siparişi KDS'den kaldırıldı!"
            }
        }
    }

    fun cancelKitchenTicketForTable(tableId: Int) {
        val table = allTables.value.find { it.id == tableId } ?: return
        viewModelScope.launch {
            if (_deviceRole.value == DeviceRole.SERVER) {
                repository.deleteKitchenTicketsForTable(tableId)
            } else {
                val payload = AddOrderPayload(
                    tableId = tableId,
                    tableName = table.name,
                    waiterName = "Garson",
                    items = emptyList(),
                    onlySendKitchen = true
                )
                client?.sendAddOrder(payload)
            }
            _snackbarMessage.value = "${table.name} mutfak siparişi KDS'den kaldırıldı!"
        }
    }

    fun updateKitchenTicketStatus(ticketId: String, status: String) {
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.updateKitchenTicketStatus(ticketId, status)
                if (status == "READY") {
                    val ticket = repository.getKitchenTicketById(ticketId)
                    if (ticket != null) {
                        val alert = KitchenReadyAlertPayload(
                            ticketId = ticket.ticketId,
                            tableId = ticket.tableId,
                            tableName = ticket.tableName
                        )
                        server?.broadcastKitchenReadyAlert(alert)
                        playReadyChime()
                        _snackbarMessage.value = "🔔 ${ticket.tableName} siparişi mutfakta HAZIR!"
                    }
                }
            }
        } else {
            client?.sendUpdateTicketStatus(ticketId, status)
        }
    }

    private fun playReadyChime() {
        viewModelScope.launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 300)
                delay(320L)
                toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT, 400)
                delay(450L)
                toneGen.release()
            } catch (_: Exception) {}
        }
    }

    fun incrementItem(item: OrderItem) {
        val tableId = _selectedTableId.value ?: return
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                val ok = repository.incrementOrderItem(tableId, item)
                if (!ok) {
                    _snackbarMessage.value = "'${item.itemName}' için günlük porsiyon tükendi!"
                }
            }
        } else {
            val matchedMenu = allMenuItems.value.find { it.id == item.menuItemId }
                ?: MenuItem(id = item.menuItemId, name = item.itemName, price = item.unitPrice, categoryId = 1)
            addItemToCurrentTable(matchedMenu, item.note)
        }
    }

    fun decrementItem(item: OrderItem) {
        val tableId = _selectedTableId.value ?: return
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.decrementOrderItem(tableId, item)
            }
        } else {
            client?.sendModifyOrderItem(
                ModifyOrderItemPayload(
                    tableId = tableId,
                    itemId = item.id,
                    modifyAction = "DECREMENT"
                )
            )
        }
    }

    fun removeItem(item: OrderItem) {
        val tableId = _selectedTableId.value ?: return
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.removeOrderItem(tableId, item)
            }
        } else {
            client?.sendModifyOrderItem(
                ModifyOrderItemPayload(
                    tableId = tableId,
                    itemId = item.id,
                    modifyAction = "REMOVE"
                )
            )
        }
    }

    fun updateItemNote(item: OrderItem, note: String) {
        val tableId = _selectedTableId.value ?: 0
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.updateOrderItemNote(item, note)
            }
        } else {
            client?.sendModifyOrderItem(
                ModifyOrderItemPayload(
                    tableId = tableId,
                    itemId = item.id,
                    modifyAction = "UPDATE_NOTE",
                    note = note
                )
            )
        }
    }

    fun applyDiscount(discountAmount: Double) {
        val tableId = _selectedTableId.value ?: return
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.applyDiscountToBill(tableId, discountAmount)
                _snackbarMessage.value = if (discountAmount > 0) {
                    "₺${"%.2f".format(discountAmount)} indirim uygulandı"
                } else {
                    "İndirim kaldırıldı"
                }
            }
        }
    }

    fun makePayment(
        amount: Double,
        method: String,
        note: String = "",
        closeTableIfPaid: Boolean = true,
        paidItemQuantities: Map<Long, Int> = emptyMap(),
        onTableClosed: () -> Unit = {}
    ) {
        val tableId = _selectedTableId.value ?: return
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                val closed = repository.processPayment(
                    tableId = tableId,
                    amount = amount,
                    paymentMethod = method,
                    note = note,
                    closeTableIfPaid = closeTableIfPaid,
                    paidItemQuantities = paidItemQuantities
                )
                refreshDetailedReport()
                if (closed) {
                    _snackbarMessage.value = "Hesap kapatıldı (₺${"%.2f".format(amount)} - $method)"
                    onTableClosed()
                } else {
                    _snackbarMessage.value = "Ara ödeme alındı: ₺${"%.2f".format(amount)} ($method)"
                }
            }
        }
    }

    fun refundPayment(payment: PaymentRecord) {
        val tableId = _selectedTableId.value ?: return
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.refundPayment(tableId, payment)
                refreshDetailedReport()
                _snackbarMessage.value = "₺${"%.2f".format(payment.amount)} tutarındaki ara ödeme geri alındı"
            }
        }
    }

    fun togglePaymentMethod(payment: PaymentRecord) {
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.togglePaymentMethod(payment)
                refreshDetailedReport()
                val nextMethod = if (payment.paymentMethod == "NAKİT") "KART" else "NAKİT"
                _snackbarMessage.value = "Ödeme yöntemi '$nextMethod' olarak değiştirildi"
            }
        }
    }

    fun cancelTableOrder(onComplete: () -> Unit = {}) {
        val tableId = _selectedTableId.value ?: return
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.cancelAndClearTable(tableId)
                releaseTableLock(tableId)
                _selectedTableId.value = null
                _snackbarMessage.value = "Masa adisyonu iptal edildi"
                onComplete()
            }
        }
    }

    fun transferCurrentTable(targetTableId: Int, onComplete: () -> Unit = {}) {
        val fromId = _selectedTableId.value ?: return
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.transferTable(fromId, targetTableId)
                releaseTableLock(fromId)
                acquireTableLock(targetTableId)
                _selectedTableId.value = targetTableId
                _snackbarMessage.value = "Masa başarıyla aktarıldı"
                onComplete()
            }
        }
    }

    fun transferSelectedItemsToTable(
        targetTableId: Int,
        selectedQuantities: Map<Long, Int>,
        onComplete: () -> Unit = {}
    ) {
        val fromId = _selectedTableId.value ?: return
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.transferSelectedItems(fromId, targetTableId, selectedQuantities)
                _snackbarMessage.value = "Seçilen ürünler diğer masaya aktarıldı"
                onComplete()
            }
        }
    }

    fun setManagementPin(pin: String) {
        val cleanPin = pin.trim()
        prefs.edit().putString("management_pin", cleanPin).apply()
        _managementPin.value = cleanPin
        _snackbarMessage.value = if (cleanPin.isEmpty()) {
            "Yönetim paneli PIN kilidi kaldırıldı"
        } else {
            "Yönetim paneli PIN kodu güncellendi"
        }
    }

    fun exportMenuBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportMenuJson()
            onResult(json)
        }
    }

    fun importMenuBackup(json: String) {
        viewModelScope.launch {
            val ok = repository.importMenuJson(json)
            if (ok) {
                _snackbarMessage.value = "Menü yedeği başarıyla geri yüklendi!"
            } else {
                _snackbarMessage.value = "Geçersiz yedek formatı!"
            }
        }
    }

    fun addCustomProduct(
        categoryId: Int,
        name: String,
        price: Double,
        description: String,
        hasDailyLimit: Boolean = false,
        dailyLimit: Int = 0
    ) {
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.addCustomMenuItem(categoryId, name, price, description, hasDailyLimit, dailyLimit)
                _snackbarMessage.value = "'$name' menüye eklendi"
            }
        }
    }

    fun updateProduct(item: MenuItem) {
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.updateMenuItem(item)
                _snackbarMessage.value = "'${item.name}' güncellendi"
            }
        }
    }

    fun deleteProduct(item: MenuItem) {
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.deleteMenuItem(item)
                _snackbarMessage.value = "'${item.name}' silindi"
            }
        }
    }

    fun addCategory(name: String, iconName: String = "dinner") {
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.addMenuCategory(name, iconName)
                _snackbarMessage.value = "'$name' kategorisi eklendi"
            }
        }
    }

    fun updateCategory(category: MenuCategory) {
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.updateMenuCategory(category)
                _snackbarMessage.value = "'${category.name}' kategorisi güncellendi"
            }
        }
    }

    fun deleteCategory(category: MenuCategory) {
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.deleteMenuCategory(category)
                _snackbarMessage.value = "'${category.name}' kategorisi silindi"
            }
        }
    }

    fun addTable(name: String, zone: String, capacity: Int) {
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.addNewTable(name, zone, capacity)
                _snackbarMessage.value = "'$name' ($zone) eklendi"
            }
        }
    }

    fun updateTable(table: RestaurantTable, name: String, zone: String, capacity: Int) {
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                repository.updateTableInfo(table, name, zone, capacity)
                _snackbarMessage.value = "'$name' güncellendi"
            }
        }
    }

    fun deleteTable(table: RestaurantTable) {
        if (_deviceRole.value == DeviceRole.SERVER) {
            viewModelScope.launch {
                val deleted = repository.deleteTableIfEmpty(table)
                if (deleted) {
                    _snackbarMessage.value = "'${table.name}' silindi"
                } else {
                    _snackbarMessage.value = "Açık adisyonu olan masa silinemez!"
                }
            }
        }
    }

    fun updateRestaurantInfo(name: String) {
        val cleanName = name.trim().ifBlank { "Xubopos" }
        prefs.edit()
            .putString("restaurant_name", cleanName)
            .remove("restaurant_subtitle")
            .apply()
        _restaurantName.value = cleanName
        _snackbarMessage.value = "İşletme adı '$cleanName' olarak güncellendi"
    }

    fun checkForUpdates(currentVersion: String) {
        viewModelScope.launch {
            _updateStatus.value = UpdateStatus.Checking
            val result = AppUpdateManager.checkForUpdates(currentVersion)
            result.onSuccess { info ->
                if (info.isUpdateAvailable) {
                    _updateStatus.value = UpdateStatus.UpdateAvailable(info)
                } else {
                    _updateStatus.value = UpdateStatus.UpToDate
                }
            }.onFailure { error ->
                _updateStatus.value = UpdateStatus.Error(error.message ?: "Güncelleme denetlenirken hata oluştu")
            }
        }
    }

    fun downloadAndInstallUpdate(context: Context, downloadUrl: String) {
        viewModelScope.launch {
            _updateStatus.value = UpdateStatus.Downloading(0, 0L, 0L)
            val result = AppUpdateManager.downloadApk(context, downloadUrl) { percent, downloaded, total ->
                _updateStatus.value = UpdateStatus.Downloading(percent, downloaded, total)
            }
            result.onSuccess { apkFile ->
                _updateStatus.value = UpdateStatus.ReadyToInstall(apkFile)
                AppUpdateManager.installApk(context, apkFile)
            }.onFailure { error ->
                _updateStatus.value = UpdateStatus.Error(error.message ?: "APK indirilirken hata oluştu")
            }
        }
    }

    fun installDownloadedApk(context: Context, apkFile: File) {
        AppUpdateManager.installApk(context, apkFile)
    }

    fun resetUpdateStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }

    private fun stopNetworking() {
        isStartingServer = false
        isServerRunning = false

        serverBroadcastJobs.forEach { it.cancel() }
        serverBroadcastJobs.clear()

        autoReconnectJob?.cancel()
        autoReconnectJob = null

        clientStatusJob?.cancel()
        clientStatusJob = null

        try {
            server?.stop(500)
        } catch (_: Exception) {}
        server = null

        try {
            client?.close()
        } catch (_: Exception) {}
        client = null

        try {
            nsdHelper?.stop()
        } catch (_: Exception) {}
        nsdHelper = null
    }

    override fun onCleared() {
        super.onCleared()
        stopNetworking()
    }
}
