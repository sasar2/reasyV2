package com.example.reasy.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntitiy(
    @PrimaryKey(autoGenerate = true) val usrId: Int = 0,
    @ColumnInfo(name = "USERNAME") val username: String,
    @ColumnInfo(name = "PASSWORD") val password: String,
    @ColumnInfo(name = "ROLE") val role: String // "client" or "business"
)