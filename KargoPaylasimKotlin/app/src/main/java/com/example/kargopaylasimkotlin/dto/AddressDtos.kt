package com.example.kargopaylasimkotlin.dto

interface AddressFields {
    val title: String
    val city: String
    val district: String
    val neighborhood: String?
    val address_line: String
    val postal_code: String?
}

data class AddressCreateReq(
    override val title: String,
    override val city: String,
    override val district: String,
    override val neighborhood: String? = null,
    override val address_line: String,
    override val postal_code: String? = null
) : AddressFields

data class AddressDetailReq(
    val id: Int,
    val token: String
)

data class AddressUpdateReq(
    val id: Int,
    override val title: String,
    override val city: String,
    override val district: String,
    override val neighborhood: String? = null,
    override val address_line: String,
    override val postal_code: String? = null
) : AddressFields

data class AddressDto(
    val id: Int,
    override val title: String,
    override val city: String,
    override val district: String,
    override val neighborhood: String? = null,
    override val address_line: String,
    override val postal_code: String? = null,
    val is_default: Int
) : AddressFields

data class ReceiverAddressDto(
    val id: Int,
    val title: String?,
    val city: String?,
    val district: String?,
    val address_line: String?
)

data class AddressDeleteReq(val id: Int)
data class AddressSetDefaultReq(val id: Int)

data class AddressListResp(
    val items: List<AddressDto> = emptyList()
)

data class AddressIdResp(
    val id: Int
)
