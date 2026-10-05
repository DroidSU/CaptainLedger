package com.captainledger.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.captainledger.data.model.TransactionLog
import com.captainledger.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionLog): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionLog)

    @Query("SELECT * FROM transaction_log ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionLog>>

    @Query("SELECT * FROM transaction_log WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getTransactionsFrom(startTime: Long): Flow<List<TransactionLog>>

    @Query("SELECT SUM(amount) FROM transaction_log WHERE type = 'INCOME' AND timestamp >= :startTime")
    fun getIncomeSumFrom(startTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transaction_log WHERE type = 'EXPENSE' AND timestamp >= :startTime")
    fun getExpenseSumFrom(startTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transaction_log WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transaction_log WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double?>

    @Query("SELECT EXISTS(SELECT 1 FROM transaction_log WHERE amount = :amount AND type = :type AND timestamp >= :sinceTimestamp)")
    suspend fun hasRecentTransaction(amount: Double, type: TransactionType, sinceTimestamp: Long): Boolean
}
