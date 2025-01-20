package com.example.reasy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.reasy.data.entity.UserEntitiy

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntitiy): Long

    @Update
    suspend fun updateUser(user: UserEntitiy)

    @Delete
    suspend fun deleteUser(user: UserEntitiy)

    @Query("SELECT * FROM user WHERE usrId = :id")
    suspend fun getUserById(id: Int): UserEntitiy?

    @Query("SELECT * FROM user WHERE USERNAME = :username AND PASSWORD = :password")
    suspend fun authenticateUser(username: String, password: String): UserEntitiy?

    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<UserEntitiy>

}