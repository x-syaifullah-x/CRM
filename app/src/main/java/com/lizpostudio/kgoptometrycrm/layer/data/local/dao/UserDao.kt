package com.lizpostudio.kgoptometrycrm.layer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.lizpostudio.kgoptometrycrm.layer.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao(val db: RoomDatabase) {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(e: UserEntity): Long

    @Query("SELECT * FROM ${UserEntity.TABLE_NAME} WHERE ${UserEntity.COLUMN_NAME_ID}=:id")
    abstract fun selectUserByID(id: String): UserEntity?

    @Query("SELECT * FROM ${UserEntity.TABLE_NAME} WHERE ${UserEntity.COLUMN_NAME_ID}=:id")
    abstract fun selectUserByIDAsFlow(id: String?): Flow<UserEntity?>

    @Query("DELETE FROM ${UserEntity.TABLE_NAME} WHERE ${UserEntity.COLUMN_NAME_ID}=:id")
    abstract fun deleteByID(id: String?): Int

    @Query("DELETE FROM ${UserEntity.TABLE_NAME}")
    abstract fun deleteAll(): Int
}