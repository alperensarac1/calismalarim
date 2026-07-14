import Foundation

struct Ticket: Codable {


    let id: Int?
    let ticketId: Int?

    let eventId: Int?
    let eventTitle: String?

    let ticketCode: String?
    let qrCodeText: String?

    let price: Double?


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
        SwiftUI List / ForEach içinde id olarak bunu kullanacağız.

        Örnek:

        ForEach(tickets, id: \.resolvedTicketId) { ticket in
            TicketCardView(ticket: ticket)
        }
    */
    var resolvedTicketId: Int {
        return ticketId ?? id ?? 0
    }
}

/*
    TicketLocation

    Bilet listesinde location objesi dönebilir.
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
