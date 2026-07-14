//
//  Event.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation

/*
    Event

    Etkinlik modelidir.

    Bazı endpointlerde tüm alanlar dönmeyebilir.
    Bu yüzden birçok alan optional tanımlandı.
*/
struct Event: Codable, Identifiable, Hashable {
    let id: Int

    let cityId: Int?
    let districtId: Int?
    let venueId: Int?

    let title: String
    let description: String?
    let posterUrl: String?
    let eventDate: String?

    let basePrice: Double?
    let totalQuota: Int?
    let soldCount: Int?
    let remainingQuota: Int?

    let cityName: String?
    let districtName: String?

    let venue: Venue?
    let city: City?
    let district: District?

    let createdAt: String?

    enum CodingKeys: String, CodingKey {
        case id
        case cityId = "city_id"
        case districtId = "district_id"
        case venueId = "venue_id"
        case title
        case description
        case posterUrl = "poster_url"
        case eventDate = "event_date"
        case basePrice = "base_price"
        case totalQuota = "total_quota"
        case soldCount = "sold_count"
        case remainingQuota = "remaining_quota"
        case cityName = "city_name"
        case districtName = "district_name"
        case venue
        case city
        case district
        case createdAt = "created_at"
    }
}
