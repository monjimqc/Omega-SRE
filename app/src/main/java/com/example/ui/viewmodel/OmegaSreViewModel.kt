package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AuditRecord
import com.example.data.repository.AuditRepository
import com.example.domain.protocol.OmegaProtocolEngine
import com.example.domain.protocol.ProtocolExecutionReport
import com.example.domain.protocol.ProtocolStage
import com.example.domain.security.SecurityIntegrityChecker
import com.example.domain.security.SecurityPosture
import com.example.domain.telemetry.FullSystemSnapshot
import com.example.domain.telemetry.SystemTelemetryManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavigationTab {
    TELEMETRY,
    SECURITY,
    PROTOCOL,
    LEDGER
}

data class OmegaUiState(
    val currentTab: NavigationTab = NavigationTab.TELEMETRY,
    val snapshot: FullSystemSnapshot? = null,
    val securityPosture: SecurityPosture? = null,
    val isRunningProtocol: Boolean = false,
    val activeProtocolStage: ProtocolStage? = null,
    val latestReport: ProtocolExecutionReport? = null,
    val isRefreshing: Boolean = false,
    val statusMessage: String = "OMEGA SRE Engine Online"
)

class OmegaSreViewModel(application: Application) : AndroidViewModel(application) {

    private val telemetryManager = SystemTelemetryManager(application)
    private val securityChecker = SecurityIntegrityChecker(application)
    private val protocolEngine = OmegaProtocolEngine()
    private val repository = AuditRepository(AppDatabase.getInstance(application).auditDao())

    val auditHistory: StateFlow<List<AuditRecord>> = repository.allAudits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(OmegaUiState())
    val uiState: StateFlow<OmegaUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val snapshot = telemetryManager.captureSnapshot()
            val security = securityChecker.evaluateSecurityPosture()
            _uiState.value = _uiState.value.copy(
                snapshot = snapshot,
                securityPosture = security,
                isRefreshing = false,
                statusMessage = "Telemetry Updated at ${System.currentTimeMillis() % 100000}"
            )
        }
    }

    fun runOmegaProtocol() {
        if (_uiState.value.isRunningProtocol) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRunningProtocol = true,
                currentTab = NavigationTab.PROTOCOL
            )

            val currentSnapshot = telemetryManager.captureSnapshot()
            val currentSecurity = securityChecker.evaluateSecurityPosture()

            // Simulate deterministic stage transitions with high responsiveness
            for (stage in ProtocolStage.entries) {
                _uiState.value = _uiState.value.copy(
                    activeProtocolStage = stage,
                    statusMessage = "Protocol executing: ${stage.displayName}"
                )
                delay(120) // Snappy responsive visual update
            }

            val report = protocolEngine.executeFullProtocol(currentSnapshot, currentSecurity)

            // Persist to Room Database
            val record = AuditRecord(
                runId = report.runId,
                overallHealthScore = report.overallReliabilityScore,
                verdict = report.verdict,
                findingsCount = report.findingsSummary.size,
                memoryUsagePercent = currentSnapshot.memory.usedPercent,
                batteryLevel = currentSnapshot.battery.levelPercent,
                storageUsagePercent = currentSnapshot.storage.usedPercent,
                securityStatus = currentSecurity.statusLabel,
                detailsJson = report.rawJsonExport
            )
            repository.saveAudit(record)

            _uiState.value = _uiState.value.copy(
                isRunningProtocol = false,
                activeProtocolStage = null,
                latestReport = report,
                statusMessage = "Protocol complete: ${report.verdict} (Score: ${report.overallReliabilityScore})"
            )
        }
    }

    fun clearLedger() {
        viewModelScope.launch {
            repository.clearHistory()
            _uiState.value = _uiState.value.copy(statusMessage = "Evidence ledger purged.")
        }
    }

    fun removeRecord(record: AuditRecord) {
        viewModelScope.launch {
            repository.removeAudit(record)
        }
    }
}
