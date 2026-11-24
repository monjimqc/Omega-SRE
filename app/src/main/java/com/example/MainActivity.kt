package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.ProtocolScreen
import com.example.ui.screens.SecurityScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.OmegaSreViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: OmegaSreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OmegaSreApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmegaSreApp(
    viewModel: OmegaSreViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val auditHistory by viewModel.auditHistory.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Logo",
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshAll() },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("appbar_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh System State",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == NavigationTab.TELEMETRY,
                    onClick = { viewModel.selectTab(NavigationTab.TELEMETRY) },
                    icon = { Icon(Icons.Default.Memory, contentDescription = "Telemetry") },
                    label = { Text(stringResource(R.string.tab_dashboard)) },
                    modifier = Modifier.testTag("nav_tab_telemetry"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = uiState.currentTab == NavigationTab.SECURITY,
                    onClick = { viewModel.selectTab(NavigationTab.SECURITY) },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Security") },
                    label = { Text(stringResource(R.string.tab_security)) },
                    modifier = Modifier.testTag("nav_tab_security"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = uiState.currentTab == NavigationTab.PROTOCOL,
                    onClick = { viewModel.selectTab(NavigationTab.PROTOCOL) },
                    icon = { Icon(Icons.Default.PlayCircleOutline, contentDescription = "Protocol") },
                    label = { Text(stringResource(R.string.tab_protocol)) },
                    modifier = Modifier.testTag("nav_tab_protocol"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = uiState.currentTab == NavigationTab.LEDGER,
                    onClick = { viewModel.selectTab(NavigationTab.LEDGER) },
                    icon = { Icon(Icons.Default.History, contentDescription = "Ledger") },
                    label = { Text(stringResource(R.string.tab_ledger)) },
                    modifier = Modifier.testTag("nav_tab_ledger"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (uiState.currentTab) {
                NavigationTab.TELEMETRY -> DashboardScreen(
                    snapshot = uiState.snapshot,
                    securityPosture = uiState.securityPosture,
                    onRunProtocol = { viewModel.runOmegaProtocol() },
                    onRefresh = { viewModel.refreshAll() }
                )
                NavigationTab.SECURITY -> SecurityScreen(
                    securityPosture = uiState.securityPosture
                )
                NavigationTab.PROTOCOL -> ProtocolScreen(
                    isRunning = uiState.isRunningProtocol,
                    activeStage = uiState.activeProtocolStage,
                    latestReport = uiState.latestReport,
                    onExecuteProtocol = { viewModel.runOmegaProtocol() }
                )
                NavigationTab.LEDGER -> LedgerScreen(
                    auditList = auditHistory,
                    onDeleteRecord = { viewModel.removeRecord(it) },
                    onClearAll = { viewModel.clearLedger() },
                    onRunNewProtocol = { viewModel.runOmegaProtocol() }
                )
            }
        }
    }
}
