package com.example.csvexplorerjava.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.example.csvexplorerjava.entity.RowEntity;

import java.util.List;

@Dao
public interface RowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RowEntity> items);

    @Query("SELECT * FROM rows ORDER BY localId DESC")
    List<RowEntity> getAll();

    @Query("DELETE FROM rows")
    void clear();

    @Query("SELECT * FROM rows " +
            "WHERE (:q IS NULL OR :q = '' OR dataJson LIKE '%' || :q || '%') " +
            "ORDER BY localId DESC")
    List<RowEntity> searchAllColumns(String q);

    @Query("SELECT * FROM rows " +
            "WHERE (:key IS NULL OR :key = '' OR :q IS NULL OR :q = '' OR " +
            "dataJson LIKE '%\"' || :key || '\":\"%' || :q || '%\"%') " +
            "ORDER BY localId DESC")
    List<RowEntity> searchInColumn(String key, String q);
}

