package com.captainledger.service

import com.captainledger.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentNotificationParserTest {

    @Test
    fun `test Google Pay India package name is identified as target app`() {
        val gpayPackage = "com.google.android.apps.nbu.paisa.user"
        assertTrue(PaymentNotificationParser.isTargetApp(gpayPackage))
    }

    @Test
    fun `test Google Pay received money parsing`() {
        val result = PaymentNotificationParser.parseNotification(
            packageName = "com.google.android.apps.nbu.paisa.user",
            title = "Received ₹500.00",
            text = "You received ₹500.00 from Swiggy via Google Pay",
            bigText = null,
            summaryText = null
        )

        assertTrue(result.isTargetApp)
        assertTrue(result.isParsedSuccessfully)
        assertEquals(500.0, result.amount)
        assertEquals(TransactionType.INCOME, result.type)
        assertEquals("Swiggy", result.platform)
    }

    @Test
    fun `test PhonePe received payment with non-breaking space`() {
        val textWithNbsp = "Received Rs.\u00A01,250.00 from John Doe"
        val result = PaymentNotificationParser.parseNotification(
            packageName = "com.phonepe.app",
            title = "Payment received",
            text = textWithNbsp,
            bigText = null,
            summaryText = null
        )

        assertTrue(result.isParsedSuccessfully)
        assertEquals(1250.0, result.amount)
        assertEquals(TransactionType.INCOME, result.type)
        assertEquals("PhonePe", result.platform)
    }

    @Test
    fun `test Bank SMS with account balance and transaction amount`() {
        val title = "HDFC Bank Alert"
        val text = "Rs 2,500.00 credited to A/C ending 1234. Avail Bal: Rs 45,000.00"

        val result = PaymentNotificationParser.parseNotification(
            packageName = "com.google.android.apps.messaging",
            title = title,
            text = text,
            bigText = null,
            summaryText = null
        )

        assertTrue(result.isParsedSuccessfully)
        assertEquals(2500.0, result.amount)
        assertEquals(TransactionType.INCOME, result.type)
    }

    @Test
    fun `test Sent to your account phrase is parsed as credit income`() {
        val text = "₹1,000.00 sent to your bank account by Rapido"
        val result = PaymentNotificationParser.parseNotification(
            packageName = "com.android.mms",
            title = "Money Received",
            text = text,
            bigText = null,
            summaryText = null
        )

        assertTrue(result.isParsedSuccessfully)
        assertEquals(1000.0, result.amount)
        assertEquals(TransactionType.INCOME, result.type)
        assertEquals("Rapido", result.platform)
    }

    @Test
    fun `test Paid to merchant phrase is parsed as debit expense`() {
        val text = "Paid ₹350.00 to Zomato"
        val result = PaymentNotificationParser.parseNotification(
            packageName = "net.one97.paytm",
            title = "Payment Successful",
            text = text,
            bigText = null,
            summaryText = null
        )

        assertTrue(result.isParsedSuccessfully)
        assertEquals(350.0, result.amount)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("Zomato", result.platform)
    }

    @Test
    fun `test Truecaller UPI transfer notification parsing`() {
        val result = PaymentNotificationParser.parseNotification(
            packageName = "com.truecaller",
            title = "₹1",
            text = "UPI transfer from user1 on 05 to Account 1234",
            bigText = null,
            summaryText = null
        )

        assertTrue(result.isTargetApp)
        assertTrue(result.isParsedSuccessfully)
        assertEquals(1.0, result.amount)
        assertEquals(TransactionType.INCOME, result.type)
        assertEquals("Truecaller", result.platform)
    }

    @Test
    fun `test Non target app is ignored`() {
        val result = PaymentNotificationParser.parseNotification(
            packageName = "com.example.unrelatedapp",
            title = "Received ₹500",
            text = "You received points",
            bigText = null,
            summaryText = null
        )

        assertFalse(result.isTargetApp)
        assertFalse(result.isParsedSuccessfully)
        assertNull(result.amount)
    }
}
