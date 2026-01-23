package com.example.csvexplorer.service


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.csvexplorer.dao.RowDao
import com.example.csvexplorer.entity.RowEntity

@Database(
    entities = [RowEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDB : RoomDatabase() {

    abstract fun rowDao(): RowDao

    companion object {
        @Volatile private var INSTANCE: AppDB? = null

        fun get(context: Context): AppDB {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "csv_exported.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
