package com.captainledger.ui.debug

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.captainledger.data.model.NotificationLog
import com.captainledger.data.model.TransactionType
import com.captainledger.service.PaymentNotificationParser
import com.captainledger.ui.theme.CaptainLedgerTheme
import com.captainledger.ui.theme.FinancialColors
import com.captainledger.util.DateTimeUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDebugScreen(
    logs: List<NotificationLog>,
    onClearLogs: () -> Unit,
    onBack: () -> Unit,
    onSendMockNotification: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notification Inspector", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (logs.isNotEmpty()) {
                        IconButton(onClick = onClearLogs) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Logs"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            MockNotificationTriggers(onSendMock = onSendMockNotification)

            Text(
                text = "Live Intercepted Notifications (${logs.size})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No notifications intercepted yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Send a GPay/PhonePe or test notification to inspect live logs.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = logs, key = { it.id }) { log ->
                        NotificationLogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationLogItem(log: NotificationLog) {
    val timeStr = DateTimeUtils.formatDebugLogTimestamp(log.timestamp)
    val parseResult = PaymentNotificationParser.parseNotification(
        packageName = log.packageName,
        title = log.title,
        text = log.text,
        bigText = null,
        summaryText = null
    )
    val amount = parseResult.amount ?: log.extractedAmount
    val type = parseResult.type
    val isSuccessfullyParsed = parseResult.isParsedSuccessfully && amount != null && type != null

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = log.packageName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (log.title.isNotBlank()) {
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            Text(
                text = log.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isSuccessfullyParsed && type != null) {
                val isIncome = type == TransactionType.INCOME
                val badgeColor = if (isIncome) FinancialColors.success else FinancialColors.expense
                val badgeText = if (isIncome) {
                    String.format(Locale.getDefault(), "✓ Parsed Income: ₹%.2f", amount)
                } else {
                    String.format(Locale.getDefault(), "✓ Parsed Expense: ₹%.2f", amount)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "• Intercepted (Not Auto-Logged)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MockNotificationTriggers(
    onSendMock: (String, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "⚡ Test Mock Notifications (Verify Parsing)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onSendMock(
                            "₹500.00",
                            "You received ₹500.00 from Swiggy via Google Pay"
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("GPay Credit", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = {
                        onSendMock(
                            "₹5",
                            "UPI transfer to duttasinc Value 06 from Account 8714"
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("PhonePe Debit", style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onSendMock(
                            "HDFC Bank Alert",
                            "Rs 2,500.00 credited to A/C ending 1234. Avail Bal: Rs 45,000.00"
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Bank SMS Credit", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = {
                        onSendMock(
                            "System Update",
                            "Your app has been updated successfully."
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Unparseable", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationDebugScreenPreview() {
    CaptainLedgerTheme {
        NotificationDebugScreen(
            logs = listOf(
                NotificationLog(
                    id = 1,
                    packageName = "com.google.android.apps.nbu.paisa.user",
                    title = "Received ₹500.00",
                    text = "You received ₹500.00 from John Doe via Google Pay",
                    timestamp = System.currentTimeMillis(),
                    isParsedSuccessfully = true,
                    extractedAmount = 500.0
                ),
                NotificationLog(
                    id = 2,
                    packageName = "com.phonepe.app",
                    title = "Payment received",
                    text = "Received Rs. 1,200 from Jane Smith",
                    timestamp = System.currentTimeMillis() - 3600000,
                    isParsedSuccessfully = false,
                    extractedAmount = null
                )
            ),
            onClearLogs = {},
            onBack = {},
            onSendMockNotification = { _, _ -> }
        )
    }
}
