package com.example.ui.screens

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
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.security.SecurityFinding
import com.example.domain.security.SecurityPosture
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.EmeraldReliable
import com.example.ui.theme.WarningAmber

@Composable
fun SecurityScreen(
    securityPosture: SecurityPosture?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        val score = securityPosture?.overallScore ?: 100
        val status = securityPosture?.statusLabel ?: "HARDENED"

        // Overall Posture Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("security_posture_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                (if (score >= 80) EmeraldReliable else WarningAmber).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (score >= 80) Icons.Default.GppGood else Icons.Default.GppMaybe,
                            contentDescription = "Security Posture",
                            tint = if (score >= 80) EmeraldReliable else WarningAmber,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "DEVSECOPS POSTURE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = status,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (score >= 80) EmeraldReliable else WarningAmber
                        )
                    }
                }

                StatusBadge(
                    text = "$score / 100",
                    color = if (score >= 80) EmeraldReliable else WarningAmber
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "HARDENING AUDIT & INTEGRITY CHECKS",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val findings = securityPosture?.findings ?: emptyList()
        findings.forEach { finding ->
            SecurityFindingItem(finding = finding)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun SecurityFindingItem(finding: SecurityFinding) {
    val statusColor = if (finding.isRisk) WarningAmber else EmeraldReliable

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("security_check_${finding.title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (finding.isRisk) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = finding.title,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = finding.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                StatusBadge(text = finding.severity, color = statusColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = finding.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Remediation: ${finding.remediation}",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
