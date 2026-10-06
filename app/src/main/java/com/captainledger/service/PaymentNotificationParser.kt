package com.captainledger.service

import com.captainledger.data.model.TransactionType
import java.util.regex.Pattern

data class ParsedNotificationResult(
    val amount: Double?,
    val type: TransactionType?,
    val platform: String,
    val isTargetApp: Boolean,
    val isParsedSuccessfully: Boolean
)

object PaymentNotificationParser {

    private val targetPackages = setOf(
        "com.google.android.apps.n2p",
        "com.google.android.apps.nbu.paisa.user",
        "com.phonepe.app",
        "net.one97.paytm",
        "in.org.npci.upiapp",
        "com.whatsapp",
        "com.whatsapp.w4b",
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
        "com.truecaller",
        "com.captainledger"
    )

    private val paymentAmountPattern = Pattern.compile(
        "(?i)(?:[+\\-]\\s*)?(?:₹|rs\\.?|inr|rs:?|inr:?)[\\s\\u00A0\\u202F]*([\\d,]+(?:\\.\\d{1,2})?)|(?:sent|paid|debited|transferred|spent|received|credited)\\s+([\\d,]+(?:\\.\\d{1,2})?)|([\\d,]+(?:\\.\\d{1,2})?)\\s+(?:sent|paid|debited|transferred|spent|received|credited)|([+\\-])\\s*([\\d,]+(?:\\.\\d{1,2})?)"
    )

    private val balanceKeywords = listOf(
        "bal", "balance", "avail bal", "available balance", "a/c bal",
        "account balance", "clear bal", "tot bal", "total balance", "net bal"
    )

    fun isTargetApp(packageName: String): Boolean {
        val lower = packageName.lowercase()
        return targetPackages.contains(lower) ||
                lower.contains("pay") ||
                lower.contains("paisa") ||
                lower.contains("upi") ||
                lower.contains("bank") ||
                lower.contains("message") ||
                lower.contains("sms") ||
                lower.contains("mms") ||
                lower.contains("captainledger")
    }

    fun buildFullContent(
        title: String?,
        text: String?,
        bigText: String?,
        summaryText: String?
    ): String {
        val list = listOfNotNull(title, text, bigText, summaryText)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        return list.joinToString(" ")
    }

    fun determineTransactionType(content: String): TransactionType? {
        val lower = content.lowercase()

        val creditKeywords = listOf(
            "credited", "received", "added", "sent you", "sent to your",
            "deposited", "payment from", "cashback", "refund", "got ₹",
            "got rs", "transfer from", "upi transfer from", "paid you"
        )

        val debitKeywords = listOf(
            "debited", "paid to", "spent", "withdrawn",
            "sent to", "paid ₹", "paid rs", "transfer to", "upi transfer to",
            "sent"
        )

        val isExplicitCredit = creditKeywords.any { lower.contains(it) }
        val isExplicitDebit = debitKeywords.any { keyword ->
            when (keyword) {
                "sent to" -> lower.contains("sent to") && !lower.contains("sent to your")
                "sent" -> lower.contains("sent") && !lower.contains("sent you") && !lower.contains("sent to your") && !lower.contains("sent by")
                else -> lower.contains(keyword)
            }
        }

        val hasPlusSign = content.contains("+")
        val hasMinusSign = content.contains("-") && !content.contains("a/c") && !content.contains("account")

        return when {
            isExplicitCredit && !isExplicitDebit -> TransactionType.INCOME
            isExplicitDebit && !isExplicitCredit -> TransactionType.EXPENSE
            isExplicitCredit && isExplicitDebit -> {
                val firstCredit = creditKeywords.map { lower.indexOf(it) }.filter { it >= 0 }.minOrNull() ?: Int.MAX_VALUE
                val firstDebit = debitKeywords.map { lower.indexOf(it) }.filter { it >= 0 }.minOrNull() ?: Int.MAX_VALUE

                if (firstCredit < firstDebit) TransactionType.INCOME
                else if (firstDebit < firstCredit) TransactionType.EXPENSE
                else TransactionType.INCOME
            }
            hasPlusSign && !hasMinusSign -> TransactionType.INCOME
            hasMinusSign && !hasPlusSign -> TransactionType.EXPENSE
            lower.contains("from") && !lower.contains("to") -> TransactionType.INCOME
            lower.contains("to") && !lower.contains("from") -> TransactionType.EXPENSE
            else -> null
        }
    }

    fun extractAmount(content: String): Double? {
        val matcher = paymentAmountPattern.matcher(content)
        while (matcher.find()) {
            val startPos = matcher.start()
            val precedingText = content.substring(0.coerceAtLeast(startPos - 35), startPos).lowercase()

            val isBalanceAmount = balanceKeywords.any { keyword -> precedingText.contains(keyword) }
            if (isBalanceAmount) continue

            val rawAmountStr = (matcher.group(1) ?: matcher.group(2) ?: matcher.group(3) ?: matcher.group(5))?.replace(",", "")
            val amount = rawAmountStr?.toDoubleOrNull()
            if (amount != null && amount > 0) {
                return amount
            }
        }
        return null
    }

    fun detectPlatformName(packageName: String, content: String): String {
        val lowerContent = content.lowercase()
        val lowerPkg = packageName.lowercase()

        return when {
            lowerContent.contains("rapido") -> "Rapido"
            lowerContent.contains("swiggy") -> "Swiggy"
            lowerContent.contains("uber") -> "Uber"
            lowerContent.contains("zomato") -> "Zomato"
            lowerPkg.contains("phonepe") -> "PhonePe"
            lowerPkg.contains("google") || lowerPkg.contains("n2p") || lowerPkg.contains("paisa") -> "GPay"
            lowerPkg.contains("paytm") -> "Paytm"
            lowerPkg.contains("truecaller") -> "Truecaller"
            lowerPkg.contains("whatsapp") -> "WhatsApp"
            else -> "UPI Auto"
        }
    }

    fun parseNotification(
        packageName: String,
        title: String?,
        text: String?,
        bigText: String?,
        summaryText: String?
    ): ParsedNotificationResult {
        val targetApp = isTargetApp(packageName)
        if (!targetApp) {
            return ParsedNotificationResult(
                amount = null,
                type = null,
                platform = "Unknown",
                isTargetApp = false,
                isParsedSuccessfully = false
            )
        }

        val fullContent = buildFullContent(title, text, bigText, summaryText)
        if (fullContent.isBlank()) {
            return ParsedNotificationResult(
                amount = null,
                type = null,
                platform = detectPlatformName(packageName, ""),
                isTargetApp = true,
                isParsedSuccessfully = false
            )
        }

        val type = determineTransactionType(fullContent)
        val amount = extractAmount(fullContent)
        val platform = detectPlatformName(packageName, fullContent)
        val success = type != null && amount != null && amount > 0

        return ParsedNotificationResult(
            amount = amount,
            type = type,
            platform = platform,
            isTargetApp = true,
            isParsedSuccessfully = success
        )
    }
}
