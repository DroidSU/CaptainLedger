package com.captainledger.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.captainledger.data.local.NotificationLogDao
import com.captainledger.data.model.NotificationLog
import com.captainledger.data.model.PaymentMode
import com.captainledger.data.model.TransactionLog
import com.captainledger.data.model.TransactionType
import com.captainledger.data.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PaymentNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var repository: TransactionRepository

    @Inject
    lateinit var notificationLogDao: NotificationLogDao

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        val extras = sbn.notification?.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString().orEmpty()

        val parseResult = PaymentNotificationParser.parseNotification(
            packageName = packageName,
            title = title,
            text = text,
            bigText = bigText,
            summaryText = summaryText
        )

        if (!parseResult.isTargetApp) return

        val amount = parseResult.amount
        val type = parseResult.type

        if (parseResult.isParsedSuccessfully && amount != null && type != null) {
            val category = if (type == TransactionType.INCOME) "Auto UPI Earnings" else "Auto UPI Expenses"
            val platformName = parseResult.platform

            serviceScope.launch {
                try {
                    val isDuplicate = repository.isDuplicateTransaction(amount, type)
                    if (isDuplicate) {
                        Log.d(TAG, "Duplicate $type transaction detected: ₹$amount from $platformName. Skipping auto-log and ignoring notification log.")
                    } else {
                        val transaction = TransactionLog(
                            amount = amount,
                            type = type,
                            paymentMode = PaymentMode.UPI,
                            category = category,
                            platform = platformName,
                            timestamp = System.currentTimeMillis()
                        )
                        repository.addTransaction(transaction)
                        Log.d(TAG, "Auto-logged UPI $type: ₹$amount from $platformName")

                        val log = NotificationLog(
                            packageName = packageName,
                            title = title,
                            text = "$text $bigText".trim(),
                            timestamp = System.currentTimeMillis(),
                            isParsedSuccessfully = true,
                            extractedAmount = amount
                        )
                        notificationLogDao.insertNotificationLog(log)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing notification log", e)
                }
            }
        } else {
            serviceScope.launch {
                try {
                    val log = NotificationLog(
                        packageName = packageName,
                        title = title,
                        text = "$text $bigText".trim(),
                        timestamp = System.currentTimeMillis(),
                        isParsedSuccessfully = false,
                        extractedAmount = amount
                    )
                    notificationLogDao.insertNotificationLog(log)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing notification log", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "PaymentNotifListener"
    }
}
