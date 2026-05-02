//
//  DriverHomeViewController.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation
import UIKit
import CoreLocation
import MapKit
final class DriverHomeViewController: UIViewController {

    @IBOutlet weak var statusLabel: UILabel!
    @IBOutlet weak var locationLabel: UILabel!
    @IBOutlet weak var logLabel: UILabel!

    @IBOutlet weak var ridesTableView: UITableView!

    @IBOutlet weak var connectSocketButton: UIButton!
    @IBOutlet weak var onlineButton: UIButton!
    @IBOutlet weak var offlineButton: UIButton!
    @IBOutlet weak var loadRidesButton: UIButton!
    @IBOutlet weak var startLocationButton: UIButton!
    @IBOutlet weak var stopLocationButton: UIButton!
    @IBOutlet weak var logoutButton: UIButton!
    @IBOutlet weak var mapView: MKMapView!
    private let driverRepository = DriverRepository()
    private let socketManager = SocketManager()
    private let locationManager = CLLocationManager()

    private var driverAnnotation: MKPointAnnotation?
    private var pickupAnnotation: MKPointAnnotation?
    private var dropoffAnnotation: MKPointAnnotation?

    private var availableRides: [AvailableRideItem] = []
    private var activeRide: RideResponse?

    override func viewDidLoad() {
        super.viewDidLoad()

        ridesTableView.dataSource = self
        ridesTableView.delegate = self

        mapView.showsUserLocation = true

        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest

        statusLabel.text = "Offline"
        locationLabel.text = "Konum: -"
        logLabel.text = "Hazır"
        ridesTableView.allowsSelection = true
        setupDefaultMap()
        setupButtons()
        setupSocket()
    }

    private func setupDefaultMap() {
        let istanbul = CLLocationCoordinate2D(latitude: 41.0082, longitude: 28.9784)
        let region = MKCoordinateRegion(
            center: istanbul,
            span: MKCoordinateSpan(latitudeDelta: 0.15, longitudeDelta: 0.15)
        )
        mapView.setRegion(region, animated: false)
    }

