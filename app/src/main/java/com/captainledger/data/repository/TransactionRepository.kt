package com.captainledger.data.repository

import android.content.Context
import com.captainledger.data.local.TransactionDao
import com.captainledger.data.model.TimeFilter
import com.captainledger.data.model.TransactionLog
import com.captainledger.data.model.TransactionSummary
import com.captainledger.data.model.TransactionType
import com.captainledger.util.DateTimeUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    @ApplicationContext private val context: Context
) {

    private val prefs = context.getSharedPreferences("captain_ledger_prefs", Context.MODE_PRIVATE)
    private val _monthlyBudget = MutableStateFlow(prefs.getFloat("monthly_budget", 25000f).toDouble())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    fun setMonthlyBudget(amount: Double) {
        prefs.edit().putFloat("monthly_budget", amount.toFloat()).apply()
        _monthlyBudget.value = amount
    }

    fun getCurrentMonthExpenses(): Flow<Double> {
        val startOfMonth = DateTimeUtils.getStartOfMonth()
        return transactionDao.getExpenseSumFrom(startOfMonth).map { it ?: 0.0 }
    }

    fun getTransactions(timeFilter: TimeFilter): Flow<List<TransactionLog>> {
        val startTime = getStartTimeForFilter(timeFilter)
        return if (startTime == 0L) {
            transactionDao.getAllTransactions()
        } else {
            transactionDao.getTransactionsFrom(startTime)
        }
    }

    fun getSummary(timeFilter: TimeFilter): Flow<TransactionSummary> {
        val startTime = getStartTimeForFilter(timeFilter)
        val incomeFlow = if (startTime == 0L) {
            transactionDao.getTotalIncome()
        } else {
            transactionDao.getIncomeSumFrom(startTime)
        }

        val expenseFlow = if (startTime == 0L) {
            transactionDao.getTotalExpense()
        } else {
            transactionDao.getExpenseSumFrom(startTime)
        }

        return combine(incomeFlow, expenseFlow) { income, expense ->
            val totalIncome = income ?: 0.0
            val totalExpense = expense ?: 0.0
            TransactionSummary(
                totalEarnings = totalIncome,
                totalExpenses = totalExpense,
                netProfit = totalIncome - totalExpense
            )
        }
    }

    suspend fun addTransaction(transaction: TransactionLog): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun isDuplicateTransaction(
        amount: Double,
        type: TransactionType,
        windowMillis: Long = 120000L
    ): Boolean {
        val sinceTimestamp = System.currentTimeMillis() - windowMillis
        return transactionDao.hasRecentTransaction(amount, type, sinceTimestamp)
    }

    suspend fun deleteTransaction(transaction: TransactionLog) {
        transactionDao.deleteTransaction(transaction)
    }

    private fun getStartTimeForFilter(filter: TimeFilter): Long {
        return when (filter) {
            TimeFilter.TODAY -> DateTimeUtils.getStartOfToday()
            TimeFilter.THIS_WEEK -> DateTimeUtils.getStartOfWeek()
            TimeFilter.THIS_MONTH -> DateTimeUtils.getStartOfMonth()
            TimeFilter.ALL -> 0L
        }
    }
}
