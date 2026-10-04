package com.captainledger.data.repository

import com.captainledger.data.local.TransactionDao
import com.captainledger.data.model.TimeFilter
import com.captainledger.data.model.TransactionLog
import com.captainledger.data.model.TransactionSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

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

    suspend fun deleteTransaction(transaction: TransactionLog) {
        transactionDao.deleteTransaction(transaction)
    }

    private fun getStartTimeForFilter(filter: TimeFilter): Long {
        val calendar = Calendar.getInstance()
        return when (filter) {
            TimeFilter.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            TimeFilter.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            TimeFilter.ALL -> 0L
        }
    }
}
