package com.example.csvexplorer.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.csvexplorer.entity.RowEntity

@Dao
interface RowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RowEntity>)

    @Query("SELECT * FROM rows ORDER BY localId DESC")
    suspend fun getAll(): List<RowEntity>

    @Query("DELETE FROM rows")
    suspend fun clear()

    @Query("""
        SELECT * FROM rows
        WHERE (:q IS NULL OR :q = '' OR dataJson LIKE '%' || :q || '%')
        ORDER BY localId DESC
    """)
    suspend fun searchAllColumns(q: String?): List<RowEntity>

    @Query("""
        SELECT * FROM rows
        WHERE (:key IS NULL OR :key = '' OR :q IS NULL OR :q = '' OR
               dataJson LIKE '%"' || :key || '":"%' || :q || '%"%')
        ORDER BY localId DESC
    """)
    suspend fun searchInColumn(key: String?, q: String?): List<RowEntity>
}
