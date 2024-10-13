package com.lizpostudio.kgoptometrycrm.layer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = UserEntity.TABLE_NAME)
data class UserEntity(

    @PrimaryKey
    @ColumnInfo(name = COLUMN_NAME_UID)
    val uid: String,

    @ColumnInfo(name = COLUMN_NAME_EMAIL)
    val email: String?,

    @ColumnInfo(name = COLUMN_NAME_IS_ADMIN)
    val isAdmin: Boolean,

    @ColumnInfo(name = COLUMN_NAME_IS_SELECT)
    val isSelect: Boolean,
) {
    companion object {

        const val TABLE_NAME = "USER"
        const val COLUMN_NAME_UID = "UID"
        const val COLUMN_NAME_EMAIL = "EMAIL"
        const val COLUMN_NAME_IS_ADMIN = "IS_ADMIN"
        const val COLUMN_NAME_IS_SELECT = "IS_SELECT"
    }
}