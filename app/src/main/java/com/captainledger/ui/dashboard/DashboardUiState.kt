package com.captainledger.ui.dashboard

import com.captainledger.data.model.TimeFilter
import com.captainledger.data.model.TransactionLog
import com.captainledger.data.model.TransactionSummary

data class DashboardUiState(
    val summary: TransactionSummary = TransactionSummary(),
    val selectedFilter: TimeFilter = TimeFilter.TODAY,
    val transactions: List<TransactionLog> = emptyList(),
    val isLoading: Boolean = false,
    val isNotificationListenerEnabled: Boolean = false,
    val showAddBottomSheet: Boolean = false
)
