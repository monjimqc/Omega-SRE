package com.example.domain.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.provider.Settings
import java.io.File

data class SecurityFinding(
    val title: String,
    val severity: String, // P0, P1, P2, P3, P4, P5
    val isRisk: Boolean,
    val description: String,
    val remediation: String
)

data class SecurityPosture(
    val overallScore: Int, // 0 - 100
    val statusLabel: String, // HARDENED, ELEVATED_RISK, COMPROMISED
    val isRooted: Boolean,
    val isDebuggable: Boolean,
    val isAdbEnabled: Boolean,
    val isTestKeysBuild: Boolean,
    val findings: List<SecurityFinding>
)

class SecurityIntegrityChecker(private val context: Context) {

    fun evaluateSecurityPosture(): SecurityPosture {
        val findings = mutableListOf<SecurityFinding>()

        // 1. Root Check
        val rootPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        )
        val isRooted = rootPaths.any { path ->
            try {
                File(path).exists()
            } catch (_: Exception) {
                false
            }
        }
        findings.add(
            SecurityFinding(
                title = "Root Binary Detection",
                severity = if (isRooted) "P0" else "P5",
                isRisk = isRooted,
                description = if (isRooted) "Privileged su binaries detected on system path." else "No unauthorized root or su binaries detected.",
                remediation = if (isRooted) "Unroot device and re-lock bootloader to prevent unauthorized system modifications." else "Maintain locked bootloader."
            )
        )

        // 2. Build Tags Check
        val buildTags = Build.TAGS
        val isTestKeys = buildTags != null && buildTags.contains("test-keys")
        findings.add(
            SecurityFinding(
                title = "OS Build Signature",
                severity = if (isTestKeys) "P1" else "P5",
                isRisk = isTestKeys,
                description = if (isTestKeys) "OS image compiled with unofficial test-keys signature." else "OS image signed with official release-keys.",
                remediation = if (isTestKeys) "Flash OEM certified stock firmware image." else "Official firmware verified."
            )
        )

        // 3. Application Debuggable Flag
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        findings.add(
            SecurityFinding(
                title = "Application Debug Flag",
                severity = if (isDebuggable) "P2" else "P5",
                isRisk = isDebuggable,
                description = if (isDebuggable) "App running with android:debuggable enabled (Runtime inspection active)." else "App compiled in release mode; JDWP debugger attached disabled.",
                remediation = if (isDebuggable) "Compile release APK with minify and debug flags disabled for production deployment." else "Hardened runtime verified."
            )
        )

        // 4. ADB Enabled Check
        val isAdbEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (_: Exception) {
            false
        }
        findings.add(
            SecurityFinding(
                title = "USB Debugging (ADB Bridge)",
                severity = if (isAdbEnabled) "P3" else "P5",
                isRisk = isAdbEnabled,
                description = if (isAdbEnabled) "Android Debug Bridge (ADB) daemon active on host interface." else "ADB interface disabled.",
                remediation = if (isAdbEnabled) "Disable Developer Options > USB Debugging when in untrusted physical environments." else "Host perimeter secured."
            )
        )

        // Calculate score
        var score = 100
        if (isRooted) score -= 40
        if (isTestKeys) score -= 25
        if (isDebuggable) score -= 15
        if (isAdbEnabled) score -= 10
        score = score.coerceIn(0, 100)

        val statusLabel = when {
            score >= 85 -> "HARDENED"
            score >= 60 -> "ELEVATED_RISK"
            else -> "CRITICAL_ATTENTION"
        }

        return SecurityPosture(
            overallScore = score,
            statusLabel = statusLabel,
            isRooted = isRooted,
            isDebuggable = isDebuggable,
            isAdbEnabled = isAdbEnabled,
            isTestKeysBuild = isTestKeys,
            findings = findings
        )
    }
}
