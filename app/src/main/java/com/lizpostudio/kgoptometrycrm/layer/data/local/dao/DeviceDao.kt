package com.lizpostudio.kgoptometrycrm.layer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.lizpostudio.kgoptometrycrm.layer.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DeviceDao(val db: RoomDatabase) {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(e: DeviceEntity): Long

    @Query("DELETE FROM ${DeviceEntity.TABLE_NAME} WHERE ${DeviceEntity.COLUMN_NAME_PROJECT_ID}=:projectID")
    abstract fun deleteByID(projectID: String?): Int

    @Query("SELECT * FROM ${DeviceEntity.TABLE_NAME} WHERE ${DeviceEntity.COLUMN_NAME_PROJECT_ID}=:projectID")
    abstract fun selectByProjectIdAsFlow(projectID: String?): Flow<DeviceEntity?>

    @Query("SELECT * FROM ${DeviceEntity.TABLE_NAME} WHERE ${DeviceEntity.COLUMN_NAME_PROJECT_ID}=:projectID")
    abstract fun selectByProjectId(projectID: String?): DeviceEntity?
}