package com.captainledger.ui.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.captainledger.ui.theme.CaptainLedgerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationDebugActivity : ComponentActivity() {

    private val viewModel: NotificationDebugViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CaptainLedgerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val logs by viewModel.logs.collectAsState()

                    NotificationDebugScreen(
                        logs = logs,
                        onClearLogs = { viewModel.clearLogs() },
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}
