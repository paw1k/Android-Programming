package dev.almasum.fittrack.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.almasum.fittrack.local_db.daos.ItemDao
import dev.almasum.fittrack.local_db.entities.ItemEntity

@Database(entities = [ItemEntity::class], version = 2, exportSchema = false)
abstract class RoomDB : RoomDatabase() {
    abstract fun getItemDao(): ItemDao

    companion object {
        @Volatile
        private var instance: RoomDB? = null

        fun getInstance(context: Context): RoomDB {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): RoomDB {
            return Room.databaseBuilder(
                context.applicationContext,
                RoomDB::class.java,
                "fittrack_db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
