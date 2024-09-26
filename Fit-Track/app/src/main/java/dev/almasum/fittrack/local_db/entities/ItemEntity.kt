package dev.almasum.fittrack.local_db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ItemEntity(
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "code")
    val code: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "imageUrl")
    val imageUrl: String,
    @ColumnInfo(name = "group")
    val group: String,
    @ColumnInfo(name = "energy")
    val energy: Double,
    @ColumnInfo(name = "fat")
    val fat: Double,
    @ColumnInfo(name = "protein")
    val protein: Double,
    @ColumnInfo(name = "sugar")
    val sugar: Double,
    @ColumnInfo(name = "sodium")
    val sodium: Double,
    @ColumnInfo(name = "amount")
    val amount: Double,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    constructor() : this(0L, "", "", "", "", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
}