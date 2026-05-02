//
//  RideRepository.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation

final class RideRepository {

    func createRide(
        pickupLat: Double,
        pickupLng: Double,
        pickupAddress: String,
        dropoffLat: Double,
        dropoffLng: Double,
        dropoffAddress: String,
        completion: @escaping (Result<RideResponse, Error>) -> Void
    ) {
        let request = CreateRideRequest(
            pickup_lat: pickupLat,
            pickup_lng: pickupLng,
            pickup_address: pickupAddress,
            dropoff_lat: dropoffLat,
            dropoff_lng: dropoffLng,
            dropoff_address: dropoffAddress
        )

        APIClient.shared.request(
            endpoint: .createRide,
            method: "POST",
            body: request,
            responseType: RideResponse.self,
            completion: completion
        )
    }
}
