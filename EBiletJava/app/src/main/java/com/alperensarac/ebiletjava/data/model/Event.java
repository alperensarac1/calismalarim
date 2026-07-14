package com.alperensarac.ebiletjava.data.model;

import com.google.gson.annotations.SerializedName;

public class Event {

    private int id;

    @SerializedName("city_id")
    private Integer cityId;

    @SerializedName("district_id")
    private Integer districtId;

    @SerializedName("venue_id")
    private Integer venueId;

    private String title;

    private String description;

    @SerializedName("poster_url")
    private String posterUrl;

    @SerializedName("event_date")
    private String eventDate;

    @SerializedName("base_price")
    private Double basePrice;

    @SerializedName("total_quota")
    private Integer totalQuota;

    @SerializedName("sold_count")
    private Integer soldCount;

    @SerializedName("remaining_quota")
    private Integer remainingQuota;

    @SerializedName("city_name")
    private String cityName;

    @SerializedName("district_name")
    private String districtName;

    private Venue venue;

    private City city;

    private District district;

    @SerializedName("created_at")
    private String createdAt;

    public int getId() {
        return id;
    }

    public Integer getCityId() {
        return cityId;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public Integer getVenueId() {
        return venueId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getEventDate() {
        return eventDate;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public Integer getTotalQuota() {
        return totalQuota;
    }

    public Integer getSoldCount() {
        return soldCount;
    }

    public Integer getRemainingQuota() {
        return remainingQuota;
    }

    public String getCityName() {
        return cityName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public Venue getVenue() {
        return venue;
    }

    public City getCity() {
        return city;
    }

    public District getDistrict() {
        return district;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
