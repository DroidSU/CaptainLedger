package com.captainledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_log")
data class TransactionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val paymentMode: PaymentMode,
    val category: String,
    val platform: String,
    val timestamp: Long = System.currentTimeMillis()
)
