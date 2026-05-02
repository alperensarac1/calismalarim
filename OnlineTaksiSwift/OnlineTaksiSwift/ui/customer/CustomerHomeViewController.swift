//
//  CustomerHomeViewController.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation
import UIKit
import MapKit

final class CustomerHomeViewController: UIViewController {

    @IBOutlet weak var welcomeLabel: UILabel!
    @IBOutlet weak var socketStatusLabel: UILabel!
    @IBOutlet weak var rideStatusLabel: UILabel!
    @IBOutlet weak var driverLocationLabel: UILabel!

    @IBOutlet weak var mapView: MKMapView!

    @IBOutlet weak var pickupLatTextField: UITextField!
    @IBOutlet weak var pickupLngTextField: UITextField!
    @IBOutlet weak var pickupAddressTextField: UITextField!

    @IBOutlet weak var dropoffLatTextField: UITextField!
    @IBOutlet weak var dropoffLngTextField: UITextField!
    @IBOutlet weak var dropoffAddressTextField: UITextField!

    @IBOutlet weak var connectSocketButton: UIButton!
    @IBOutlet weak var pingButton: UIButton!
    @IBOutlet weak var createRideButton: UIButton!
    @IBOutlet weak var logoutButton: UIButton!

    private let rideRepository = RideRepository()
    private let socketManager = SocketManager()

    private var activeRide: RideResponse?

    private var pickupAnnotation: CustomerMapAnnotation?
    private var dropoffAnnotation: CustomerMapAnnotation?
    private var driverAnnotation: CustomerMapAnnotation?

    override func viewDidLoad() {
        super.viewDidLoad()

        mapView.delegate = self

        welcomeLabel.text = "Hoş geldin, \(SessionManager.shared.fullName ?? "Müşteri")"
        socketStatusLabel.text = "Socket: Bağlı değil"
        rideStatusLabel.text = "Ride Durumu: Aktif ride yok"
        driverLocationLabel.text = "Taksi Konumu: -"

        setupDefaultMap()
        setupButtons()
        setupSocketCallbacks()
    }

