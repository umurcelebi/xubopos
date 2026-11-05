package com.xubopos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xubopos.network.ConnectionStatus
import com.xubopos.network.DeviceRole
import com.xubopos.ui.theme.Amber100
import com.xubopos.ui.theme.Amber500
import com.xubopos.ui.theme.Amber600
import com.xubopos.ui.theme.Amber700
import com.xubopos.ui.theme.Emerald100
import com.xubopos.ui.theme.Emerald600
import com.xubopos.ui.theme.Emerald700
import com.xubopos.ui.theme.PureWhite
import com.xubopos.ui.theme.Sky100
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

@Composable
fun DeviceRoleDialog(
    currentRole: DeviceRole,
    connectionStatus: ConnectionStatus,
    serverHost: String?,
    connectedClientCount: Int,
    onDismiss: () -> Unit,
    onSelectRole: (DeviceRole) -> Unit,
    onConnectManualIp: (String) -> Unit
) {
    var selectedRole by remember(currentRole) { mutableStateOf(currentRole) }
    var manualIpInput by remember { mutableStateOf(serverHost ?: "") }
    var showManualIp by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(24.dp),
            color = PureWhite
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 580.dp

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isWide) 16.dp else 20.dp)
                ) {
                    // 1. BAŞLIK
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Slate900,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = Amber500,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Ağ & Cihaz Çalışma Modu",
                                    fontSize = if (isWide) 17.sp else 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate900
                                )
                                Text(
                                    text = "İnternetsiz yerel Wi-Fi ağı üzerinden çoklu cihaz",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. KAYDIRILABİLİR İÇERİK
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Slate100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Şu Anki Durum:",
                                        fontSize = 10.sp,
                                        color = Slate500
                                    )
                                    Text(
                                        text = "${currentRole.title} (${currentRole.badge})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Slate900
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (currentRole == DeviceRole.SERVER) Slate800 else Emerald100
                                ) {
                                    Text(
                                        text = if (currentRole == DeviceRole.SERVER) {
                                            "$connectedClientCount Bağlantı"
                                        } else {
                                            if (connectionStatus == ConnectionStatus.CONNECTED) "Bağlı" else "Aranıyor"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentRole == DeviceRole.SERVER) Amber500 else Emerald700,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Bu cihazın çalışma rolünü seçin:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (isWide) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RoleOptionCard(
                                    title = "1. Ana Kasa & Sunucu",
                                    badge = "KASA",
                                    description = "Ana veritabanını tutar, mDNS ile yayın yapar.",
                                    icon = Icons.Default.PointOfSale,
                                    isSelected = selectedRole == DeviceRole.SERVER,
                                    badgeBg = Amber100,
                                    badgeColor = Amber700,
                                    onClick = { selectedRole = DeviceRole.SERVER },
                                    modifier = Modifier.weight(1f)
                                )
                                RoleOptionCard(
                                    title = "2. Garson Terminali",
                                    badge = "GARSON",
                                    description = "Wi-Fi'da kasayı bulur, masadan sipariş alır.",
                                    icon = Icons.Default.Smartphone,
                                    isSelected = selectedRole == DeviceRole.WAITER,
                                    badgeBg = Sky100,
                                    badgeColor = Sky700,
                                    onClick = { selectedRole = DeviceRole.WAITER },
                                    modifier = Modifier.weight(1f)
                                )
                                RoleOptionCard(
                                    title = "3. Mutfak Takip (KDS)",
                                    badge = "MUTFAK",
                                    description = "Aşçı ve bar hazırlık ekranı, fişleri yönetir.",
                                    icon = Icons.Default.Restaurant,
                                    isSelected = selectedRole == DeviceRole.KITCHEN,
                                    badgeBg = Emerald100,
                                    badgeColor = Emerald700,
                                    onClick = { selectedRole = DeviceRole.KITCHEN },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                RoleOptionCard(
                                    title = "1. Ana Kasa & Sunucu",
                                    badge = "KASA",
                                    description = "Ana veritabanını tutar. Yerel ağda mDNS ile 'Xubopos-Server' yayını yapar. Tüm adisyon ve tahsilat yetkisine sahiptir.",
                                    icon = Icons.Default.PointOfSale,
                                    isSelected = selectedRole == DeviceRole.SERVER,
                                    badgeBg = Amber100,
                                    badgeColor = Amber700,
                                    onClick = { selectedRole = DeviceRole.SERVER }
                                )

                                RoleOptionCard(
                                    title = "2. Garson Terminali",
                                    badge = "GARSON",
                                    description = "Ana kasayı Wi-Fi'da isimden otomatik bulur. Masaları görür, sipariş alır ve mutfağa iletir.",
                                    icon = Icons.Default.Smartphone,
                                    isSelected = selectedRole == DeviceRole.WAITER,
                                    badgeBg = Sky100,
                                    badgeColor = Sky700,
                                    onClick = { selectedRole = DeviceRole.WAITER }
                                )

                                RoleOptionCard(
                                    title = "3. Mutfak Takip Ekranı (KDS)",
                                    badge = "MUTFAK",
                                    description = "Aşçı ve bar hazırlık ekranı. Yeni siparişler sesli uyarıyla düşer, hazırlık aşamaları dokunarak yönetilir.",
                                    icon = Icons.Default.Restaurant,
                                    isSelected = selectedRole == DeviceRole.KITCHEN,
                                    badgeBg = Emerald100,
                                    badgeColor = Emerald700,
                                    onClick = { selectedRole = DeviceRole.KITCHEN }
                                )
                            }
                        }

                        if (selectedRole != DeviceRole.SERVER) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "mDNS otomatik eşleşme aktiftir (IP gerekmez)",
                                    fontSize = 11.sp,
                                    color = Emerald700,
                                    fontWeight = FontWeight.SemiBold
                                )
                                OutlinedButton(
                                    onClick = { showManualIp = !showManualIp },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (showManualIp) "Kapat" else "Manuel IP",
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (showManualIp) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = manualIpInput,
                                        onValueChange = { manualIpInput = it },
                                        label = { Text("Sunucu IP'si (Örn: 192.168.1.45)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            if (manualIpInput.isNotBlank()) {
                                                onConnectManualIp(manualIpInput.trim())
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                                    ) {
                                        Text("Bağlan")
                                    }
                                }
                            }
                        }
                    }

                    // 3. TABANA SABİTLENMİŞ ONAY BUTONU
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Slate200)
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            onSelectRole(selectedRole)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                    ) {
                        Text(
                            text = if (selectedRole == currentRole) "Bu Modda Devam Et" else "Seçilen Role Geç (${selectedRole.badge})",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    badge: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeBg: androidx.compose.ui.graphics.Color,
    badgeColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Slate50 else PureWhite
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) Slate900 else Slate200
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) Slate900 else Slate100,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) PureWhite else Slate700,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Slate500,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Slate900,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
