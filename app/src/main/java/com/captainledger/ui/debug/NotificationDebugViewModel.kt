package com.captainledger.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.captainledger.data.local.NotificationLogDao
import com.captainledger.data.model.NotificationLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationDebugViewModel @Inject constructor(
    private val notificationLogDao: NotificationLogDao
) : ViewModel() {

    val logs: StateFlow<List<NotificationLog>> = notificationLogDao.getAllNotificationLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearLogs() {
        viewModelScope.launch {
            notificationLogDao.clearAllLogs()
        }
    }
}
