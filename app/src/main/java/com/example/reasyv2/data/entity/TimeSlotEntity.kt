package com.example.reasy.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "timeslot",
    foreignKeys = [ForeignKey(
        entity = BusinessEntity::class,
        parentColumns = ["busId"],
        childColumns = ["BUS_ID"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["BUS_ID", "DATE", "START_TIME", "END_TIME"], unique = true)]
)
data class TimeSlotEntity(
    @PrimaryKey(autoGenerate = true) val tmsId: Int = 0,
    @ColumnInfo(name = "BUS_ID") val busId: Int,
    @ColumnInfo(name = "DATE") val date: String, // YYYY-MM-DD
    @ColumnInfo(name = "START_TIME") val startTime: String, // HH:mm
    @ColumnInfo(name = "END_TIME") val endTime: String, // HH:mm
    @ColumnInfo(name = "STATUS") val status: String // available, pending, reserved
)