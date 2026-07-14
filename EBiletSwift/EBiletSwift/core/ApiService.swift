//
//  ApiService.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 25.06.2026.
//

import Foundation

/*
    APIService

    Uygulamada kullanılacak endpoint fonksiyonlarını tek yerde toplar.

    ViewController içinde direkt endpoint stringleri yazmak yerine
    buradaki fonksiyonları çağıracağız.

    Böylece:
    - Kod temiz olur.
    - Endpoint değişirse tek yerden düzenlenir.
*/
final class APIService {

    static let shared = APIService()

    private let client = APIClient.shared

    private init() {}

    /*
        Kullanıcı kayıt.

        PHP:
        auth/register.php
    */
    func register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        completion: @escaping (Result<APIResponse<User>, Error>) -> Void
    ) {
        client.post(
            endpoint: "auth/register.php",
            parameters: [
                "full_name": fullName,
                "email": email,
                "phone": phone,
                "password": password
            ],
            completion: completion
        )
    }

    /*
        Kullanıcı giriş.

        PHP:
        auth/login.php
    */
    func login(
        email: String,
        password: String,
        completion: @escaping (Result<APIResponse<User>, Error>) -> Void
    ) {
        client.post(
            endpoint: "auth/login.php",
            parameters: [
                "email": email,
                "password": password
            ],
            completion: completion
        )
    }

    /*
        Profil.

        PHP:
        auth/profile.php
    */
    func profile(
        apiToken: String,
        completion: @escaping (Result<APIResponse<User>, Error>) -> Void
    ) {
        client.post(
            endpoint: "auth/profile.php",
            parameters: [
                "api_token": apiToken
            ],
            completion: completion
        )
    }

    /*
        Şehir listeleme.

        PHP:
        locations/cities_list.php
    */
    func getCities(
        apiToken: String,
        completion: @escaping (Result<APIResponse<[City]>, Error>) -> Void
    ) {
        client.post(
            endpoint: "locations/cities_list.php",
            parameters: [
                "api_token": apiToken
            ],
            completion: completion
        )
    }

    /*
        Şehre göre ilçe listeleme.

        PHP:
        locations/districts_by_city.php
    */
    func getDistrictsByCity(
        apiToken: String,
        cityId: Int,
        completion: @escaping (Result<APIResponse<[District]>, Error>) -> Void
    ) {
        client.post(
            endpoint: "locations/districts_by_city.php",
            parameters: [
                "api_token": apiToken,
                "city_id": String(cityId)
            ],
            completion: completion
        )
    }

    /*
        İlçeye göre mekan listeleme.
        Şimdilik UI'da kullanmasak da endpoint hazır kalsın.

        PHP:
        locations/venues_by_district.php
    */
    func getVenuesByDistrict(
        apiToken: String,
        cityId: Int,
        districtId: Int,
        completion: @escaping (Result<APIResponse<[Venue]>, Error>) -> Void
    ) {
        client.post(
            endpoint: "locations/venues_by_district.php",
            parameters: [
                "api_token": apiToken,
                "city_id": String(cityId),
                "district_id": String(districtId)
            ],
            completion: completion
        )
    }

    /*
        Şehir + ilçe seçimine göre etkinlik listeleme.

        PHP:
        events/events_by_location.php
    */
    func getEventsByLocation(
        apiToken: String,
        cityId: Int,
        districtId: Int,
        completion: @escaping (Result<APIResponse<[Event]>, Error>) -> Void
    ) {
        client.post(
            endpoint: "events/events_by_location.php",
            parameters: [
                "api_token": apiToken,
                "city_id": String(cityId),
                "district_id": String(districtId)
            ],
            completion: completion
        )
    }

    /*
        Etkinlik detay.

        PHP:
        events/event_detail.php
    */
    func getEventDetail(
        apiToken: String,
        eventId: Int,
        completion: @escaping (Result<APIResponse<Event>, Error>) -> Void
    ) {
        client.post(
            endpoint: "events/event_detail.php",
            parameters: [
                "api_token": apiToken,
                "event_id": String(eventId)
            ],
            completion: completion
        )
    }

    /*
        Bilet satın alma.

        PHP:
        tickets/ticket_buy.php
    */
    func buyTicket(
        apiToken: String,
        eventId: Int,
        completion: @escaping (Result<APIResponse<Ticket>, Error>) -> Void
    ) {
        client.post(
            endpoint: "tickets/ticket_buy.php",
            parameters: [
                "api_token": apiToken,
                "event_id": String(eventId)
            ],
            completion: completion
        )
    }

    /*
        Kullanıcının biletleri.

        PHP:
        tickets/my_tickets.php
    */
    func getMyTickets(
        apiToken: String,
        completion: @escaping (Result<APIResponse<[Ticket]>, Error>) -> Void
    ) {
        client.post(
            endpoint: "tickets/my_tickets.php",
            parameters: [
                "api_token": apiToken
            ],
            completion: completion
        )
    }

    /*
        Tek bilet detayı.

        PHP:
        tickets/ticket_detail.php
    */
    func getTicketDetail(
        apiToken: String,
        ticketId: Int,
        completion: @escaping (Result<APIResponse<Ticket>, Error>) -> Void
    ) {
        client.post(
            endpoint: "tickets/ticket_detail.php",
            parameters: [
                "api_token": apiToken,
                "ticket_id": String(ticketId)
            ],
            completion: completion
        )
    }

    /*
        QR bilet kontrol.

        PHP:
        check/ticket_check.php

        Sadece staff/admin token ile kullanılmalı.
    */
    func checkTicket(
        apiToken: String,
        ticketCode: String,
        completion: @escaping (Result<APIResponse<Ticket>, Error>) -> Void
    ) {
        client.post(
            endpoint: "check/ticket_check.php",
            parameters: [
                "api_token": apiToken,
                "ticket_code": ticketCode
            ],
            completion: completion
        )
    }
}
