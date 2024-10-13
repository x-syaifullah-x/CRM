package com.lizpostudio.kgoptometrycrm.layer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = UserEntity.TABLE_NAME)
data class UserEntity(

    @PrimaryKey
    @ColumnInfo(name = COLUMN_NAME_ID)
    val id: String,

    @ColumnInfo(name = COLUMN_NAME_EMAIL)
    val email: String?,

    @ColumnInfo(name = COLUMN_NAME_DEVICE_CODE)
    val deviceCode: String,

    @ColumnInfo(name = COLUMN_NAME_IS_TRUSTED)
    val isTrusted: Boolean,

    @ColumnInfo(name = COLUMN_NAME_IS_ADMIN)
    val isAdmin: Boolean
) {
    companion object {

        const val TABLE_NAME = "users"
        const val COLUMN_NAME_ID = "id"
        const val COLUMN_NAME_EMAIL = "email"
        const val COLUMN_NAME_DEVICE_CODE = "device_code"
        const val COLUMN_NAME_IS_TRUSTED = "is_trusted"
        const val COLUMN_NAME_IS_ADMIN = "is_admin"
    }
}