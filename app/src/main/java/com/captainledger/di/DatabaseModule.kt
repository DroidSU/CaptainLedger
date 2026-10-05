package com.captainledger.di

import android.content.Context
import androidx.room.Room
import com.captainledger.data.local.AppDatabase
import com.captainledger.data.local.NotificationLogDao
import com.captainledger.data.local.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "captain_ledger.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao()
    }

    @Provides
    fun provideNotificationLogDao(appDatabase: AppDatabase): NotificationLogDao {
        return appDatabase.notificationLogDao()
    }
}
