package com.example.csvexplorerjava.service;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.csvexplorerjava.dao.RowDao;
import com.example.csvexplorerjava.entity.RowEntity;


@Database(entities = {RowEntity.class}, version = 1, exportSchema = false)
public abstract class AppDB extends RoomDatabase {

    public abstract RowDao rowDao();

    private static volatile AppDB INSTANCE;

    public static AppDB get(Context context) {
        if (INSTANCE != null) return INSTANCE;
        synchronized (AppDB.class) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDB.class,
                        "csv_exported.db"
                ).build();
            }
        }
        return INSTANCE;
    }
}

