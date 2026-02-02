package com.example.kargopaylasimkotlin.dto

import com.google.gson.annotations.SerializedName

/**
 * Ortak shipment alanları (liste, detay, item gibi sınıflar için)
 */
interface ShipmentCore {
    val id: Int
    val status: String
}

interface HasPickupCode {
    val pickupCode: String
}

interface HasCodeExpiresAt {
    val codeExpiresAt: String?
}

interface ShipmentIssued {
    val shipment_id: Int
    val pickup_code: String
    val code_expires_at: String
    val status: String
}

data class ShipmentCreateResp(
    override val shipment_id: Int,
    override val pickup_code: String,
    override val code_expires_at: String,
    override val status: String
) : ShipmentIssued

data class ShipmentItemDto(
    override val id: Int,
    val pickup_code: String,
    override val status: String,
    val code_expires_at: String,
    val created_at: String,
    val used_at: String?
) : ShipmentCore {
    val pickupCode: String get() = pickup_code
    val codeExpiresAt: String get() = code_expires_at
}

data class ShipmentListResp(
    @SerializedName("items") val shipments: List<ShipmentDto>
)

data class ShipmentDeleteResp(
    val deleted: Boolean,
    val id: Int
)

data class ShipmentDto(
    override val id: Int,

    @SerializedName("sender_user_id")
    val senderUserId: Int,

    @SerializedName("receiver_user_id")
    val receiverUserId: Int,

    @SerializedName("pickup_code")
    override val pickupCode: String,

    override val status: String,

    @SerializedName("code_expires_at")
    override val codeExpiresAt: String?,

    @SerializedName("cargo_company_id")
    val cargoCompanyId: Long? = null,

    @SerializedName("cargo_company_name")
    val cargoCompanyName: String? = null,

    val role: String,
    val visible: Boolean? = true,

    @SerializedName("sender_initials")
    val senderInitials: String? = null,

    @SerializedName("receiver_address_title")
    val receiverAddressTitle: String? = null

) : ShipmentCore, HasPickupCode, HasCodeExpiresAt


data class ShipmentDetailDto(
    override val id: Int,

    @SerializedName("pickup_code")
    override val pickupCode: String,

    override val status: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("code_expires_at")
    val codeExpiresAtRaw: String,

    @SerializedName("used_at")
    val usedAt: String?,

    @SerializedName("confirmed_at")
    val confirmedAt: String?,

    @SerializedName("in_transit_at")
    val inTransitAt: String?,

    @SerializedName("delivered_at")
    val deliveredAt: String?,

    @SerializedName("cancelled_at")
    val cancelledAt: String?,

    @SerializedName("expired_at")
    val expiredAt: String?,

    @SerializedName("is_sender")
    val isSender: Boolean,

    // ✅ firma
    @SerializedName("cargo_company_id")
    val cargoCompanyId: Long? = null,

    @SerializedName("cargo_company_name")
    val cargoCompanyName: String? = null

) : ShipmentCore, HasPickupCode, HasCodeExpiresAt {
    override val codeExpiresAt: String? get() = codeExpiresAtRaw
}



data class ShipmentDetailResp(
    val shipment: ShipmentDetailDto
)

data class ShipmentCreateReq(
    val receiver_phone: String,
    val sender_address_id: Int? = null
)

data class ShipmentCancelReq(val shipment_id: Int)
data class ShipmentRegenerateReq(val shipment_id: Int)

data class ShipmentDeleteReq(
    @SerializedName("id")
    val id: Int
)

data class ShipmentRegenerateResp(
    override val shipment_id: Int,
    override val pickup_code: String,
    override val code_expires_at: String,
    override val status: String
) : ShipmentIssued
