package com.example.reasy.data.dao

import androidx.room.*
import com.example.reasy.data.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinesses(businesses: List<BusinessEntity>)

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    @Delete
    suspend fun deleteBusiness(business: BusinessEntity)

    @Query("SELECT * FROM businesses WHERE busId = :id")
    suspend fun getBusinessById(id: Long): BusinessEntity?

    @Query("SELECT * FROM businesses WHERE USR_ID = :userId")
    suspend fun getBusinessesByUserId(userId: Int): List<BusinessEntity>

    @Query("SELECT * FROM businesses WHERE category = :category")
    fun getBusinessesByCategory(category: String): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses")
    suspend fun getAllBusinesses(): List<BusinessEntity>

    @Query("SELECT * FROM businesses")
    fun getAllBusinessesFlow(): Flow<List<BusinessEntity>>

} 