package com.example.kargopaylasimjetpack.model


data class Shipment(
    val id: Int,
    val pickup_code: String,
    val status: String,
    val cargo_company_name: String? = null
)

data class ShipmentListData(val items: List<Shipment>)

data class LookupReceiverReq(val phone: String)
data class LookupReceiverData(
    val receiver_user_id: Int,
    val masked_first_name: String,
    val masked_last_name: String
)

data class CreateShipmentReq(
    val receiver_phone: String,
    val sender_address_id: Int? = null
)

data class CreateShipmentData(
    val shipment_id: Int,
    val pickup_code: String,
    val status: String,
    val code_expires_at: String
)
