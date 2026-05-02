//
//  DriverRepository.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation

final class DriverRepository {

    func setOnline(_ isOnline: Bool, completion: @escaping (Result<DriverProfileResponse, Error>) -> Void) {
        let request = DriverOnlineStatusRequest(is_online: isOnline)

        APIClient.shared.request(
            endpoint: .driverOnlineStatus,
            method: "PUT",
            body: request,
            responseType: DriverProfileResponse.self,
            completion: completion
        )
    }

    func getAvailableRides(completion: @escaping (Result<AvailableRideListResponse, Error>) -> Void) {
        APIClient.shared.request(
            endpoint: .availableRides,
            method: "GET",
            body: Optional<EmptyBody>.none,
            responseType: AvailableRideListResponse.self,
            completion: completion
        )
    }

    func acceptRide(rideId: Int, completion: @escaping (Result<RideResponse, Error>) -> Void) {
        APIClient.shared.request(
            endpoint: .acceptRide(rideId),
            method: "PUT",
            body: Optional<EmptyBody>.none,
            responseType: RideResponse.self,
            completion: completion
        )
    }

    func updateLocation(lat: Double, lng: Double, completion: @escaping (Result<DriverProfileResponse, Error>) -> Void) {
        let request = DriverLocationUpdateRequest(lat: lat, lng: lng)

        APIClient.shared.request(
            endpoint: .driverLocation,
            method: "PUT",
            body: request,
            responseType: DriverProfileResponse.self,
            completion: completion
        )
    }

    func updateRideStatus(rideId: Int, status: String, completion: @escaping (Result<RideResponse, Error>) -> Void) {
        let request = UpdateRideStatusRequest(status: status, note: nil)

        APIClient.shared.request(
            endpoint: .updateRideStatus(rideId),
            method: "PUT",
            body: request,
            responseType: RideResponse.self,
            completion: completion
        )
    }
}
