package com.lizpostudio.kgoptometrycrm.layer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = DeviceEntity.TABLE_NAME)
data class DeviceEntity(

    @PrimaryKey
    @ColumnInfo(name = COLUMN_NAME_PROJECT_ID)
    val projectID: String,

    @ColumnInfo(name = COLUMN_NAME_CODE)
    val code: String,

    @ColumnInfo(name = COLUMN_NAME_IS_TRUSTED)
    val isTrusted: Boolean,
) {
    companion object {

        const val TABLE_NAME = "DEVICE"
        const val COLUMN_NAME_PROJECT_ID = "PROJECT_ID"
        const val COLUMN_NAME_CODE = "CODE"
        const val COLUMN_NAME_IS_TRUSTED = "IS_TRUSTED"
    }
}