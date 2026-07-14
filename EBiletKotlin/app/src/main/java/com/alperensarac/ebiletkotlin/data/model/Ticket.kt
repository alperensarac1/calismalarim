package com.alperensarac.ebiletkotlin.data.model

import com.google.gson.annotations.SerializedName

/*
    Ticket modeli

    Bilet satın alma,
    biletlerim,
    bilet detay,
    QR kontrol cevaplarında kullanılacak.
*/
data class Ticket(
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

    /*
        ticket_detail.php cevabında ayrı nested olarak gelebilir.
    */
    val city: City? = null,

    val district: District? = null,

    val venue: Venue? = null,

    /*
        my_tickets.php cevabında location object dönüyor.
    */
    val location: TicketLocation? = null,

    /*
        QR kontrol API cevabında user bilgisi gelebilir.
    */
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