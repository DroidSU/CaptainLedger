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
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class PaymentNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var repository: TransactionRepository

    @Inject
    lateinit var notificationLogDao: NotificationLogDao

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val paymentAmountPattern = Pattern.compile("(?i)(?:₹|rs\\.?|inr)\\s*([\\d,]+(?:\\.\\d{1,2})?)")

    private val targetPackages = setOf(
        "com.google.android.apps.n2p",
        "com.phonepe.app",
        "net.one97.paytm",
        "in.org.npci.upiapp",
        "com.whatsapp",
        "com.icicibank.pockets",
        "com.sbi.upi",
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.android.mms",
        "com.hdfcbank.mobilebanking",
        "com.sbi.lotusintouch",
        "com.icicibank.mobilebanking",
        "com.axis.mobile",
        "com.kotak.mobilebanking",
        "com.truecaller"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        val extras = sbn.notification?.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString().orEmpty()

        val fullContent = "$title $text $bigText $summaryText".trim()
        if (fullContent.isBlank()) return

        val isTargetApp = targetPackages.contains(packageName) ||
                packageName.contains("pay", ignoreCase = true) ||
                packageName.contains("upi", ignoreCase = true) ||
                packageName.contains("bank", ignoreCase = true) ||
                packageName.contains("message", ignoreCase = true) ||
                packageName.contains("sms", ignoreCase = true)

        if (!isTargetApp) return

        val isCreditEvent = fullContent.contains("received", ignoreCase = true) ||
                fullContent.contains("credited", ignoreCase = true) ||
                fullContent.contains("added", ignoreCase = true) ||
                fullContent.contains("sent you", ignoreCase = true) ||
                fullContent.contains("deposited", ignoreCase = true) ||
                fullContent.contains("payment from", ignoreCase = true)

        val isDebitEvent = fullContent.contains("debited", ignoreCase = true) ||
                fullContent.contains("paid to", ignoreCase = true) ||
                fullContent.contains("sent to", ignoreCase = true) ||
                fullContent.contains("spent", ignoreCase = true)

        var extractedAmount: Double? = null
        var isParsed = false

        if (isCreditEvent && !isDebitEvent) {
            val matcher = paymentAmountPattern.matcher(fullContent)
            if (matcher.find()) {
                val rawAmountStr = matcher.group(1)?.replace(",", "")
                val amount = rawAmountStr?.toDoubleOrNull()

                if (amount != null && amount > 0) {
                    extractedAmount = amount
                    isParsed = true
                    val platformName = detectPlatformName(packageName, fullContent)

                    serviceScope.launch {
                        val transaction = TransactionLog(
                            amount = amount,
                            type = TransactionType.INCOME,
                            paymentMode = PaymentMode.UPI,
                            category = "Auto UPI Earnings",
                            platform = platformName,
                            timestamp = System.currentTimeMillis()
                        )
                        repository.addTransaction(transaction)
                        Log.d(TAG, "Auto-logged UPI Income: ₹$amount from $platformName")
                    }
                }
            }
        }

        serviceScope.launch {
            val log = NotificationLog(
                packageName = packageName,
                title = title,
                text = "$text $bigText".trim(),
                timestamp = System.currentTimeMillis(),
                isParsedSuccessfully = isParsed,
                extractedAmount = extractedAmount
            )
            notificationLogDao.insertNotificationLog(log)
        }
    }

    private fun detectPlatformName(packageName: String, content: String): String {
        return when {
            content.contains("Rapido", ignoreCase = true) -> "Rapido"
            content.contains("Swiggy", ignoreCase = true) -> "Swiggy"
            content.contains("Uber", ignoreCase = true) -> "Uber"
            content.contains("Zomato", ignoreCase = true) -> "Zomato"
            packageName.contains("phonepe", ignoreCase = true) -> "PhonePe"
            packageName.contains("google", ignoreCase = true) || packageName.contains("n2p", ignoreCase = true) -> "GPay"
            packageName.contains("paytm", ignoreCase = true) -> "Paytm"
            packageName.contains("truecaller", ignoreCase = true) -> "Truecaller"
            else -> "UPI Auto"
        }
    }

    companion object {
        private const val TAG = "PaymentNotifListener"
    }
}
