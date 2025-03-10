package com.bookxpert.app.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Account::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "account_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database.accountDao())
                }
            }
        }

        private suspend fun seedDatabase(accountDao: AccountDao) {
            if (accountDao.getCount() == 0) {
                val accounts = listOf(
                    Account(accountName = "John Doe", accountAltName = "JD", accountPicture = "https://example.com/john.jpg"),
                    Account(accountName = "Alice Smith", accountAltName = "Ali", accountPicture = "https://example.com/alice.jpg"),
                    Account(accountName = "Bob Johnson", accountAltName = "Bobby", accountPicture = "https://example.com/bob.jpg")
                )
                accounts.forEach { accountDao.insert(it) }
            }
        }
    }
}
