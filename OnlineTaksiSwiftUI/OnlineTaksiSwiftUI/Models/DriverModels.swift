//
//  DriverModels.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation

struct DriverLocationUpdateRequest: Encodable {
    let lat: Double
    let lng: Double
}

struct DriverProfileResponse: Decodable {
    let user_id: Int
    let is_online: Bool
    let current_lat: Double?
    let current_lng: Double?
}

struct AvailableRideItem: Decodable, Identifiable {
    let id: Int
    let customer_id: Int
    let pickup_lat: Double
    let pickup_lng: Double
    let pickup_address: String
    let dropoff_lat: Double
    let dropoff_lng: Double
    let dropoff_address: String
    let status: String
    let estimated_fare: Double?
}

struct AvailableRideListResponse: Decodable {
    let rides: [AvailableRideItem]
}

struct RideListResponse: Decodable {
    let rides: [RideResponse]
}

struct DriverOnlineStatusRequest: Encodable {
    let is_online: Bool
}

struct UpdateRideStatusRequest: Encodable {
    let status: String
    let note: String?
}
