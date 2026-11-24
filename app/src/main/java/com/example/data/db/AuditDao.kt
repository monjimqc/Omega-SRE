package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AuditRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_records ORDER BY timestamp DESC")
    fun getAllAudits(): Flow<List<AuditRecord>>

    @Query("SELECT * FROM audit_records ORDER BY timestamp DESC LIMIT 1")
    fun getLatestAudit(): Flow<AuditRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(record: AuditRecord): Long

    @Delete
    suspend fun deleteAudit(record: AuditRecord)

    @Query("DELETE FROM audit_records")
    suspend fun clearAllAudits()
}
