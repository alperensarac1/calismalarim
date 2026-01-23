package com.example.csvexplorerjetpack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rows")
data class RowEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val externalId: String? = null,
    val dataJson: String
)
