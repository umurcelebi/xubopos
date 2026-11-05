package com.xubopos.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket

class PosNsdHelper(private val context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private var multicastLock: WifiManager.MulticastLock? = null

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var isDiscovering = false
    private var isRegistered = false
    private var isResolving = false

    private val helperScope = CoroutineScope(Dispatchers.IO + Job())
    private var beaconBroadcastJob: Job? = null
    private var beaconListenJob: Job? = null
    private var periodicDiscoveryJob: Job? = null

    companion object {
        const val SERVICE_TYPE = "_xubopos._tcp."
        const val SERVICE_NAME = "Xubopos-Server"
        const val UDP_BEACON_PORT = 8888
        private const val TAG = "PosNsdHelper"
    }

    private fun acquireMulticastLock() {
        try {
            if (multicastLock == null) {
                multicastLock = wifiManager?.createMulticastLock("xubopos_nsd_lock")?.apply {
                    setReferenceCounted(false)
                }
            }
            multicastLock?.let {
                if (!it.isHeld) it.acquire()
            }
        } catch (e: Exception) {
            Log.e(TAG, "MulticastLock error", e)
        }
    }

    private fun releaseMulticastLock() {
        try {
            multicastLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "MulticastLock release error", e)
        }
    }

    fun registerServer(port: Int, onRegistered: () -> Unit = {}) {
        if (isRegistered) return
        acquireMulticastLock()

        val serviceInfo = NsdServiceInfo().apply {
            serviceName = SERVICE_NAME
            serviceType = SERVICE_TYPE
            this.port = port
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                isRegistered = true
                Log.d(TAG, "Server registered: ${serviceInfo.serviceName}")
                onRegistered()
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Server registration failed: $errorCode")
                isRegistered = false
                helperScope.launch {
                    delay(2000L)
                    if (!isRegistered) {
                        try {
                            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
                        } catch (_: Exception) {}
                    }
                }
            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                isRegistered = false
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                isRegistered = false
            }
        }

        try {
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register NSD service", e)
        }

        // UDP Broadcast Beacon başlat (Ağdaki tüm cihazlara 2 saniyede bir IP bildirimi yapar)
        startBeaconBroadcaster(port)
    }

    fun startDiscovery(onServerResolved: (host: String, port: Int) -> Unit) {
        acquireMulticastLock()

        if (!isDiscovering) {
            discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) {
                    isDiscovering = true
                    Log.d(TAG, "Discovery started for $regType")
                }

                override fun onServiceFound(service: NsdServiceInfo) {
                    Log.d(TAG, "Service found: ${service.serviceName}")
                    if (service.serviceType.contains("xubopos") || service.serviceName.contains("Xubopos")) {
                        resolveService(service, onServerResolved)
                    }
                }

                override fun onServiceLost(service: NsdServiceInfo) {
                    Log.d(TAG, "Service lost: ${service.serviceName}")
                }

                override fun onDiscoveryStopped(serviceType: String) {
                    isDiscovering = false
                    Log.d(TAG, "Discovery stopped")
                }

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    isDiscovering = false
                    Log.e(TAG, "Start discovery failed: $errorCode")
                    helperScope.launch {
                        delay(1500L)
                        if (!isDiscovering && discoveryListener != null) {
                            try {
                                nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
                            } catch (_: Exception) {}
                        }
                    }
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    isDiscovering = false
                    Log.e(TAG, "Stop discovery failed: $errorCode")
                }
            }

            try {
                nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start discovery", e)
            }
        }

        // 2. Katman: UDP Beacon Dinleyici (Sunucu varsa anında 1-2 saniye içinde yakalar)
        startBeaconListener(onServerResolved)

        // 3. Katman: Subnet Fast Probe (Alt ağdaki 8887 portunu paralel tarar)
        probeSubnet(onServerResolved)

        // Periyodik olarak (her 8 saniyede bir) subnet probe ve beacon listener'ı canlı tutar
        periodicDiscoveryJob?.cancel()
        periodicDiscoveryJob = helperScope.launch {
            while (isActive) {
                delay(8000L)
                probeSubnet(onServerResolved)
            }
        }
    }

    private fun resolveService(service: NsdServiceInfo, onResolved: (host: String, port: Int) -> Unit) {
        if (isResolving) return
        isResolving = true
        try {
            nsdManager.resolveService(
                service,
                object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.e(TAG, "Resolve failed: $errorCode")
                        isResolving = false
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        isResolving = false
                        val host = serviceInfo.host?.hostAddress
                        val port = serviceInfo.port
                        Log.d(TAG, "Service resolved via NSD: $host:$port")
                        if (!host.isNullOrBlank()) {
                            onResolved(host, port)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "resolveService exception", e)
            isResolving = false
        }
    }

    private fun startBeaconBroadcaster(serverPort: Int) {
        beaconBroadcastJob?.cancel()
        beaconBroadcastJob = helperScope.launch {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket().apply {
                    broadcast = true
                }
                val msg = "XUBOPOS_BEACON:$serverPort".toByteArray(Charsets.UTF_8)
                val generalBroadcast = InetAddress.getByName("255.255.255.255")

                while (isActive) {
                    try {
                        socket.send(DatagramPacket(msg, msg.size, generalBroadcast, UDP_BEACON_PORT))
                        val subnetBroadcasts = getAllBroadcastAddresses()
                        for (broadcastAddr in subnetBroadcasts) {
                            try {
                                socket.send(DatagramPacket(msg, msg.size, broadcastAddr, UDP_BEACON_PORT))
                            } catch (_: Exception) {}
                        }
                    } catch (_: Exception) {}
                    delay(2000L)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Beacon broadcaster error", e)
            } finally {
                socket?.close()
            }
        }
    }

    private fun startBeaconListener(onServerFound: (host: String, port: Int) -> Unit) {
        beaconListenJob?.cancel()
        beaconListenJob = helperScope.launch {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket(null).apply {
                    reuseAddress = true
                    broadcast = true
                    bind(InetSocketAddress(UDP_BEACON_PORT))
                }
                val buffer = ByteArray(256)
                val packet = DatagramPacket(buffer, buffer.size)

                while (isActive) {
                    try {
                        socket.receive(packet)
                        val text = String(packet.data, 0, packet.length, Charsets.UTF_8)
                        if (text.startsWith("XUBOPOS_BEACON")) {
                            val senderIp = packet.address?.hostAddress
                            val parts = text.split(":")
                            val sPort = if (parts.size >= 2) parts[1].toIntOrNull() ?: 8887 else 8887
                            if (!senderIp.isNullOrBlank()) {
                                Log.d(TAG, "Server resolved via UDP Beacon: $senderIp:$sPort")
                                onServerFound(senderIp, sPort)
                            }
                        }
                    } catch (e: Exception) {
                        if (!isActive) break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Beacon listener error", e)
            } finally {
                socket?.close()
            }
        }
    }

    fun probeSubnet(onServerFound: (host: String, port: Int) -> Unit) {
        val myIp = getLocalIpAddress() ?: return
        val prefix = myIp.substringBeforeLast(".") + "."
        helperScope.launch {
            (1..254).chunked(32).forEach { chunk ->
                if (!isActive) return@launch
                val jobs = chunk.map { i ->
                    async {
                        val testIp = "$prefix$i"
                        if (testIp != myIp) {
                            try {
                                Socket().use { s ->
                                    s.connect(InetSocketAddress(testIp, 8887), 250)
                                    Log.d(TAG, "Server found via Subnet Probe: $testIp:8887")
                                    onServerFound(testIp, 8887)
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
                jobs.awaitAll()
            }
        }
    }

    private fun getAllBroadcastAddresses(): List<InetAddress> {
        val broadcastList = mutableListOf<InetAddress>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return broadcastList
            for (intf in interfaces) {
                if (!intf.isUp || intf.isLoopback) continue
                for (intfAddr in intf.interfaceAddresses) {
                    val broadcast = intfAddr.broadcast
                    if (broadcast != null && broadcast is Inet4Address) {
                        broadcastList.add(broadcast)
                    }
                }
            }
        } catch (_: Exception) {}
        return broadcastList
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            for (intf in interfaces) {
                if (!intf.isUp || intf.isLoopback) continue
                val addrs = intf.inetAddresses ?: continue
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val ip = addr.hostAddress ?: continue
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                            return ip
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    fun stop() {
        beaconBroadcastJob?.cancel()
        beaconBroadcastJob = null

        beaconListenJob?.cancel()
        beaconListenJob = null

        periodicDiscoveryJob?.cancel()
        periodicDiscoveryJob = null

        try {
            if (isRegistered && registrationListener != null) {
                nsdManager.unregisterService(registrationListener)
                isRegistered = false
            }
        } catch (_: Exception) {}

        try {
            if (isDiscovering && discoveryListener != null) {
                nsdManager.stopServiceDiscovery(discoveryListener)
                isDiscovering = false
            }
        } catch (_: Exception) {}

        releaseMulticastLock()
    }
}
