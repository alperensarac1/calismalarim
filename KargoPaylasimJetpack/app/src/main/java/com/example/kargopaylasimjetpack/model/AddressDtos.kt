package com.example.kargopaylasimjetpack.model

data class Address(
    val id: Int,
    val title: String,
    val city: String,
    val district: String,
    val address_line: String,
    val is_default: Int
)

data class AddressListData(val items: List<Address>)
data class AddressCreateReq(
    val title: String,
    val city: String,
    val district: String,
    val neighborhood: String,
    val address_line: String,
    val postal_code: String
)
data class AddressCreateData(val id: Int)

data class AddressIdReq(val id: Int)
