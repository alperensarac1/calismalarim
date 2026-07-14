//
//  APIService.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import Foundation

final class APIService {

    static let shared = APIService()

    private let client = APIClient.shared

    private init() {}

    func register(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ) async throws -> APIResponse<User> {
        try await client.post(
            endpoint: "auth/register.php",
            parameters: [
                "full_name": fullName,
                "email": email,
                "phone": phone,
                "password": password
            ]
        )
    }

    func login(
        email: String,
        password: String
    ) async throws -> APIResponse<User> {
        try await client.post(
            endpoint: "auth/login.php",
            parameters: [
                "email": email,
                "password": password
            ]
        )
    }

    func profile(
        apiToken: String
    ) async throws -> APIResponse<User> {
        try await client.post(
            endpoint: "auth/profile.php",
            parameters: [
                "api_token": apiToken
            ]
        )
    }

    func getCities(
        apiToken: String
    ) async throws -> APIResponse<[City]> {
        try await client.post(
            endpoint: "locations/cities_list.php",
            parameters: [
                "api_token": apiToken
            ]
        )
    }

    func getDistrictsByCity(
        apiToken: String,
        cityId: Int
    ) async throws -> APIResponse<[District]> {
        try await client.post(
            endpoint: "locations/districts_by_city.php",
            parameters: [
                "api_token": apiToken,
                "city_id": String(cityId)
            ]
        )
    }

    func getVenuesByDistrict(
        apiToken: String,
        cityId: Int,
        districtId: Int
    ) async throws -> APIResponse<[Venue]> {
        try await client.post(
            endpoint: "locations/venues_by_district.php",
            parameters: [
                "api_token": apiToken,
                "city_id": String(cityId),
                "district_id": String(districtId)
            ]
        )
    }

    func getEventsByLocation(
        apiToken: String,
        cityId: Int,
        districtId: Int
    ) async throws -> APIResponse<[Event]> {
        try await client.post(
            endpoint: "events/events_by_location.php",
            parameters: [
                "api_token": apiToken,
                "city_id": String(cityId),
                "district_id": String(districtId)
            ]
        )
    }

    func getEventDetail(
        apiToken: String,
        eventId: Int
    ) async throws -> APIResponse<Event> {
        try await client.post(
            endpoint: "events/event_detail.php",
            parameters: [
                "api_token": apiToken,
                "event_id": String(eventId)
            ]
        )
    }

    func buyTicket(
        apiToken: String,
        eventId: Int
    ) async throws -> APIResponse<Ticket> {
        try await client.post(
            endpoint: "tickets/ticket_buy.php",
            parameters: [
                "api_token": apiToken,
                "event_id": String(eventId)
            ]
        )
    }

    func getMyTickets(
        apiToken: String
    ) async throws -> APIResponse<[Ticket]> {
        try await client.post(
            endpoint: "tickets/my_tickets.php",
            parameters: [
                "api_token": apiToken
            ]
        )
    }

    func getTicketDetail(
        apiToken: String,
        ticketId: Int
    ) async throws -> APIResponse<Ticket> {
        try await client.post(
            endpoint: "tickets/ticket_detail.php",
            parameters: [
                "api_token": apiToken,
                "ticket_id": String(ticketId)
            ]
        )
    }

    func checkTicket(
        apiToken: String,
        ticketCode: String
    ) async throws -> APIResponse<Ticket> {
        try await client.post(
            endpoint: "check/ticket_check.php",
            parameters: [
                "api_token": apiToken,
                "ticket_code": ticketCode
            ]
        )
    }
}
