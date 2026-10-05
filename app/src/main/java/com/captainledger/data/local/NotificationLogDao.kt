package com.captainledger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.captainledger.data.model.NotificationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationLog(log: NotificationLog): Long

    @Query("SELECT * FROM notification_log ORDER BY timestamp DESC LIMIT 100")
    fun getAllNotificationLogs(): Flow<List<NotificationLog>>

    @Query("DELETE FROM notification_log")
    suspend fun clearAllLogs()
}
