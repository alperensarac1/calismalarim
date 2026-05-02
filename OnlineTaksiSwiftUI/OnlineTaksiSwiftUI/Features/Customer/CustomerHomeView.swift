import SwiftUI

struct CustomerHomeView: View {
    @EnvironmentObject var sessionManager: SessionManager
    @EnvironmentObject var router: AppRouter

    @StateObject private var viewModel: CustomerHomeViewModel

    init() {
        let tempSession = SessionManager()
        let apiClient = APIClient(sessionManager: tempSession)
        let rideRepository = RideRepository(apiClient: apiClient)
        _viewModel = StateObject(
            wrappedValue: CustomerHomeViewModel(
                sessionManager: tempSession,
                rideRepository: rideRepository
            )
        )
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                headerSection
                socketSection
                Divider()
                mapSection
                Divider()
                formSection
                Divider()
                activeRideSection
                Divider()
                logoutSection
            }
            .padding(20)
        }
        .onAppear {
            sessionManager.load()
        }
        .alert(
            "Bilgi",
            isPresented: Binding(
                get: {
                    switch viewModel.createRideState {
                    case .success, .failure:
                        return true
                    default:
                        return false
                    }
                },
                set: { newValue in
                    if !newValue {
                        viewModel.createRideState = .idle
                    }
                }
            )
        ) {
            Button("Tamam", role: .cancel) {
                viewModel.createRideState = .idle
            }
        } message: {
            switch viewModel.createRideState {
            case .success(let message):
                Text(message)
            case .failure(let error):
                Text(error)
            default:
                Text("")
            }
        }
    }
}

// MARK: - Sections
private extension CustomerHomeView {

    var headerSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Customer Home")
                .font(.largeTitle)
                .bold()

            Text("Socket: \(viewModel.socketConnected ? "Bağlı" : "Bağlı değil")")
            Text("Ride Durumu: \(viewModel.rideStatus)")
            Text("Taksi Enlem: \(viewModel.driverLatText)")
            Text("Taksi Boylam: \(viewModel.driverLngText)")
            Text("Son Konum Güncelleme: \(viewModel.lastLocationUpdateText)")
            Text("Son Event: \(viewModel.lastSocketEvent)")
                .font(.footnote)
        }
    }

    var socketSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Button("Socket Bağlan") {
                viewModel.connectSocket()
            }
            .buttonStyle(.borderedProminent)

            Button("Ping Gönder") {
                viewModel.sendPing()
            }
            .buttonStyle(.bordered)
        }
    }

    var mapSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Harita")
                .font(.title3)
                .bold()

            CustomerMapView(points: viewModel.mapPoints)
        }
    }

    var formSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Pickup / Dropoff Bilgileri")
                .font(.title3)
                .bold()

            pickupFields
            dropoffFields
            createRideButton
        }
    }

    var pickupFields: some View {
        VStack(alignment: .leading, spacing: 8) {
            TextField("Pickup Enlem", text: $viewModel.pickupLat)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.decimalPad)

            TextField("Pickup Boylam", text: $viewModel.pickupLng)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.decimalPad)

            TextField("Pickup Adres", text: $viewModel.pickupAddress)
                .textFieldStyle(.roundedBorder)
        }
    }

    var dropoffFields: some View {
        VStack(alignment: .leading, spacing: 8) {
            TextField("Dropoff Enlem", text: $viewModel.dropoffLat)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.decimalPad)

            TextField("Dropoff Boylam", text: $viewModel.dropoffLng)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.decimalPad)

            TextField("Dropoff Adres", text: $viewModel.dropoffAddress)
                .textFieldStyle(.roundedBorder)
        }
    }

    var createRideButton: some View {
        Button {
            Task {
                await viewModel.createRide()
            }
        } label: {
            createRideButtonContent
        }
        .buttonStyle(.borderedProminent)
    }

    @ViewBuilder
    var createRideButtonContent: some View {
        switch viewModel.createRideState {
        case .loading:
            ProgressView()
                .frame(maxWidth: .infinity)
        default:
            Text("Taksi Çağır")
                .frame(maxWidth: .infinity)
        }
    }

    @ViewBuilder
    var activeRideSection: some View {
        if let activeRide = viewModel.activeRide {
            VStack(alignment: .leading, spacing: 8) {
                Text("Aktif Ride")
                    .font(.title3)
                    .bold()

                Text("Ride ID: \(activeRide.id)")
                Text("Pickup: \(activeRide.pickup_address)")
                Text("Dropoff: \(activeRide.dropoff_address)")
                Text("Durum: \(activeRide.status)")
            }
        } else {
            Text("Aktif ride yok")
        }
    }

    var logoutSection: some View {
        Button("Çıkış Yap") {
            viewModel.logout()
            router.route = .login
        }
        .buttonStyle(.bordered)
    }
}
