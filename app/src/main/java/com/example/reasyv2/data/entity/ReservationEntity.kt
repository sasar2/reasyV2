package com.example.reasy.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reservation",
    foreignKeys = [
        ForeignKey(
            entity = UserEntitiy::class,
            parentColumns = ["usrId"],
            childColumns = ["CLI_ID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["busId"],
            childColumns = ["BUS_ID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TimeSlotEntity::class,
            parentColumns = ["tmsId"],
            childColumns = ["TMS_ID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true) val resId: Int = 0,
    @ColumnInfo(name = "CLI_ID") val cliId: Int,
    @ColumnInfo(name = "BUS_ID") val busId: Int,
    @ColumnInfo(name = "TMS_ID") val tmsId: Int,
    @ColumnInfo(name = "STATUS") val status: String, // pending, approved, rejected
    @ColumnInfo(name = "CREATED_AT") val createdAt: String // Timestamp
)
