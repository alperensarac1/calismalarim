package com.example.csvexplorer.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rows")
data class RowEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val externalId: String?,
    val dataJson: String
)
