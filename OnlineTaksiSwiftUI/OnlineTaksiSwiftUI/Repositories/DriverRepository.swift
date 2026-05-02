//
//  DriverRepository.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation

final class DriverRepository {
    private let apiClient: APIClient

    init(apiClient: APIClient) {
        self.apiClient = apiClient
    }

    func updateLocation(lat: Double, lng: Double) async throws -> DriverProfileResponse {
        let request = DriverLocationUpdateRequest(lat: lat, lng: lng)
        return try await apiClient.request(
            endpoint: .driverLocation,
            method: "PUT",
            body: request,
            responseType: DriverProfileResponse.self
        )
    }

    func setOnline(_ isOnline: Bool) async throws -> DriverProfileResponse {
        let request = DriverOnlineStatusRequest(is_online: isOnline)
        return try await apiClient.request(
            endpoint: .driverOnlineStatus,
            method: "PUT",
            body: request,
            responseType: DriverProfileResponse.self
        )
    }

    func getAvailableRides() async throws -> AvailableRideListResponse {
        return try await apiClient.request(
            endpoint: .availableRides,
            method: "GET",
            body: Optional<EmptyBody>.none,
            responseType: AvailableRideListResponse.self
        )
    }

    func acceptRide(rideId: Int) async throws -> RideResponse {
        return try await apiClient.request(
            endpoint: .acceptRide(rideId),
            method: "PUT",
            body: Optional<EmptyBody>.none,
            responseType: RideResponse.self
        )
    }

    func getMyActiveRides() async throws -> RideListResponse {
        return try await apiClient.request(
            endpoint: .activeRides,
            method: "GET",
            body: Optional<EmptyBody>.none,
            responseType: RideListResponse.self
        )
    }

    func updateRideStatus(
        rideId: Int,
        status: String,
        note: String? = nil
    ) async throws -> RideResponse {
        let request = UpdateRideStatusRequest(status: status, note: note)
        return try await apiClient.request(
            endpoint: .updateRideStatus(rideId),
            method: "PUT",
            body: request,
            responseType: RideResponse.self
        )
    }
}
