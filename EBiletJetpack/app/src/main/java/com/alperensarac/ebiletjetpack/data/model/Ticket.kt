package com.alperensarac.ebiletjetpack.data.model

import com.google.gson.annotations.SerializedName

/*
    Bilet modeli.

    Şu API'lerde kullanılır:
    - ticket_buy.php
    - my_tickets.php
    - ticket_detail.php
    - ticket_check.php
*/
data class Ticket(
    val id: Int? = null,
    @SerializedName("ticket_id")
    val ticketId: Int? = null,

    @SerializedName("event_id")
    val eventId: Int? = null,

    @SerializedName("event_title")
    val eventTitle: String? = null,

    @SerializedName("ticket_code")
    val ticketCode: String? = null,

    @SerializedName("qr_code_text")
    val qrCodeText: String? = null,

    val price: Double? = null,

    /*
        active
        used
        cancelled
    */
    val status: String? = null,

    @SerializedName("ticket_status")
    val ticketStatus: String? = null,

    @SerializedName("purchased_at")
    val purchasedAt: String? = null,

    @SerializedName("used_at")
    val usedAt: String? = null,

    @SerializedName("transaction_id")
    val transactionId: String? = null,

    val event: Event? = null,
    val city: City? = null,
    val district: District? = null,
    val venue: Venue? = null,
    val location: TicketLocation? = null,
    val user: User? = null,

    @SerializedName("checked_by")
    val checkedBy: CheckedBy? = null,

    val result: String? = null
)

data class TicketLocation(
    @SerializedName("city_name")
    val cityName: String? = null,

    @SerializedName("district_name")
    val districtName: String? = null,

    @SerializedName("venue_name")
    val venueName: String? = null,

    @SerializedName("venue_address")
    val venueAddress: String? = null
)

data class CheckedBy(
    val id: Int,

    @SerializedName("full_name")
    val fullName: String
)