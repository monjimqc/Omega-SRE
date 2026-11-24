package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.protocol.ProtocolExecutionReport
import com.example.domain.protocol.ProtocolStage
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldReliable
import com.example.ui.theme.WarningAmber

@Composable
fun ProtocolScreen(
    isRunning: Boolean,
    activeStage: ProtocolStage?,
    latestReport: ProtocolExecutionReport?,
    onExecuteProtocol: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Hero Trigger Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("protocol_control_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "OMEGA-AELR PROTOCOL v1.0",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Autonomous Error, Log, Root-Cause, Remediation & Verification Engine with cryptographic provenance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onExecuteProtocol,
                    enabled = !isRunning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("execute_protocol_action_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "EXECUTING: ${activeStage?.displayName ?: "STAGES"}",
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DISPATCH OMEGA-AELR AUDIT",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Live Execution Pipeline
        if (isRunning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AUTONOMOUS PIPELINE IN PROGRESS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ProtocolStage.entries.forEach { stage ->
                        val isCurrent = activeStage == stage
                        val isPassed = activeStage != null && stage.ordinal < activeStage.ordinal

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    isPassed -> Icons.Default.CheckCircle
                                    isCurrent -> Icons.Default.Sync
                                    else -> Icons.Default.HourglassEmpty
                                },
                                contentDescription = null,
                                tint = when {
                                    isPassed -> EmeraldReliable
                                    isCurrent -> CyberCyan
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stage.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                color = if (isCurrent) CyberCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Latest Report Results
        AnimatedVisibility(visible = latestReport != null) {
            latestReport?.let { report ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "AUTHORITATIVE VERIFICATION RESULT",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Verdict Summary Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("protocol_verdict_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "FINAL VERDICT",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = report.verdict,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = if (report.overallReliabilityScore >= 80) EmeraldReliable else WarningAmber
                                    )
                                }

                                StatusBadge(
                                    text = "SCORE: ${report.overallReliabilityScore}/100",
                                    color = if (report.overallReliabilityScore >= 80) EmeraldReliable else WarningAmber
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Run ID: ${report.runId} • Trace ID: ${report.traceId}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cryptographic Hash Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cryptographic_hash_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "SHA-256",
                                tint = CyberCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "CRYPTOGRAPHIC PROVENANCE (SHA-256)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan
                                )
                                Text(
                                    text = report.sha256Fingerprint,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Machine Report JSON Card + Copy Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("machine_report_json_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MACHINE REPORT (JSON)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Omega Report", report.rawJsonExport)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Report JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("copy_json_report_button")
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Copy JSON", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = report.rawJsonExport,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
