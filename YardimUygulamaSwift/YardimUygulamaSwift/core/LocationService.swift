//
//  LocationService.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 1.03.2026.
//

import Foundation
import Foundation
import CoreLocation

final class LocationService: NSObject, CLLocationManagerDelegate {
    static let shared = LocationService()

    private let manager = CLLocationManager()
    private let geocoder = CLGeocoder()

    private var onResult: ((Double, Double, String?, String?) -> Void)?
    private var onFail: ((String) -> Void)?

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
    }

    func requestOnce(
        onSuccess: @escaping (Double, Double, String?, String?) -> Void,
        onFailure: @escaping (String) -> Void
    ) {
        self.onResult = onSuccess
        self.onFail = onFailure

        let st = manager.authorizationStatus
        if st == .notDetermined {
            manager.requestWhenInUseAuthorization()
            return
        }
        if st == .denied || st == .restricted {
            onFailure("Konum izni verilmedi")
            return
        }
        manager.requestLocation()
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let st = manager.authorizationStatus
        if st == .authorizedWhenInUse || st == .authorizedAlways {
            manager.requestLocation()
        } else if st == .denied || st == .restricted {
            onFail?("Konum izni verilmedi")
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.first else {
            onFail?("Konum alınamadı")
            return
        }
        let lat = loc.coordinate.latitude
        let lng = loc.coordinate.longitude

        geocoder.reverseGeocodeLocation(loc) { [weak self] placemarks, _ in
            guard let self else { return }
            let p = placemarks?.first
            let city = p?.administrativeArea
            let district = p?.subAdministrativeArea ?? p?.locality
            self.onResult?(lat, lng, city, district)
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        onFail?("Konum hatası: \(error.localizedDescription)")
    }
}