    private func setupButtons() {
        connectSocketButton.addTarget(self, action: #selector(connectSocketTapped), for: .touchUpInside)
        pingButton.addTarget(self, action: #selector(pingTapped), for: .touchUpInside)
        createRideButton.addTarget(self, action: #selector(createRideTapped), for: .touchUpInside)
        logoutButton.addTarget(self, action: #selector(logoutTapped), for: .touchUpInside)
    }

    private func setupSocketCallbacks() {
        socketManager.onConnected = { [weak self] in
            self?.socketStatusLabel.text = "Socket: Bağlı"
        }

        socketManager.onDisconnected = { [weak self] in
            self?.socketStatusLabel.text = "Socket: Bağlı değil"
        }

        socketManager.onError = { [weak self] error in
            self?.showAlert(error)
            self?.socketStatusLabel.text = "Socket: Hata"
        }

        socketManager.onMessageReceived = { [weak self] message in
            self?.handleSocketMessage(message)
        }
    }

    @objc private func connectSocketTapped() {
        guard let token = SessionManager.shared.token else {
            showAlert("Token bulunamadı")
            return
        }

        socketManager.connect(token: token)
    }

    @objc private func pingTapped() {
        socketManager.sendPing()
    }

    @objc private func createRideTapped() {
        guard let pickupLat = Double(pickupLatTextField.text ?? ""),
              let pickupLng = Double(pickupLngTextField.text ?? ""),
              let dropoffLat = Double(dropoffLatTextField.text ?? ""),
              let dropoffLng = Double(dropoffLngTextField.text ?? "") else {
            showAlert("Enlem ve boylam değerlerini doğru gir")
            return
        }

        let pickupAddress = pickupAddressTextField.text?.trimmingCharacters(in: .whitespaces) ?? ""
        let dropoffAddress = dropoffAddressTextField.text?.trimmingCharacters(in: .whitespaces) ?? ""

        guard !pickupAddress.isEmpty, !dropoffAddress.isEmpty else {
            showAlert("Adres alanları boş olamaz")
            return
        }

        createRideButton.isEnabled = false
        createRideButton.setTitle("Çağırılıyor...", for: .normal)

        rideRepository.createRide(
            pickupLat: pickupLat,
            pickupLng: pickupLng,
            pickupAddress: pickupAddress,
            dropoffLat: dropoffLat,
            dropoffLng: dropoffLng,
            dropoffAddress: dropoffAddress
        ) { [weak self] result in
            guard let self else { return }

            self.createRideButton.isEnabled = true
            self.createRideButton.setTitle("Taksi Çağır", for: .normal)

            switch result {
            case .success(let ride):
                self.activeRide = ride
                self.rideStatusLabel.text = "Ride Durumu: \(ride.status)"
                self.updatePickupAndDropoffAnnotations(ride)
                self.showAlert("Taksi çağrısı oluşturuldu")

            case .failure(let error):
                self.showAlert(error.localizedDescription)
            }
        }
    }

    @objc private func logoutTapped() {
        socketManager.disconnect()
        SessionManager.shared.clear()

        let vc = storyboard!.instantiateViewController(withIdentifier: "LoginVC")
        vc.modalPresentationStyle = .fullScreen
        present(vc, animated: true)
    }

    private func handleSocketMessage(_ message: String) {
        let eventName = SocketEventParser.getEventName(message)

        if eventName == "RIDE_ACCEPTED" ||
            eventName == "RIDE_STATUS_CHANGED" ||
            eventName == "RIDE_CANCELLED" {

            if let status = SocketEventParser.parseRideStatus(message) {
                rideStatusLabel.text = "Ride Durumu: \(status)"
            }
        }

        if eventName == "DRIVER_LOCATION" {
            if let location = SocketEventParser.parseDriverLocation(message) {
                driverLocationLabel.text = "Taksi Konumu: \(location.lat), \(location.lng)"
                updateDriverAnnotation(lat: location.lat, lng: location.lng)
            }
        }
    }

    private func setupDefaultMap() {
        let istanbul = CLLocationCoordinate2D(latitude: 41.0082, longitude: 28.9784)
        let region = MKCoordinateRegion(
            center: istanbul,
            span: MKCoordinateSpan(latitudeDelta: 0.2, longitudeDelta: 0.2)
        )
        mapView.setRegion(region, animated: false)
    }

    private func updatePickupAndDropoffAnnotations(_ ride: RideResponse) {
        if let pickupAnnotation {
            mapView.removeAnnotation(pickupAnnotation)
        }

        if let dropoffAnnotation {
            mapView.removeAnnotation(dropoffAnnotation)
        }

        let pickup = CustomerMapAnnotation(
            type: .pickup,
            coordinate: CLLocationCoordinate2D(latitude: ride.pickup_lat, longitude: ride.pickup_lng),
            title: "Alınış Noktası",
            subtitle: ride.pickup_address
        )

        let dropoff = CustomerMapAnnotation(
            type: .dropoff,
            coordinate: CLLocationCoordinate2D(latitude: ride.dropoff_lat, longitude: ride.dropoff_lng),
            title: "Varış Noktası",
            subtitle: ride.dropoff_address
        )

        pickupAnnotation = pickup
        dropoffAnnotation = dropoff

        mapView.addAnnotations([pickup, dropoff])
        fitMapToAnnotations()
    }

    private func updateDriverAnnotation(lat: Double, lng: Double) {
        let coordinate = CLLocationCoordinate2D(latitude: lat, longitude: lng)

        if let driverAnnotation {
            driverAnnotation.coordinate = coordinate
        } else {
            let annotation = CustomerMapAnnotation(
                type: .driver,
                coordinate: coordinate,
                title: "Taksiniz",
                subtitle: "Canlı konum"
            )

            driverAnnotation = annotation
            mapView.addAnnotation(annotation)
        }

        fitMapToAnnotations()
    }

    private func fitMapToAnnotations() {
        let annotations = [pickupAnnotation, dropoffAnnotation, driverAnnotation].compactMap { $0 }

        guard !annotations.isEmpty else {
            setupDefaultMap()
            return
        }

        if annotations.count == 1, let first = annotations.first {
            let region = MKCoordinateRegion(
                center: first.coordinate,
                span: MKCoordinateSpan(latitudeDelta: 0.02, longitudeDelta: 0.02)
            )
            mapView.setRegion(region, animated: true)
            return
        }

        var rect = MKMapRect.null

        annotations.forEach { annotation in
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

    private func showAlert(_ message: String) {
        let alert = UIAlertController(title: "Bilgi", message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(alert, animated: true)
    }
}

extension CustomerHomeViewController: MKMapViewDelegate {

    func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
        guard let annotation = annotation as? CustomerMapAnnotation else {
            return nil
        }

        let identifier = "CustomerAnnotation"
        var view = mapView.dequeueReusableAnnotationView(withIdentifier: identifier) as? MKMarkerAnnotationView

        if view == nil {
            view = MKMarkerAnnotationView(annotation: annotation, reuseIdentifier: identifier)
        } else {
            view?.annotation = annotation
        }

        view?.canShowCallout = true

        switch annotation.type {
        case .pickup:
            view?.markerTintColor = .systemGreen
            view?.glyphImage = UIImage(systemName: "mappin.circle.fill")
        case .dropoff:
            view?.markerTintColor = .systemRed
            view?.glyphImage = UIImage(systemName: "flag.fill")
        case .driver:
            view?.markerTintColor = .systemOrange
            view?.glyphImage = UIImage(systemName: "car.fill")
        }

        return view
    }
}
