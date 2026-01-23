package com.example.csvexplorerjava.entity;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rows")
public class RowEntity {

    @PrimaryKey(autoGenerate = true)
    private long localId;

    @Nullable
    private String externalId;

    private String dataJson;

    public RowEntity(long localId, @Nullable String externalId, String dataJson) {
        this.localId = localId;
        this.externalId = externalId;
        this.dataJson = dataJson;
    }

    // Room için kolay constructor (localId auto)
    public RowEntity(@Nullable String externalId, String dataJson) {
        this.localId = 0L;
        this.externalId = externalId;
        this.dataJson = dataJson;
    }

    public long getLocalId() { return localId; }
    public void setLocalId(long localId) { this.localId = localId; }

    @Nullable
    public String getExternalId() { return externalId; }
    public void setExternalId(@Nullable String externalId) { this.externalId = externalId; }

    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }
}

