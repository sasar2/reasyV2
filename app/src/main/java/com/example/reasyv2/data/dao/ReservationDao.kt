package com.example.reasy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.reasy.data.entity.ReservationEntity

@Dao
interface ReservationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity): Long

    @Update
    suspend fun updateReservation(reservation: ReservationEntity)

    @Delete
    suspend fun deleteReservation(reservation: ReservationEntity)

    @Query("SELECT * FROM reservation WHERE resId = :id")
    suspend fun getReservationById(id: Int): ReservationEntity?

    @Query("SELECT * FROM reservation WHERE CLI_ID = :clientId")
    suspend fun getReservationsByClient(clientId: Int): List<ReservationEntity>

    @Query("SELECT * FROM reservation WHERE BUS_ID = :businessId")
    suspend fun getReservationsByBusiness(businessId: Int): List<ReservationEntity>

    @Query("SELECT * FROM reservation WHERE TMS_ID = :timeSlotId")
    suspend fun getReservationsByTimeSlot(timeSlotId: Int): List<ReservationEntity>

    @Query("SELECT * FROM reservation")
    suspend fun getAllReservations(): List<ReservationEntity>
}
