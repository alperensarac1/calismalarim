package com.alperensarac.ebiletjetpack.data.model

import com.google.gson.annotations.SerializedName

data class Event(
    val id: Int,

    @SerializedName("city_id")
    val cityId: Int? = null,

    @SerializedName("district_id")
    val districtId: Int? = null,

    @SerializedName("venue_id")
    val venueId: Int? = null,

    val title: String,

    val description: String? = null,

    @SerializedName("poster_url")
    val posterUrl: String? = null,

    @SerializedName("event_date")
    val eventDate: String? = null,

    @SerializedName("base_price")
    val basePrice: Double? = null,

    @SerializedName("total_quota")
    val totalQuota: Int? = null,

    @SerializedName("sold_count")
    val soldCount: Int? = null,

    @SerializedName("remaining_quota")
    val remainingQuota: Int? = null,

    @SerializedName("city_name")
    val cityName: String? = null,

    @SerializedName("district_name")
    val districtName: String? = null,

    val venue: Venue? = null,
    val city: City? = null,
    val district: District? = null,

    @SerializedName("created_at")
    val createdAt: String? = null
)