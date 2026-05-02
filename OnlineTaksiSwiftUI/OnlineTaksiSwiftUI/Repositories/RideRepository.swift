//
//  RideRepository.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation

final class RideRepository {
    private let apiClient: APIClient

    init(apiClient: APIClient) {
        self.apiClient = apiClient
    }

    func createRide(
        pickupLat: Double,
        pickupLng: Double,
        pickupAddress: String,
        dropoffLat: Double,
        dropoffLng: Double,
        dropoffAddress: String
    ) async throws -> RideResponse {
        let request = CreateRideRequest(
            pickup_lat: pickupLat,
            pickup_lng: pickupLng,
            pickup_address: pickupAddress,
            dropoff_lat: dropoffLat,
            dropoff_lng: dropoffLng,
            dropoff_address: dropoffAddress
        )

        return try await apiClient.request(
            endpoint: .createRide,
            method: "POST",
            body: request,
            responseType: RideResponse.self
        )
    }
}
