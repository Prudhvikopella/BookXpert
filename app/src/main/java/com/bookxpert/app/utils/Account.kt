package com.bookxpert.app.utils

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val accountId: Int = 0,
    val accountName: String,
    val accountAltName: String,
    val accountPicture: String
) : Parcelable