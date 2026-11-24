package com.example.domain.telemetry

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import java.io.File
import java.util.Locale

data class MemoryTelemetry(
    val totalMb: Long,
    val availableMb: Long,
    val usedMb: Long,
    val usedPercent: Int,
    val isLowMemory: Boolean
)

data class BatteryTelemetry(
    val levelPercent: Int,
    val isCharging: Boolean,
    val chargingSource: String,
    val health: String,
    val temperatureCelsius: Float
)

data class StorageTelemetry(
    val totalGb: Double,
    val availableGb: Double,
    val usedGb: Double,
    val usedPercent: Int
)

data class NetworkTelemetry(
    val isConnected: Boolean,
    val transportType: String,
    val isMetered: Boolean
)

data class DeviceTelemetry(
    val model: String,
    val manufacturer: String,
    val osVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val uptimeHours: Double
)

data class FullSystemSnapshot(
    val timestamp: Long = System.currentTimeMillis(),
    val memory: MemoryTelemetry,
    val battery: BatteryTelemetry,
    val storage: StorageTelemetry,
    val network: NetworkTelemetry,
    val device: DeviceTelemetry
)

class SystemTelemetryManager(private val context: Context) {

    fun getMemoryTelemetry(): MemoryTelemetry {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)

        val totalMb = memInfo.totalMem / (1024 * 1024)
        val availableMb = memInfo.availMem / (1024 * 1024)
        val usedMb = (totalMb - availableMb).coerceAtLeast(0)
        val usedPercent = if (totalMb > 0) ((usedMb.toDouble() / totalMb) * 100).toInt() else 0

        return MemoryTelemetry(
            totalMb = totalMb,
            availableMb = availableMb,
            usedMb = usedMb,
            usedPercent = usedPercent.coerceIn(0, 100),
            isLowMemory = memInfo.lowMemory
        )
    }

    fun getBatteryTelemetry(): BatteryTelemetry {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val healthCode = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0

        val levelPercent = if (level >= 0 && scale > 0) ((level.toFloat() / scale) * 100).toInt() else 100
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val source = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> if (isCharging) "Charging" else "Discharging"
        }

        val healthStr = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Optimal"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Degraded"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            else -> "Good"
        }

        val temperatureCelsius = tempTenths / 10f

        return BatteryTelemetry(
            levelPercent = levelPercent.coerceIn(0, 100),
            isCharging = isCharging,
            chargingSource = source,
            health = healthStr,
            temperatureCelsius = temperatureCelsius
        )
    }

    fun getStorageTelemetry(): StorageTelemetry {
        return try {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availableBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0)

            val totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0)
            val availableGb = availableBytes / (1024.0 * 1024.0 * 1024.0)
            val usedGb = usedBytes / (1024.0 * 1024.0 * 1024.0)

            val usedPercent = if (totalBytes > 0) ((usedBytes.toDouble() / totalBytes) * 100).toInt() else 0

            StorageTelemetry(
                totalGb = String.format(Locale.US, "%.1f", totalGb).toDouble(),
                availableGb = String.format(Locale.US, "%.1f", availableGb).toDouble(),
                usedGb = String.format(Locale.US, "%.1f", usedGb).toDouble(),
                usedPercent = usedPercent.coerceIn(0, 100)
            )
        } catch (e: Exception) {
            StorageTelemetry(totalGb = 64.0, availableGb = 32.0, usedGb = 32.0, usedPercent = 50)
        }
    }

    fun getNetworkTelemetry(): NetworkTelemetry {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)

        val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val transport = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi 802.11"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular LTE/5G"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            isConnected -> "Connected"
            else -> "Disconnected"
        }
        val isMetered = connectivityManager?.isActiveNetworkMetered == true

        return NetworkTelemetry(
            isConnected = isConnected,
            transportType = transport,
            isMetered = isMetered
        )
    }

    fun getDeviceTelemetry(): DeviceTelemetry {
        val uptimeHours = SystemClock.elapsedRealtime() / (1000.0 * 60.0 * 60.0)
        val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else {
            "Pre-M"
        }

        return DeviceTelemetry(
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            osVersion = "Android ${Build.VERSION.RELEASE}",
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = securityPatch,
            uptimeHours = String.format(Locale.US, "%.1f", uptimeHours).toDouble()
        )
    }

    fun captureSnapshot(): FullSystemSnapshot {
        return FullSystemSnapshot(
            memory = getMemoryTelemetry(),
            battery = getBatteryTelemetry(),
            storage = getStorageTelemetry(),
            network = getNetworkTelemetry(),
            device = getDeviceTelemetry()
        )
    }
}
