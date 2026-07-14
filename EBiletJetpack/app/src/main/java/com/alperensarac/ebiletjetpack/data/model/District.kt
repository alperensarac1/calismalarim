package com.alperensarac.ebiletjetpack.data.model


import com.google.gson.annotations.SerializedName

/*
    İlçe modeli.
*/
data class District(
    val id: Int,

    @SerializedName("city_id")
    val cityId: Int,

    val name: String
)