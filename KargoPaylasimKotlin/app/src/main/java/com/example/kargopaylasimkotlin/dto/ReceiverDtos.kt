package com.example.kargopaylasimkotlin.dto

data class ReceiverLookupReq(val phone: String)

data class ReceiverLookupResp(
    val receiver_user_id: Int,
    val masked_first_name: String,
    val masked_last_name: String
)
