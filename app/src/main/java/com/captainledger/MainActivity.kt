package com.captainledger

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.captainledger.ui.dashboard.DashboardScreen
import com.captainledger.ui.dashboard.DashboardViewModel
import com.captainledger.ui.debug.NotificationDebugActivity
import com.captainledger.ui.theme.CaptainLedgerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CaptainLedgerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onRequestNotificationListenerPermission = { openNotificationListenerSettings() },
                        onOpenDebugScreen = {
                            val intent = Intent(this, NotificationDebugActivity::class.java)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkNotificationListenerPermission()
    }

    private fun checkNotificationListenerPermission() {
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        val isEnabled = enabledListeners?.contains(packageName) == true
        dashboardViewModel.setNotificationListenerStatus(isEnabled)
    }

    private fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }
}
