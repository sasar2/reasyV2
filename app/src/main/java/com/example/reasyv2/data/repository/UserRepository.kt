package com.example.reasy.data.repository

import com.example.reasy.data.dao.UserDao
import com.example.reasy.data.entity.UserEntitiy

class UserRepository(private val userDao: UserDao) {

    suspend fun insertUser(user: UserEntitiy): Long {
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntitiy) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: UserEntitiy) {
        userDao.deleteUser(user)
    }

    suspend fun getUserById(id: Int): UserEntitiy? {
        return userDao.getUserById(id)
    }

    suspend fun authenticateUser(username: String, password: String): UserEntitiy? {
        return userDao.authenticateUser(username, password)
    }

    suspend fun getAllUsers(): List<UserEntitiy> {
        return userDao.getAllUsers()
    }
}
