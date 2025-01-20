package com.example.reasy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.reasy.data.entity.TimeSlotEntity

@Dao
interface TimeSlotDao {

    @Query("SELECT COUNT(*) FROM timeslot WHERE BUS_ID = :busId AND DATE = :date AND START_TIME = :startTime AND END_TIME = :endTime")
    suspend fun isTimeSlotExists(busId: Int, date: String, startTime: String, endTime: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlot(timeSlot: TimeSlotEntity): Long

    @Update
    suspend fun updateTimeSlot(timeSlot: TimeSlotEntity)

    @Query("UPDATE timeslot SET STATUS = :status WHERE BUS_ID = :busId AND DATE = :date AND START_TIME = :startTime AND END_TIME = :endTime")
    suspend fun updateStatus(busId: Int, date: String, startTime: String, endTime: String, status: String)

    @Delete
    suspend fun deleteTimeSlot(timeSlot: TimeSlotEntity)

    @Query("SELECT * FROM timeslot WHERE tmsId = :id")
    suspend fun getTimeSlotById(id: Int): TimeSlotEntity?

    @Query("SELECT * FROM timeslot WHERE BUS_ID = :businessId AND DATE = :date")
    suspend fun getTimeSlotsByBusinessAndDate(businessId: Int, date: String): List<TimeSlotEntity>

    @Query("SELECT * FROM timeslot")
    suspend fun getAllTimeSlots(): List<TimeSlotEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM timeslot WHERE BUS_ID = :businessId AND DATE = :date)")
    suspend fun hasTimeSlotsForDate(businessId: Int, date: String): Boolean

    @Query("SELECT STATUS FROM timeslot WHERE BUS_ID = :busId AND DATE = :date AND START_TIME = :startTime AND END_TIME = :endTime")
    suspend fun getTimeSlotStatus(busId: Int, date: String, startTime: String, endTime: String): String?
}
