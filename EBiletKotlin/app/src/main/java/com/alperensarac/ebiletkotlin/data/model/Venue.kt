package com.alperensarac.ebiletkotlin.data.model

import com.google.gson.annotations.SerializedName

/*
    Venue modeli

    Sahne / mekan bilgisini temsil eder.
*/
data class Venue(
    val id: Int,

    @SerializedName("city_id")
    val cityId: Int? = null,

    @SerializedName("district_id")
    val districtId: Int? = null,

    val name: String,

    val address: String? = null,

    val capacity: Int? = null
)