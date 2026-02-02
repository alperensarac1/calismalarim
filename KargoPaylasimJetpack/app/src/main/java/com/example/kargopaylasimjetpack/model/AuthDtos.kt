package com.example.kargopaylasimjetpack.model

data class LoginReq(val phone: String, val password: String)
data class LoginData(val token: String, val user_id: Int)

data class RegisterReq(
    val phone: String,
    val first_name: String,
    val last_name: String,
    val tc_no: String,
    val password: String,

    val address_title: String,
    val city: String,
    val district: String,
    val neighborhood: String,
    val address_line: String,
    val postal_code: String
)

data class RegisterData(val user_id: Int, val address_id: Int)
