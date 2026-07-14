package com.alperensarac.ebiletjava.data.model;

import com.google.gson.annotations.SerializedName;

/*
    District.java

    İlçe modelidir.
*/
public class District {

    private int id;

    @SerializedName("city_id")
    private int cityId;

    private String name;

    public int getId() {
        return id;
    }

    public int getCityId() {
        return cityId;
    }

    public String getName() {
        return name;
    }

    /*
        Spinner içinde ilçe adının görünmesini sağlar.
    */
    @Override
    public String toString() {
        return name;
    }
}
