//
//  HomeView.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import SwiftUI

/*
    HomeView

    SwiftUI ana ekranıdır.

    Görevleri:
    - Kullanıcıyı karşılamak
    - Şehirleri backend'den çekmek
    - Seçilen şehre göre ilçeleri çekmek
    - Şehir + ilçe seçimine göre etkinlikleri listelemek
    - Etkinlik kartına tıklayınca detay ekranına geçmek

    SwiftUI karşılıkları:
    - Spinner yerine Picker
    - RecyclerView/TableView yerine ScrollView + LazyVStack
    - Glide/Coil yerine AsyncImage
*/
struct HomeView: View {

    @EnvironmentObject private var appState: AppState

    // MARK: - API Data

    @State private var cities: [City] = []
    @State private var districts: [District] = []
    @State private var events: [Event] = []

    // MARK: - Selected Values

    @State private var selectedCityId: Int = 0
    @State private var selectedDistrictId: Int = 0

    // MARK: - UI State

    @State private var statusMessage: String = "Şehirler yükleniyor..."
    @State private var isLoadingCities: Bool = false
    @State private var isLoadingDistricts: Bool = false
    @State private var isLoadingEvents: Bool = false

    @State private var showAlert: Bool = false
    @State private var alertMessage: String = ""

    /*
        Navigation path ileride detay/bilet ekranlarında da genişletilebilir.
        Şimdilik NavigationLink ile EventDetailPlaceholderView açacağız.
    */

    var body: some View {
        NavigationStack {
            ZStack {
                Color(red: 245 / 255, green: 246 / 255, blue: 250 / 255)
                    .ignoresSafeArea()

                ScrollView {
                    LazyVStack(spacing: 14) {
                        headerCard

                        filterCard

                        statusView

                        ForEach(events, id: \.id) { event in
                            NavigationLink {
                                EventDetailView(eventId: event.id)
                            } label: {
                                EventCardView(event: event)
                                    .foregroundStyle(.primary)
                            }
                        }
                    }
                    .padding(14)
                }
            }
            .navigationTitle("Etkinlikler")
            .navigationBarTitleDisplayMode(.inline)
            .task {
                /*
                    Ekran ilk açıldığında şehirleri yükle.
                */
                if cities.isEmpty {
                    await loadCities()
                }
            }
            .alert("Uyarı", isPresented: $showAlert) {
                Button("Tamam", role: .cancel) {}
            } message: {
                Text(alertMessage)
            }
        }
    }

    // MARK: - Header

