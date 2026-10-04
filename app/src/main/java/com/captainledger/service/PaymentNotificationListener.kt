package com.captainledger.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
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

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Extract payment amount matching e.g. Rs. 150 or INR 250.00
    private val paymentAmountPattern = Pattern.compile("(?i)(?:rs\\.?|inr)\\s*([\\d,]+(?:\\.\\d{1,2})?)")

    private val paymentPackages = setOf(
        "com.google.android.apps.n2p",
        "com.phonepe.app",
        "net.one97.paytm",
        "in.org.npci.upiapp",
        "com.whatsapp",
        "com.icicibank.pockets",
        "com.sbi.upi"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        val extras = sbn.notification?.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()

        val fullContent = "$title $text $bigText"

        val isPaymentApp = paymentPackages.contains(packageName) ||
                packageName.contains("pay", ignoreCase = true) ||
                packageName.contains("upi", ignoreCase = true)

        val isCreditEvent = fullContent.contains("received", ignoreCase = true) ||
                fullContent.contains("credited", ignoreCase = true) ||
                fullContent.contains("added", ignoreCase = true) ||
                fullContent.contains("sent you", ignoreCase = true) ||
                fullContent.contains("payment from", ignoreCase = true)

        if (isPaymentApp && isCreditEvent) {
            val matcher = paymentAmountPattern.matcher(fullContent)
            if (matcher.find()) {
                val rawAmountStr = matcher.group(1)?.replace(",", "")
                val amount = rawAmountStr?.toDoubleOrNull()

                if (amount != null && amount > 0) {
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
            else -> "UPI Auto"
        }
    }

    companion object {
        private const val TAG = "PaymentNotifListener"
    }
}
