package com.bookxpert.app.utils

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(account: Account)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(accounts: List<Account>) // ✅ Insert multiple accounts

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<Account>

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getCount(): Int

    @Query("DELETE FROM accounts WHERE accountId = :id") // ✅ Delete by ID
    suspend fun deleteById(id: Int)

    @Query("UPDATE accounts SET accountAltName = :altName, accountPicture = :profile WHERE accountId = :id")
    suspend fun updateAltNameAndProfile(id: Int, altName: String, profile: String) // ✅ Update altName & profile
}

