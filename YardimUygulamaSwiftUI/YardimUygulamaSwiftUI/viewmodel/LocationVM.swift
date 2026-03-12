//
//  LocationVM.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation
import Foundation
import CoreLocation

@MainActor
final class LocationVM: NSObject, ObservableObject, CLLocationManagerDelegate {

    @Published var statusText: String = "Konum: tespit edilmedi"
    @Published var lat: Double?
    @Published var lng: Double?
    @Published var city: String?
    @Published var district: String?

    private let manager = CLLocationManager()
    private let geocoder = CLGeocoder()

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
    }

    func requestAndFetch() {
        let st = manager.authorizationStatus
        if st == .notDetermined {
            manager.requestWhenInUseAuthorization()
            return
        }
        if st == .denied || st == .restricted {
            statusText = "Konum izni verilmedi"
            return
        }
        manager.requestLocation()
        statusText = "Konum alınıyor..."
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        if manager.authorizationStatus == .authorizedWhenInUse || manager.authorizationStatus == .authorizedAlways {
            manager.requestLocation()
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.first else {
            statusText = "Konum alınamadı"
            return
        }
        lat = loc.coordinate.latitude
        lng = loc.coordinate.longitude
        statusText = "Konum: \(lat!), \(lng!)"

        geocoder.reverseGeocodeLocation(loc) { [weak self] placemarks, _ in
            guard let self else { return }
            let p = placemarks?.first
            // Türkiye’de genelde:
            // administrativeArea = İl, subAdministrativeArea = İlçe
            self.city = p?.administrativeArea
            self.district = p?.subAdministrativeArea ?? p?.locality
            if let c = self.city, let d = self.district {
                self.statusText = "Tespit edilen: \(c) / \(d)"
            } else {
                self.statusText = "Şehir/ilçe tespit edilemedi"
            }
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        statusText = "Konum hatası: \(error.localizedDescription)"
    }
}
