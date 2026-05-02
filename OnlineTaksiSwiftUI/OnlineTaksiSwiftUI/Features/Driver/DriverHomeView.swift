import Foundation
import SwiftUI

struct DriverHomeView: View {
    @EnvironmentObject var sessionManager: SessionManager
    @EnvironmentObject var router: AppRouter

    @StateObject private var viewModel: DriverHomeViewModel

    init() {
        let tempSession = SessionManager()
        let apiClient = APIClient(sessionManager: tempSession)
        let repo = DriverRepository(apiClient: apiClient)

        _viewModel = StateObject(
            wrappedValue: DriverHomeViewModel(
                sessionManager: tempSession,
                driverRepository: repo
            )
        )
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                headerSection
                connectionSection
                locationSection
                Divider()
                activeRideSection
                Divider()
                availableRidesSection
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
                get: { viewModel.message != nil },
                set: { newValue in
                    if !newValue {
                        viewModel.clearMessage()
                    }
                }
            )
        ) {
            Button("Tamam", role: .cancel) {
                viewModel.clearMessage()
            }
        } message: {
            Text(viewModel.message ?? "")
        }
    }
}

// MARK: - Sections
private extension DriverHomeView {

    var headerSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Driver Home")
                .font(.largeTitle)
                .bold()

            Text("Durum: \(viewModel.isOnline ? "Online" : "Offline")")
            Text("Konum: \(viewModel.currentLatText), \(viewModel.currentLngText)")
            Text("Log: \(viewModel.lastLog)")
                .font(.footnote)
        }
    }

    var connectionSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Button("Socket Bağlan") {
                viewModel.connectSocket()
            }
            .buttonStyle(.borderedProminent)

            Button("ONLINE OL") {
                Task {
                    await viewModel.setOnline(true)
                }
            }
            .buttonStyle(.borderedProminent)

            Button("OFFLINE OL") {
                Task {
                    await viewModel.setOnline(false)
                }
            }
            .buttonStyle(.bordered)

            Button("Açık Ride'ları Getir") {
                Task {
                    await viewModel.loadAvailableRides()
                }
            }
            .buttonStyle(.borderedProminent)

            Button("Aktif Ride'ı Getir") {
                Task {
                    await viewModel.loadActiveRide()
                }
            }
            .buttonStyle(.bordered)
        }
    }

    var locationSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Button("Konum Başlat") {
                viewModel.startLocationTracking()
            }
            .buttonStyle(.borderedProminent)

            Button("Konum Durdur") {
                viewModel.stopLocationTracking()
            }
            .buttonStyle(.bordered)
        }
    }

    @ViewBuilder
    var activeRideSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Aktif Ride")
                .font(.title3)
                .bold()

            if let activeRide = viewModel.activeRide {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Ride ID: \(activeRide.id)")
                    Text("Pickup: \(activeRide.pickup_address)")
                    Text("Dropoff: \(activeRide.dropoff_address)")
                    Text("Durum: \(activeRide.status)")

                    Button("Yoldayım") {
                        Task {
                            await viewModel.updateRideStatus(
                                "DRIVER_ARRIVING",
                                note: "Şoför müşteriye doğru yola çıktı."
                            )
                        }
                    }
                    .buttonStyle(.borderedProminent)

                    Button("Geldim") {
                        Task {
                            await viewModel.updateRideStatus(
                                "DRIVER_ARRIVED",
                                note: "Şoför alım noktasına ulaştı."
                            )
                        }
                    }
                    .buttonStyle(.borderedProminent)

                    Button("Başlat") {
                        Task {
                            await viewModel.updateRideStatus(
                                "RIDE_STARTED",
                                note: "Müşteri araca bindi."
                            )
                        }
                    }
                    .buttonStyle(.borderedProminent)

                    Button("Bitir") {
                        Task {
                            await viewModel.updateRideStatus(
                                "RIDE_COMPLETED",
                                note: "Yolculuk tamamlandı."
                            )
                        }
                    }
                    .buttonStyle(.borderedProminent)
                }
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.gray.opacity(0.1))
                .cornerRadius(12)
            } else {
                Text("Aktif ride yok")
            }
        }
    }

    var availableRidesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Açık Ride Listesi")
                    .font(.title3)
                    .bold()

                if viewModel.isLoadingAvailableRides {
                    ProgressView()
                }
            }

            ForEach(viewModel.availableRides) { ride in
                availableRideCard(ride)
            }
        }
    }

    func availableRideCard(_ ride: AvailableRideItem) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Ride ID: \(ride.id)")
                .bold()
            Text("Pickup: \(ride.pickup_address)")
            Text("Dropoff: \(ride.dropoff_address)")

            Button {
                Task {
                    await viewModel.acceptRide(ride.id)
                }
            } label: {
                if viewModel.isAcceptingRide {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                } else {
                    Text("Kabul Et")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(viewModel.isAcceptingRide)
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.gray.opacity(0.1))
        .cornerRadius(12)
    }

    var logoutSection: some View {
        Button("Çıkış Yap") {
            viewModel.disconnectSocket()
            sessionManager.clear()
            router.route = .login
        }
        .buttonStyle(.bordered)
    }
}
