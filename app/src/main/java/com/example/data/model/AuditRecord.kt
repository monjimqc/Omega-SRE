package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_records")
data class AuditRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val runId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val overallHealthScore: Int, // 0 - 100
    val verdict: String, // CLEAN, FIXED_VERIFIED, WARNING, CRITICAL
    val findingsCount: Int,
    val memoryUsagePercent: Int,
    val batteryLevel: Int,
    val storageUsagePercent: Int,
    val securityStatus: String,
    val detailsJson: String
)
