package com.example.data.repository

import com.example.data.db.AuditDao
import com.example.data.model.AuditRecord
import kotlinx.coroutines.flow.Flow

class AuditRepository(private val auditDao: AuditDao) {
    val allAudits: Flow<List<AuditRecord>> = auditDao.getAllAudits()
    val latestAudit: Flow<AuditRecord?> = auditDao.getLatestAudit()

    suspend fun saveAudit(record: AuditRecord): Long = auditDao.insertAudit(record)

    suspend fun removeAudit(record: AuditRecord) = auditDao.deleteAudit(record)

    suspend fun clearHistory() = auditDao.clearAllAudits()
}
