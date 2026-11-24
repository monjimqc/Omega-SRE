package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.security.SecurityPosture
import com.example.domain.telemetry.FullSystemSnapshot
import com.example.ui.components.CircularReliabilityGauge
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldReliable
import com.example.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    snapshot: FullSystemSnapshot?,
    securityPosture: SecurityPosture?,
    onRunProtocol: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Reliability Gauge
        val reliabilityScore = securityPosture?.overallScore ?: 95

        Spacer(modifier = Modifier.height(8.dp))
        CircularReliabilityGauge(score = reliabilityScore)
        Spacer(modifier = Modifier.height(16.dp))

        // Hero Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onRunProtocol,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("run_protocol_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Execute Protocol",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "RUN OMEGA PROTOCOL",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier
                    .height(52.dp)
                    .testTag("refresh_telemetry_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Telemetry")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Telemetry Cards Section
        Text(
            text = "HARDWARE & RUNTIME TELEMETRY",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // 1. RAM Card
        if (snapshot != null) {
            MetricCard(
                title = "SYSTEM RAM ALLOCATION",
                value = "${snapshot.memory.usedPercent}% USED",
                subtitle = "${snapshot.memory.usedMb} MB consumed of ${snapshot.memory.totalMb} MB total (${snapshot.memory.availableMb} MB free)",
                icon = Icons.Default.Memory,
                accentColor = if (snapshot.memory.usedPercent > 80) WarningAmber else CyberCyan,
                testTag = "metric_ram_card",
                trailingBadge = {
                    StatusBadge(
                        text = if (snapshot.memory.isLowMemory) "LOW MEMORY" else "STABLE",
                        color = if (snapshot.memory.isLowMemory) WarningAmber else EmeraldReliable
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Battery Card
            MetricCard(
                title = "BATTERY & POWER SUBSYSTEM",
                value = "${snapshot.battery.levelPercent}%",
                subtitle = "${snapshot.battery.chargingSource} • Health: ${snapshot.battery.health} • ${snapshot.battery.temperatureCelsius}°C",
                icon = Icons.Default.BatteryChargingFull,
                accentColor = if (snapshot.battery.isCharging) EmeraldReliable else CyberCyan,
                testTag = "metric_battery_card",
                trailingBadge = {
                    StatusBadge(
                        text = if (snapshot.battery.isCharging) "CHARGING" else "BATTERY",
                        color = if (snapshot.battery.isCharging) EmeraldReliable else CyberCyan
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Storage Card
            MetricCard(
                title = "FLASH STORAGE PARTITION",
                value = "${snapshot.storage.usedPercent}% ALLOCATED",
                subtitle = "${snapshot.storage.usedGb} GB used of ${snapshot.storage.totalGb} GB (${snapshot.storage.availableGb} GB remaining)",
                icon = Icons.Default.Storage,
                accentColor = CyberCyan,
                testTag = "metric_storage_card"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Network Card
            MetricCard(
                title = "NETWORK & SOCKET CONNECTIVITY",
                value = snapshot.network.transportType,
                subtitle = if (snapshot.network.isConnected) "Internet link active • ${if (snapshot.network.isMetered) "Metered link" else "Unmetered link"}" else "Offline / No link",
                icon = Icons.Default.Wifi,
                accentColor = if (snapshot.network.isConnected) EmeraldReliable else WarningAmber,
                testTag = "metric_network_card",
                trailingBadge = {
                    StatusBadge(
                        text = if (snapshot.network.isConnected) "ONLINE" else "OFFLINE",
                        color = if (snapshot.network.isConnected) EmeraldReliable else WarningAmber
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Host Platform Info
            MetricCard(
                title = "HOST ENVIRONMENT & KERNEL",
                value = "${snapshot.device.manufacturer} ${snapshot.device.model}",
                subtitle = "${snapshot.device.osVersion} (API ${snapshot.device.apiLevel}) • Security Patch: ${snapshot.device.securityPatch} • Uptime: ${snapshot.device.uptimeHours}h",
                icon = Icons.Default.Security,
                accentColor = MaterialTheme.colorScheme.primary,
                testTag = "metric_device_card"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
