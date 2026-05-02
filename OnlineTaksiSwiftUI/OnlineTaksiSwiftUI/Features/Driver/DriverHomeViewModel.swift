//
//  DriverHomeViewModel.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation
import CoreLocation

@MainActor
final class DriverHomeViewModel: NSObject, ObservableObject {
    @Published var isOnline: Bool = false
    @Published var currentLatText: String = "-"
    @Published var currentLngText: String = "-"
    @Published var availableRides: [AvailableRideItem] = []
    @Published var activeRide: RideResponse?
    @Published var lastLog: String = "Hazır"
    @Published var message: String?
    @Published var isLoadingAvailableRides: Bool = false
    @Published var isAcceptingRide: Bool = false

    private let sessionManager: SessionManager
    private let driverRepository: DriverRepository
    private let socketManager: SocketManager
    private let locationManager = CLLocationManager()

    init(
        sessionManager: SessionManager,
        driverRepository: DriverRepository,
        socketManager: SocketManager = SocketManager()
    ) {
        self.sessionManager = sessionManager
        self.driverRepository = driverRepository
        self.socketManager = socketManager
        super.init()

        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        configureSocketCallbacks()
    }

    func connectSocket() {
        guard let token = sessionManager.token, !token.isEmpty else {
            message = "Token yok"
            lastLog = "Token yok"
            return
        }

        socketManager.connect(token: token)
        lastLog = "Socket bağlantısı başlatıldı"
    }

    func disconnectSocket() {
        socketManager.disconnect()
        lastLog = "Socket bağlantısı kapatıldı"
    }

    func setOnline(_ online: Bool) async {
        do {
            let response = try await driverRepository.setOnline(online)
            isOnline = response.is_online
            lastLog = online ? "Online oldun" : "Offline oldun"
            message = online ? "Online oldun" : "Offline oldun"
        } catch {
            message = error.localizedDescription
            lastLog = error.localizedDescription
        }
    }

    func loadAvailableRides() async {
        isLoadingAvailableRides = true

        do {
            let response = try await driverRepository.getAvailableRides()
            availableRides = response.rides
            lastLog = "Açık ride listesi güncellendi"
        } catch {
            message = error.localizedDescription
            lastLog = error.localizedDescription
        }

        isLoadingAvailableRides = false
    }

    func loadActiveRide() async {
        do {
            let response = try await driverRepository.getMyActiveRides()
            activeRide = response.rides.first
            lastLog = activeRide == nil ? "Aktif ride yok" : "Aktif ride güncellendi"
        } catch {
            message = error.localizedDescription
            lastLog = error.localizedDescription
        }
    }

    func acceptRide(_ rideId: Int) async {
        isAcceptingRide = true

        do {
            let response = try await driverRepository.acceptRide(rideId: rideId)
            activeRide = response
            availableRides.removeAll { $0.id == rideId }
            lastLog = "Ride kabul edildi. id=\(rideId)"
            message = "Ride kabul edildi"
        } catch {
            message = error.localizedDescription
            lastLog = error.localizedDescription
        }

        isAcceptingRide = false
    }

    func updateRideStatus(_ status: String, note: String? = nil) async {
        guard let activeRide else {
            message = "Aktif ride yok"
            lastLog = "Aktif ride yok"
            return
        }

        do {
            let response = try await driverRepository.updateRideStatus(
                rideId: activeRide.id,
                status: status,
                note: note
            )
            self.activeRide = response
            lastLog = "Status güncellendi: \(status)"
            message = "Status güncellendi: \(status)"
        } catch {
            message = error.localizedDescription
            lastLog = error.localizedDescription
        }
    }

    func startLocationTracking() {
        let status = locationManager.authorizationStatus

        switch status {
        case .notDetermined:
            locationManager.requestWhenInUseAuthorization()
        case .authorizedWhenInUse, .authorizedAlways:
            locationManager.startUpdatingLocation()
            lastLog = "Konum güncellemeleri başlatıldı"
        default:
            message = "Konum izni gerekli"
            lastLog = "Konum izni gerekli"
        }
    }

    func stopLocationTracking() {
        locationManager.stopUpdatingLocation()
        lastLog = "Konum güncellemeleri durduruldu"
    }

    func clearMessage() {
        message = nil
    }

    private func configureSocketCallbacks() {
        socketManager.onMessageReceived = { [weak self] message in
            guard let self else { return }

            self.lastLog = message

            guard let eventName = SocketEventParser.getEventName(message) else { return }

            if eventName == "NEW_RIDE_REQUEST",
               let ride = self.parseIncomingRide(message) {
                let exists = self.availableRides.contains { $0.id == ride.id }
                if !exists {
                    self.availableRides.insert(ride, at: 0)
                    self.message = "Yeni ride geldi"
                    self.lastLog = "Yeni ride geldi"
                }
            }
        }

        socketManager.onError = { [weak self] errorText in
            guard let self else { return }
            self.message = errorText
            self.lastLog = errorText
        }
    }

    private func parseIncomingRide(_ message: String) -> AvailableRideItem? {
        guard let data = message.data(using: .utf8),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let event = json["event"] as? String,
              event == "NEW_RIDE_REQUEST",
              let body = json["data"] as? [String: Any],
              let rideId = body["ride_id"] as? Int,
              let customerId = body["customer_id"] as? Int,
              let pickupLat = body["pickup_lat"] as? Double,
              let pickupLng = body["pickup_lng"] as? Double,
              let pickupAddress = body["pickup_address"] as? String,
              let dropoffLat = body["dropoff_lat"] as? Double,
              let dropoffLng = body["dropoff_lng"] as? Double,
              let dropoffAddress = body["dropoff_address"] as? String,
              let status = body["status"] as? String else {
            return nil
        }

        return AvailableRideItem(
            id: rideId,
            customer_id: customerId,
            pickup_lat: pickupLat,
            pickup_lng: pickupLng,
            pickup_address: pickupAddress,
            dropoff_lat: dropoffLat,
            dropoff_lng: dropoffLng,
            dropoff_address: dropoffAddress,
            status: status,
            estimated_fare: body["estimated_fare"] as? Double
        )
    }
}
extension DriverHomeViewModel: CLLocationManagerDelegate {
    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            let status = manager.authorizationStatus
            switch status {
            case .authorizedAlways, .authorizedWhenInUse:
                manager.startUpdatingLocation()
                self.lastLog = "Konum izni verildi"
            case .denied, .restricted:
                self.message = "Konum izni verilmedi"
                self.lastLog = "Konum izni verilmedi"
            default:
                break
            }
        }
    }

    nonisolated func locationManager(
        _ manager: CLLocationManager,
        didUpdateLocations locations: [CLLocation]
    ) {
        guard let location = locations.last else { return }

        Task { @MainActor in
            self.currentLatText = String(location.coordinate.latitude)
            self.currentLngText = String(location.coordinate.longitude)

            do {
                _ = try await self.driverRepository.updateLocation(
                    lat: location.coordinate.latitude,
                    lng: location.coordinate.longitude
                )
                self.lastLog = "Konum gönderildi"
            } catch {
                self.message = error.localizedDescription
                self.lastLog = error.localizedDescription
            }
        }
    }

    nonisolated func locationManager(
        _ manager: CLLocationManager,
        didFailWithError error: Error
    ) {
        Task { @MainActor in
            self.message = error.localizedDescription
            self.lastLog = error.localizedDescription
        }
    }
}
