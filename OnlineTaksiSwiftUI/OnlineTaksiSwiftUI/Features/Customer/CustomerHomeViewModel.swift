import Foundation
import CoreLocation

@MainActor
final class CustomerHomeViewModel: ObservableObject {
    @Published var socketConnected: Bool = false
    @Published var rideStatus: String = "Aktif ride bilgisi yok"
    @Published var lastSocketEvent: String = "Henüz event yok"
    @Published var driverLatText: String = "-"
    @Published var driverLngText: String = "-"
    @Published var lastLocationUpdateText: String = "Henüz konum güncellemesi yok"

    @Published var pickupLat: String = ""
    @Published var pickupLng: String = ""
    @Published var pickupAddress: String = ""

    @Published var dropoffLat: String = ""
    @Published var dropoffLng: String = ""
    @Published var dropoffAddress: String = ""

    @Published var activeRide: RideResponse?
    @Published var createRideState: ResourceState<String> = .idle

    private let sessionManager: SessionManager
    private let rideRepository: RideRepository
    private let socketManager: SocketManager

    private var driverLatitude: Double?
    private var driverLongitude: Double?

    init(
        sessionManager: SessionManager,
        rideRepository: RideRepository,
        socketManager: SocketManager = SocketManager()
    ) {
        self.sessionManager = sessionManager
        self.rideRepository = rideRepository
        self.socketManager = socketManager

        configureSocketCallbacks()
    }

    var mapPoints: [CustomerMapPoint] {
        var points: [CustomerMapPoint] = []

        if let ride = activeRide {
            points.append(
                CustomerMapPoint(
                    id: "pickup",
                    title: "Alınış Noktası",
                    subtitle: ride.pickup_address,
                    latitude: ride.pickup_lat,
                    longitude: ride.pickup_lng,
                    type: .pickup
                )
            )

            points.append(
                CustomerMapPoint(
                    id: "dropoff",
                    title: "Varış Noktası",
                    subtitle: ride.dropoff_address,
                    latitude: ride.dropoff_lat,
                    longitude: ride.dropoff_lng,
                    type: .dropoff
                )
            )
        }

        if let lat = driverLatitude, let lng = driverLongitude {
            points.append(
                CustomerMapPoint(
                    id: "driver",
                    title: "Taksiniz",
                    subtitle: "Canlı konum",
                    latitude: lat,
                    longitude: lng,
                    type: .driver
                )
            )
        }

        return points
    }

    func connectSocket() {
        guard let token = sessionManager.token, !token.isEmpty else {
            lastSocketEvent = "Token bulunamadı"
            return
        }

        socketManager.connect(token: token)
        socketConnected = true
    }

    func disconnectSocket() {
        socketManager.disconnect()
        socketConnected = false
    }

    func sendPing() {
        socketManager.sendPing()
    }

    func logout() {
        disconnectSocket()
        sessionManager.clear()
    }

    func createRide() async {
        guard let pickupLatValue = Double(pickupLat),
              let pickupLngValue = Double(pickupLng),
              let dropoffLatValue = Double(dropoffLat),
              let dropoffLngValue = Double(dropoffLng),
              !pickupAddress.trimmingCharacters(in: .whitespaces).isEmpty,
              !dropoffAddress.trimmingCharacters(in: .whitespaces).isEmpty else {
            createRideState = .failure("Tüm pickup/dropoff alanlarını doğru doldur")
            return
        }

        createRideState = .loading

        do {
            let ride = try await rideRepository.createRide(
                pickupLat: pickupLatValue,
                pickupLng: pickupLngValue,
                pickupAddress: pickupAddress.trimmingCharacters(in: .whitespaces),
                dropoffLat: dropoffLatValue,
                dropoffLng: dropoffLngValue,
                dropoffAddress: dropoffAddress.trimmingCharacters(in: .whitespaces)
            )

            activeRide = ride
            rideStatus = ride.status
            createRideState = .success("Taksi çağrısı oluşturuldu")
        } catch {
            createRideState = .failure(error.localizedDescription)
        }
    }

    private func configureSocketCallbacks() {
        socketManager.onMessageReceived = { [weak self] message in
            guard let self else { return }

            self.lastSocketEvent = message

            if let status = SocketEventParser.parseRideStatus(message) {
                self.rideStatus = status
            }

            if let location = SocketEventParser.parseDriverLocation(message) {
                self.driverLatText = String(location.lat)
                self.driverLngText = String(location.lng)
                self.lastLocationUpdateText = Self.currentTimeString()
                self.driverLatitude = location.lat
                self.driverLongitude = location.lng
            }
        }

        socketManager.onError = { [weak self] errorText in
            guard let self else { return }
            self.socketConnected = false
            self.lastSocketEvent = errorText
        }
    }

    func clearMessage() {
        messageReset()
    }

    private func messageReset() {
        createRideState = .idle
    }

    private static func currentTimeString() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm:ss"
        return formatter.string(from: Date())
    }
}
