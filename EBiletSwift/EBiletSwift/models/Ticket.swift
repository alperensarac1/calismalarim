//
//  Ticket.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 25.06.2026.
//

import Foundation

/*
    Ticket

    Bilet modelidir.

    Şu endpointlerde kullanılır:
    - ticket_buy.php
    - my_tickets.php
    - ticket_detail.php
    - ticket_check.php
*/
struct Ticket: Codable {

    /*
        Bazı endpointler id döndürebilir.
        Bazıları ticket_id döndürebilir.
        İkisini de destekliyoruz.
    */
    let id: Int?
    let ticketId: Int?

    let eventId: Int?
    let eventTitle: String?

    let ticketCode: String?
    let qrCodeText: String?

    let price: Double?

    /*
        active
        used
        cancelled
    */
    let status: String?
    let ticketStatus: String?

    let purchasedAt: String?
    let usedAt: String?
    let transactionId: String?

    let event: Event?
    let city: City?
    let district: District?
    let venue: Venue?
    let location: TicketLocation?
    let user: User?

    let checkedBy: CheckedBy?

    /*
        ticket_check.php sonucunda:
        approved
        already_used
        invalid
        cancelled
        passive_event
    */
    let result: String?

    enum CodingKeys: String, CodingKey {
        case id
        case ticketId = "ticket_id"
        case eventId = "event_id"
        case eventTitle = "event_title"
        case ticketCode = "ticket_code"
        case qrCodeText = "qr_code_text"
        case price
        case status
        case ticketStatus = "ticket_status"
        case purchasedAt = "purchased_at"
        case usedAt = "used_at"
        case transactionId = "transaction_id"
        case event
        case city
        case district
        case venue
        case location
        case user
        case checkedBy = "checked_by"
        case result
    }

    /*
        Kullanılabilir bilet id değerini tek yerden almak için yardımcı property.
    */
    var resolvedTicketId: Int? {
        return ticketId ?? id
    }
}

/*
    TicketLocation

    Backend bazı bilet listelerinde location objesi döndürüyor:

    {
        "city_name": "...",
        "district_name": "...",
        "venue_name": "...",
        "venue_address": "..."
    }
*/
struct TicketLocation: Codable {

    let cityName: String?
    let districtName: String?
    let venueName: String?
    let venueAddress: String?

    enum CodingKeys: String, CodingKey {
        case cityName = "city_name"
        case districtName = "district_name"
        case venueName = "venue_name"
        case venueAddress = "venue_address"
    }
}

/*
    CheckedBy

    QR kontrolünü yapan görevli bilgisi.
*/
struct CheckedBy: Codable {

    let id: Int
    let fullName: String

    enum CodingKeys: String, CodingKey {
        case id
        case fullName = "full_name"
    }
}