    private var headerCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Hoş geldin, \(SessionManager.shared.fullName)")
                .font(.title2)
                .bold()
                .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))

            Text(roleText)
                .font(.subheadline)
                .foregroundStyle(.secondary)

            HStack(spacing: 8) {
                NavigationLink {
                    MyTicketsView()
                } label: {
                    smallHeaderButtonText("Biletlerim")
                }
                .buttonStyle(.plain)

                if SessionManager.shared.isStaffOrAdmin {
                    NavigationLink {
                        TicketScannerView()
                    } label: {
                        smallHeaderButtonText("QR Kontrol")
                    }
                    .buttonStyle(.plain)
                }

                Button {
                    appState.logout()
                } label: {
                    smallHeaderButtonText("Çıkış", color: .red)
                }
                .buttonStyle(.plain)
            }
            .padding(.top, 6)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
    }

    private var roleText: String {
        let role = SessionManager.shared.role

        if role == "admin" {
            return "Admin hesabı"
        } else if role == "staff" {
            return "Görevli hesabı"
        } else {
            return "Etkinlikleri keşfet"
        }
    }

    private func smallHeaderButtonText(
        _ text: String,
        color: Color = .blue
    ) -> some View {
        Text(text)
            .font(.subheadline)
            .bold()
            .frame(maxWidth: .infinity)
            .frame(height: 42)
            .background(color)
            .foregroundStyle(.white)
            .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    // MARK: - Filter

    private var filterCard: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Konum Seç")
                .font(.title3)
                .bold()
                .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))

            Text("Önce şehir, sonra ilçe seçerek etkinlikleri listeleyebilirsin.")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            cityPicker

            districtPicker

            AppButton(
                title: isLoadingEvents ? "Etkinlikler Yükleniyor..." : "Etkinlikleri Listele",
                backgroundColor: .green,
                isLoading: isLoadingEvents
            ) {
                Task {
                    await listEventsTapped()
                }
            }
        }
        .padding(16)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
    }

    private var cityPicker: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Şehir")
                .font(.footnote)
                .foregroundStyle(.secondary)

            Picker("Şehir", selection: $selectedCityId) {
                Text(isLoadingCities ? "Şehirler yükleniyor..." : "Şehir seçiniz")
                    .tag(0)

                ForEach(cities, id: \.id) { city in
                    Text(city.name)
                        .tag(city.id)
                }
            }
            .pickerStyle(.menu)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 12)
            .frame(height: 52)
            .background(Color(red: 238 / 255, green: 242 / 255, blue: 255 / 255))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .onChange(of: selectedCityId) { newValue in
                Task {
                    await cityChanged(cityId: newValue)
                }
            }
        }
    }

    private var districtPicker: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("İlçe")
                .font(.footnote)
                .foregroundStyle(.secondary)

            Picker("İlçe", selection: $selectedDistrictId) {
                Text(districtPickerPlaceholder)
                    .tag(0)

                ForEach(districts, id: \.id) { district in
                    Text(district.name)
                        .tag(district.id)
                }
            }
            .pickerStyle(.menu)
            .disabled(selectedCityId == 0 || isLoadingDistricts)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 12)
            .frame(height: 52)
            .background(Color(red: 238 / 255, green: 242 / 255, blue: 255 / 255))
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
    }

    private var districtPickerPlaceholder: String {
        if selectedCityId == 0 {
            return "Önce şehir seçiniz"
        }

        if isLoadingDistricts {
            return "İlçeler yükleniyor..."
        }

        return "İlçe seçiniz"
    }

    // MARK: - Status

    private var statusView: some View {
        HStack {
            if isLoadingCities || isLoadingDistricts || isLoadingEvents {
                ProgressView()
            }

            Text(statusMessage)
                .font(.subheadline)
                .foregroundStyle(.secondary)

            Spacer()
        }
        .padding(.horizontal, 4)
    }

    // MARK: - Actions

    private func cityChanged(cityId: Int) async {
        /*
            Placeholder seçildiyse sıfırla.
        */
        guard cityId > 0 else {
            districts = []
            events = []
            selectedDistrictId = 0
            statusMessage = "Şehir seçiniz."
            return
        }

        selectedDistrictId = 0
        districts = []
        events = []

        await loadDistricts(cityId: cityId)
    }

    private func listEventsTapped() async {
        guard selectedCityId > 0 else {
            showError("Lütfen şehir seçiniz")
            return
        }

        guard selectedDistrictId > 0 else {
            showError("Lütfen ilçe seçiniz")
            return
        }

        await loadEvents(
            cityId: selectedCityId,
            districtId: selectedDistrictId
        )
    }

    // MARK: - API

    private func loadCities() async {
        isLoadingCities = true
        statusMessage = "Şehirler yükleniyor..."

        do {
            let response = try await APIService.shared.getCities(
                apiToken: SessionManager.shared.apiToken
            )

            isLoadingCities = false

            guard response.success else {
                statusMessage = response.message
                return
            }

            cities = response.data ?? []

            if cities.isEmpty {
                statusMessage = "Aktif şehir bulunamadı."
            } else {
                statusMessage = "Şehir seçiniz."
            }

        } catch {
            isLoadingCities = false
            statusMessage = error.localizedDescription
        }
    }

    private func loadDistricts(cityId: Int) async {
        isLoadingDistricts = true
        statusMessage = "İlçeler yükleniyor..."

        do {
            let response = try await APIService.shared.getDistrictsByCity(
                apiToken: SessionManager.shared.apiToken,
                cityId: cityId
            )

            isLoadingDistricts = false

            guard response.success else {
                statusMessage = response.message
                return
            }

            districts = response.data ?? []

            if districts.isEmpty {
                statusMessage = "Bu şehir için aktif ilçe bulunamadı."
            } else {
                statusMessage = "İlçe seçip etkinlikleri listeleyebilirsin."
            }

        } catch {
            isLoadingDistricts = false
            statusMessage = error.localizedDescription
        }
    }

    private func loadEvents(
        cityId: Int,
        districtId: Int
    ) async {
        isLoadingEvents = true
        statusMessage = "Etkinlikler yükleniyor..."

        do {
            let response = try await APIService.shared.getEventsByLocation(
                apiToken: SessionManager.shared.apiToken,
                cityId: cityId,
                districtId: districtId
            )

            isLoadingEvents = false

            guard response.success else {
                statusMessage = response.message
                return
            }

            events = response.data ?? []

            if events.isEmpty {
                statusMessage = "Bu konum için etkinlik bulunamadı."
            } else {
                statusMessage = "\(events.count) etkinlik listelendi."
            }

        } catch {
            isLoadingEvents = false
            statusMessage = error.localizedDescription
        }
    }

    private func showError(_ message: String) {
        alertMessage = message
        showAlert = true
    }
}
