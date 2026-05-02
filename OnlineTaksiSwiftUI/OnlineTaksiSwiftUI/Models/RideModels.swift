//
//  RideModels.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation

struct CreateRideRequest: Encodable {
    let pickup_lat: Double
    let pickup_lng: Double
    let pickup_address: String
    let dropoff_lat: Double
    let dropoff_lng: Double
    let dropoff_address: String
}

struct RideResponse: Decodable, Identifiable {
    let id: Int
    let customer_id: Int
    let assigned_driver_id: Int?
    let pickup_lat: Double
    let pickup_lng: Double
    let pickup_address: String
    let dropoff_lat: Double
    let dropoff_lng: Double
    let dropoff_address: String
    let status: String
    let estimated_fare: Double?
    let final_fare: Double?
    let cancel_reason: String?
}
