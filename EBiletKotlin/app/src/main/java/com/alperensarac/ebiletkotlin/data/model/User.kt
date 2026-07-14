package com.alperensarac.ebiletkotlin.data.model

import com.google.gson.annotations.SerializedName

/*
    User modeli

    PHP'den gelen kullanıcı bilgilerini temsil eder.

    Backend JSON alanları snake_case:
    full_name, api_token

    Kotlin'de camelCase kullanıyoruz:
    fullName, apiToken

    @SerializedName ile bu eşleşmeyi yapıyoruz.
*/
data class User(
    val id: Int,

    @SerializedName("full_name")
    val fullName: String,

    val email: String,

    val phone: String?,

    /*
        role:
        user
        staff
        admin
    */
    val role: String,

    @SerializedName("api_token")
    val apiToken: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null
)