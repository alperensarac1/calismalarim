import Foundation
import CoreLocation

final class LocationService: NSObject, CLLocationManagerDelegate {
    private let mgr = CLLocationManager()
    private var cb: ((CLLocation)->Void)?

    override init() {
        super.init()
        mgr.delegate = self
        mgr.desiredAccuracy = kCLLocationAccuracyBest
    }

    func requestAuth() {
        if CLLocationManager.authorizationStatus() == .notDetermined {
            mgr.requestWhenInUseAuthorization()
        }
    }

    func getOnce(_ completion: @escaping (CLLocation)->Void) {
        self.cb = completion
        mgr.requestLocation()
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let l = locations.last { cb?(l); cb = nil }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        // fallback: authorize değilse vs
    }
}
