package com.alperensarac.ebiletkotlin.data.model

import com.google.gson.annotations.SerializedName

/*
    District modeli

    Backend:
    locations/districts_by_city.php
*/
data class District(
    val id: Int,

    @SerializedName("city_id")
    val cityId: Int,

    val name: String
)