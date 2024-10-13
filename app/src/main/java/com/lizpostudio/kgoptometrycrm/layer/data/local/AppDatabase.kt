package com.lizpostudio.kgoptometrycrm.layer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lizpostudio.kgoptometrycrm.BuildConfig
import com.lizpostudio.kgoptometrycrm.layer.data.local.dao.DeviceDao
import com.lizpostudio.kgoptometrycrm.layer.data.local.dao.UserDao
import com.lizpostudio.kgoptometrycrm.layer.data.local.entity.DeviceEntity
import com.lizpostudio.kgoptometrycrm.layer.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        DeviceEntity::class
    ],
    version = BuildConfig.VERSION_CODE,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var _sInstance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.beginTransaction()
//                database.endTransaction()
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return _sInstance ?: synchronized(this) {
                _sInstance ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "KG-Optometry-CRM"
                )
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2)
                    .allowMainThreadQueries()
                    .build()
                    .also { _sInstance = it }
            }
        }
    }

    abstract val userDao: UserDao

    abstract val deviceDao: DeviceDao
}