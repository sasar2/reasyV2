package com.example.reasy.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "businesses",
    foreignKeys = [ForeignKey(
        entity = UserEntitiy::class,
        parentColumns = ["usrId"],
        childColumns = ["USR_ID"],
        onDelete = ForeignKey.CASCADE
    )])
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true) val busId: Int = 0,
    @ColumnInfo(name = "USR_ID") val usrId: Int,
    @ColumnInfo(name = "NAME") val name: String,
    @ColumnInfo(name = "DESCRIPTION") val description: String? = null,
    @ColumnInfo(name = "WORKING_HOURS") val workingHours: String, // e.g., 09:00-17:00
    @ColumnInfo(name = "RESERVATION_TIME") val reservationTime: Int,
    @ColumnInfo(name = "RATINGS") val rating: String,
    @ColumnInfo(name = "CATEGORY") val category: String,
    @ColumnInfo(name = "IMAGE_URL") val imageUrl: String,
    @ColumnInfo(name = "ADDRESS") val address: String,
    @ColumnInfo(name = "PHONE") val phone: String
) : Parcelable
