package dev.almasum.fittrack.local_db.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.almasum.fittrack.local_db.entities.ItemEntity

@Dao
interface ItemDao {

    @Insert
    fun insert(itemEntity: ItemEntity): Long

    @Query("SELECT * FROM ItemEntity WHERE timestamp >= :timestamp ORDER BY timestamp DESC")
    fun getTodayItems(timestamp: Long): LiveData<List<ItemEntity>>

    @Query("DELETE FROM ItemEntity")
    fun clearTable()

}