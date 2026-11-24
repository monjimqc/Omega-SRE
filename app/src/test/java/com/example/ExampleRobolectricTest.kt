package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.protocol.OmegaProtocolEngine
import com.example.domain.security.SecurityPosture
import com.example.domain.telemetry.BatteryTelemetry
import com.example.domain.telemetry.DeviceTelemetry
import com.example.domain.telemetry.FullSystemSnapshot
import com.example.domain.telemetry.MemoryTelemetry
import com.example.domain.telemetry.NetworkTelemetry
import com.example.domain.telemetry.StorageTelemetry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Omega SRE", appName)
    }

    @Test
    fun `protocol engine generates deterministic verdict and valid sha256`() {
        val engine = OmegaProtocolEngine()

        val dummySnapshot = FullSystemSnapshot(
            memory = MemoryTelemetry(
                totalMb = 8192,
                availableMb = 4096,
                usedMb = 4096,
                usedPercent = 50,
                isLowMemory = false
            ),
            battery = BatteryTelemetry(
                levelPercent = 85,
                isCharging = false,
                chargingSource = "Battery",
                health = "Optimal",
                temperatureCelsius = 28.5f
            ),
            storage = StorageTelemetry(
                totalGb = 128.0,
                availableGb = 64.0,
                usedGb = 64.0,
                usedPercent = 50
            ),
            network = NetworkTelemetry(
                isConnected = true,
                transportType = "Wi-Fi",
                isMetered = false
            ),
            device = DeviceTelemetry(
                model = "Pixel 8",
                manufacturer = "Google",
                osVersion = "Android 14",
                apiLevel = 34,
                securityPatch = "2024-01-01",
                uptimeHours = 12.5
            )
        )

        val dummySecurity = SecurityPosture(
            overallScore = 100,
            statusLabel = "HARDENED",
            isRooted = false,
            isDebuggable = false,
            isAdbEnabled = false,
            isTestKeysBuild = false,
            findings = emptyList()
        )

        val report = engine.executeFullProtocol(dummySnapshot, dummySecurity)

        assertNotNull(report.runId)
        assertTrue(report.overallReliabilityScore >= 90)
        assertEquals("CLEAN", report.verdict)
        assertEquals(64, report.sha256Fingerprint.length) // 256 bits = 64 hex chars
        assertTrue(report.stages.isNotEmpty())
    }
}
