package com.lizpostudio.kgoptometrycrm.layer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.lizpostudio.kgoptometrycrm.layer.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao(val db: RoomDatabase) {

    @Transaction
    open suspend fun updateIsSelectAndInsert(e: UserEntity): Long {
        updateIsSelect(false)
        return insert(e)
    }

    @Query("UPDATE ${UserEntity.TABLE_NAME} SET ${UserEntity.COLUMN_NAME_IS_SELECT}=:isSelect")
    abstract fun updateIsSelect(isSelect: Boolean): Int

    @Query("UPDATE ${UserEntity.TABLE_NAME} SET ${UserEntity.COLUMN_NAME_IS_SELECT}=:isSelect WHERE ${UserEntity.COLUMN_NAME_UID}=:uid")
    abstract fun updateIsSelectByUID(uid: String, isSelect: Boolean): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(e: UserEntity): Long

    @Query("SELECT * FROM ${UserEntity.TABLE_NAME} WHERE ${UserEntity.COLUMN_NAME_IS_SELECT}=:isSelect")
    abstract fun selectByIsSelect(isSelect: Boolean): UserEntity?

    @Query("SELECT * FROM ${UserEntity.TABLE_NAME} WHERE ${UserEntity.COLUMN_NAME_UID}=:uid")
    abstract fun selectByID(uid: String?): UserEntity?

    @Query("SELECT * FROM ${UserEntity.TABLE_NAME} WHERE ${UserEntity.COLUMN_NAME_UID}=:id")
    abstract fun selectByIDAsFlow(id: String?): Flow<UserEntity?>

    @Query("DELETE FROM ${UserEntity.TABLE_NAME} WHERE ${UserEntity.COLUMN_NAME_UID}=:id")
    abstract fun deleteByID(id: String?): Int

    @Query("DELETE FROM ${UserEntity.TABLE_NAME}")
    abstract fun deleteAll(): Int
}