    private func setupButtons() {
        connectSocketButton.addTarget(self, action: #selector(connectSocket), for: .touchUpInside)
        onlineButton.addTarget(self, action: #selector(setOnline), for: .touchUpInside)
        offlineButton.addTarget(self, action: #selector(setOffline), for: .touchUpInside)
        loadRidesButton.addTarget(self, action: #selector(loadRides), for: .touchUpInside)

        // Artık Start butonu:
        // aktif ride varsa Maps'e yönlendirir,
        // aktif ride yoksa konumu başlatır.
        startLocationButton.addTarget(self, action: #selector(startButtonTapped), for: .touchUpInside)

        stopLocationButton.addTarget(self, action: #selector(stopLocation), for: .touchUpInside)
        logoutButton.addTarget(self, action: #selector(logout), for: .touchUpInside)
    }

    private func setupSocket() {
        socketManager.onConnected = { [weak self] in
            guard let self else { return }
            self.logLabel.text = "Socket bağlandı"
            self.loadRides()
        }

        socketManager.onMessageReceived = { [weak self] message in
            self?.handleSocket(message)
        }

        socketManager.onError = { [weak self] error in
            self?.logLabel.text = error
        }
    }

    @objc private func connectSocket() {
        guard let token = SessionManager.shared.token else {
            logLabel.text = "Token bulunamadı"
            return
        }

        socketManager.connect(token: token)
        setOnline()
        loadRides()
        startLocation()
    }

    @objc private func setOnline() {
        driverRepository.setOnline(true) { [weak self] result in
            switch result {
            case .success:
                self?.statusLabel.text = "Online"
            case .failure(let error):
                self?.logLabel.text = error.localizedDescription
            }
        }
    }

    @objc private func setOffline() {
        driverRepository.setOnline(false) { [weak self] result in
            switch result {
            case .success:
                self?.statusLabel.text = "Offline"
            case .failure(let error):
                self?.logLabel.text = error.localizedDescription
            }
        }
    }

    @objc private func loadRides() {
        logLabel.text = "Açık ride listesi çekiliyor..."

        driverRepository.getAvailableRides { [weak self] result in
            guard let self else { return }

            switch result {
            case .success(let response):
                self.availableRides = response.rides
                self.ridesTableView.reloadData()
                self.logLabel.text = "Açık ride sayısı: \(response.rides.count)"

            case .failure(let error):
                self.logLabel.text = error.localizedDescription
            }
        }
    }

    @objc private func startButtonTapped() {
        guard let ride = activeRide else {
            startLocation()
            return
        }

        markRideOnMap(ride)
        showOpenMapsDialog(for: ride)
    }

    private func startLocation() {
        let status = locationManager.authorizationStatus

        switch status {
        case .notDetermined:
            locationManager.requestWhenInUseAuthorization()

        case .authorizedWhenInUse, .authorizedAlways:
            locationManager.startUpdatingLocation()
            logLabel.text = "Konum başlatıldı"

        case .denied, .restricted:
            logLabel.text = "Konum izni verilmedi"

        @unknown default:
            logLabel.text = "Konum izni bilinmiyor"
        }
    }

    @objc private func stopLocation() {
        locationManager.stopUpdatingLocation()
        logLabel.text = "Konum durduruldu"
    }

    @objc private func logout() {
        socketManager.disconnect()
        locationManager.stopUpdatingLocation()
        SessionManager.shared.clear()

        let vc = storyboard!.instantiateViewController(withIdentifier: "LoginVC")
        vc.modalPresentationStyle = .fullScreen
        present(vc, animated: true)
    }

    private func handleSocket(_ message: String) {
        logLabel.text = message

        if let event = SocketEventParser.getEventName(message),
           event == "NEW_RIDE_REQUEST",
           let ride = parseRide(message) {

            let exists = availableRides.contains { $0.id == ride.id }
            if !exists {
                availableRides.insert(ride, at: 0)
                ridesTableView.reloadData()
            }
        }
    }

    private func parseRide(_ message: String) -> AvailableRideItem? {
        guard let data = message.data(using: .utf8),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let body = json["data"] as? [String: Any] else {
            return nil
        }

        return AvailableRideItem(
            id: body["ride_id"] as? Int ?? 0,
            customer_id: body["customer_id"] as? Int ?? 0,
            pickup_lat: body["pickup_lat"] as? Double ?? 0.0,
            pickup_lng: body["pickup_lng"] as? Double ?? 0.0,
            pickup_address: body["pickup_address"] as? String ?? "",
            dropoff_lat: body["dropoff_lat"] as? Double ?? 0.0,
            dropoff_lng: body["dropoff_lng"] as? Double ?? 0.0,
            dropoff_address: body["dropoff_address"] as? String ?? "",
            status: body["status"] as? String ?? "",
            estimated_fare: body["estimated_fare"] as? Double
        )
    }

    private func updateDriverOnMap(_ location: CLLocation) {
        let coordinate = location.coordinate

        if driverAnnotation == nil {
            let annotation = MKPointAnnotation()
            annotation.title = "Benim Konumum"
            annotation.coordinate = coordinate
            driverAnnotation = annotation
            mapView.addAnnotation(annotation)
        } else {
            driverAnnotation?.coordinate = coordinate
        }

        fitMapToVisibleAnnotations()
    }

    private func markRideOnMap(_ ride: RideResponse) {
        if let pickupAnnotation {
            mapView.removeAnnotation(pickupAnnotation)
        }

        if let dropoffAnnotation {
            mapView.removeAnnotation(dropoffAnnotation)
        }

        let pickup = MKPointAnnotation()
        pickup.title = "Müşteri Alınış Noktası"
        pickup.subtitle = ride.pickup_address
        pickup.coordinate = CLLocationCoordinate2D(
            latitude: ride.pickup_lat,
            longitude: ride.pickup_lng
        )

        let dropoff = MKPointAnnotation()
        dropoff.title = "Varış Noktası"
        dropoff.subtitle = ride.dropoff_address
        dropoff.coordinate = CLLocationCoordinate2D(
            latitude: ride.dropoff_lat,
            longitude: ride.dropoff_lng
        )

        pickupAnnotation = pickup
        dropoffAnnotation = dropoff

        mapView.addAnnotations([pickup, dropoff])
        fitMapToVisibleAnnotations()

        logLabel.text = "Ride konumları haritada işaretlendi"
    }

    private func fitMapToVisibleAnnotations() {
        let annotations = [driverAnnotation, pickupAnnotation, dropoffAnnotation].compactMap { $0 }

        guard !annotations.isEmpty else { return }

        if annotations.count == 1, let first = annotations.first {
            let region = MKCoordinateRegion(
                center: first.coordinate,
                span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
            )
            mapView.setRegion(region, animated: true)
            return
        }

        var rect = MKMapRect.null

        for annotation in annotations {
            let point = MKMapPoint(annotation.coordinate)
            let pointRect = MKMapRect(x: point.x, y: point.y, width: 0.1, height: 0.1)
            rect = rect.union(pointRect)
        }

        mapView.setVisibleMapRect(
            rect,
            edgePadding: UIEdgeInsets(top: 70, left: 50, bottom: 70, right: 50),
            animated: true
        )
    }

    private func showOpenMapsDialog(for ride: RideResponse) {
        let alert = UIAlertController(
            title: "Yol Tarifi",
            message: "Müşteri alınış noktasına Apple Maps ile gitmek ister misin?",
            preferredStyle: .alert
        )

        alert.addAction(UIAlertAction(title: "İptal", style: .cancel))

        alert.addAction(UIAlertAction(title: "Maps ile Başlat", style: .default) { [weak self] _ in
            self?.openAppleMapsToPickup(ride)
        })

        present(alert, animated: true)
    }

    private func openAppleMapsToPickup(_ ride: RideResponse) {
        let coordinate = CLLocationCoordinate2D(
            latitude: ride.pickup_lat,
            longitude: ride.pickup_lng
        )

        let placemark = MKPlacemark(coordinate: coordinate)
        let mapItem = MKMapItem(placemark: placemark)
        mapItem.name = "Müşteri Alınış Noktası"

        mapItem.openInMaps(launchOptions: [
            MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving
        ])
    }
}

extension DriverHomeViewController: UITableViewDataSource, UITableViewDelegate {

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        availableRides.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {

        let cell = UITableViewCell(style: .subtitle, reuseIdentifier: "cell")
        let ride = availableRides[indexPath.row]

        cell.textLabel?.text = "Ride \(ride.id)"
        cell.detailTextLabel?.text = "Pickup: \(ride.pickup_address)"

        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)

        let ride = availableRides[indexPath.row]

        logLabel.text = "Ride seçildi: \(ride.id)"

        // Önce hemen haritada göster
        markAvailableRideOnMap(ride)

        // Hemen Maps seçeneği göster
        showOpenMapsDialogForAvailableRide(ride)

        // Sonra backend'e kabul isteği gönder
        driverRepository.acceptRide(rideId: ride.id) { [weak self] result in
            guard let self else { return }

            switch result {
            case .success(let response):
                self.activeRide = response
                self.availableRides.removeAll { $0.id == ride.id }
                self.ridesTableView.reloadData()
                self.logLabel.text = "Ride kabul edildi"

            case .failure(let error):
                self.logLabel.text = "Ride kabul hatası: \(error.localizedDescription)"
            }
        }
    }
    private func markAvailableRideOnMap(_ ride: AvailableRideItem) {
        if let pickupAnnotation {
            mapView.removeAnnotation(pickupAnnotation)
        }

        if let dropoffAnnotation {
            mapView.removeAnnotation(dropoffAnnotation)
        }

        let pickup = MKPointAnnotation()
        pickup.title = "Müşteri Alınış Noktası"
        pickup.subtitle = ride.pickup_address
        pickup.coordinate = CLLocationCoordinate2D(
            latitude: ride.pickup_lat,
            longitude: ride.pickup_lng
        )

        let dropoff = MKPointAnnotation()
        dropoff.title = "Varış Noktası"
        dropoff.subtitle = ride.dropoff_address
        dropoff.coordinate = CLLocationCoordinate2D(
            latitude: ride.dropoff_lat,
            longitude: ride.dropoff_lng
        )

        pickupAnnotation = pickup
        dropoffAnnotation = dropoff

        mapView.addAnnotations([pickup, dropoff])
        fitMapToVisibleAnnotations()

        logLabel.text = "Seçilen ride haritada gösterildi"
    }
    private func showOpenMapsDialogForAvailableRide(_ ride: AvailableRideItem) {
        let alert = UIAlertController(
            title: "Yol Tarifi",
            message: "Müşteri alınış noktasına Apple Maps ile gitmek ister misin?",
            preferredStyle: .alert
        )

        alert.addAction(UIAlertAction(title: "İptal", style: .cancel))

        alert.addAction(UIAlertAction(title: "Maps ile Başlat", style: .default) { [weak self] _ in
            self?.openAppleMapsToAvailableRidePickup(ride)
        })

        present(alert, animated: true)
    }
    private func openAppleMapsToAvailableRidePickup(_ ride: AvailableRideItem) {
        let coordinate = CLLocationCoordinate2D(
            latitude: ride.pickup_lat,
            longitude: ride.pickup_lng
        )

        let placemark = MKPlacemark(coordinate: coordinate)
        let mapItem = MKMapItem(placemark: placemark)
        mapItem.name = "Müşteri Alınış Noktası"

        mapItem.openInMaps(launchOptions: [
            MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving
        ])
    }
}

extension DriverHomeViewController: CLLocationManagerDelegate {

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let status = manager.authorizationStatus

        if status == .authorizedWhenInUse || status == .authorizedAlways {
            locationManager.startUpdatingLocation()
            logLabel.text = "Konum izni verildi"
        } else if status == .denied || status == .restricted {
            logLabel.text = "Konum izni reddedildi"
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }

        locationLabel.text =
        "Konum: \(location.coordinate.latitude), \(location.coordinate.longitude)"

        updateDriverOnMap(location)

        driverRepository.updateLocation(
            lat: location.coordinate.latitude,
            lng: location.coordinate.longitude
        ) { [weak self] result in
            switch result {
            case .success:
                self?.logLabel.text = "Konum backend'e gönderildi"
            case .failure(let error):
                self?.logLabel.text = error.localizedDescription
            }
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        logLabel.text = "Konum hatası: \(error.localizedDescription)"
    }
}
