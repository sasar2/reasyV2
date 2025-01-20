package com.example.reasy.data.repository

import com.example.reasy.data.dao.BusinessDao
import com.example.reasy.data.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BusinessRepository(private val businessDao: BusinessDao) {

    suspend fun insertBusiness(business: BusinessEntity): Long {
        return businessDao.insertBusiness(business)
    }

    suspend fun updateBusiness(business: BusinessEntity) {
        businessDao.updateBusiness(business)
    }

    suspend fun deleteBusiness(business: BusinessEntity) {
        businessDao.deleteBusiness(business)
    }


    suspend fun getBusinessById(id: Long): BusinessEntity? {
        return businessDao.getBusinessById(id)
    }

    suspend fun getBusinessesByUserId(userId: Int): List<BusinessEntity> {
        return businessDao.getBusinessesByUserId(userId)
    }

    suspend fun getAllBusinesses(): List<BusinessEntity> {
        return businessDao.getAllBusinesses()
    }

}

