package com.alperensarac.ebiletjava.data.model;

import com.google.gson.annotations.SerializedName;

/*
    Venue.java

    Sahne / mekan modelidir.
*/
public class Venue {

    private int id;

    @SerializedName("city_id")
    private Integer cityId;

    @SerializedName("district_id")
    private Integer districtId;

    private String name;

    private String address;

    private Integer capacity;

    public int getId() {
        return id;
    }

    public Integer getCityId() {
        return cityId;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Integer getCapacity() {
        return capacity;
    }
}
