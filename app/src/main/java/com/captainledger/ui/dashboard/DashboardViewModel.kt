package com.captainledger.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.captainledger.data.model.PaymentMode
import com.captainledger.data.model.TimeFilter
import com.captainledger.data.model.TransactionLog
import com.captainledger.data.model.TransactionType
import com.captainledger.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TimeFilter.TODAY)
    private val _isNotificationListenerEnabled = MutableStateFlow(false)
    private val _showAddBottomSheet = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> = combine(
        _selectedFilter,
        _isNotificationListenerEnabled,
        _showAddBottomSheet
    ) { filter, listenerEnabled, showSheet ->
        Triple(filter, listenerEnabled, showSheet)
    }.flatMapLatest { (filter, listenerEnabled, showSheet) ->
        combine(
            repository.getSummary(filter),
            repository.getTransactions(filter)
        ) { summary, transactions ->
            DashboardUiState(
                summary = summary,
                selectedFilter = filter,
                transactions = transactions,
                isNotificationListenerEnabled = listenerEnabled,
                showAddBottomSheet = showSheet
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun onFilterSelected(filter: TimeFilter) {
        _selectedFilter.value = filter
    }

    fun setNotificationListenerStatus(enabled: Boolean) {
        _isNotificationListenerEnabled.value = enabled
    }

    fun onAddTransactionClick() {
        _showAddBottomSheet.value = true
    }

    fun onDismissBottomSheet() {
        _showAddBottomSheet.value = false
    }

    fun saveTransaction(
        amount: Double,
        type: TransactionType,
        paymentMode: PaymentMode,
        category: String,
        platform: String
    ) {
        viewModelScope.launch {
            val log = TransactionLog(
                amount = amount,
                type = type,
                paymentMode = paymentMode,
                category = category,
                platform = platform
            )
            repository.addTransaction(log)
            _showAddBottomSheet.value = false
        }
    }

    fun deleteTransaction(transaction: TransactionLog) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}
