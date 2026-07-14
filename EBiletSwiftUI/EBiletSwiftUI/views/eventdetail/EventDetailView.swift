//
//  EventDetailView.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation

import SwiftUI

/*
    EventDetailView

    Etkinlik detay ekranıdır.

    HomeView'dan sadece eventId gelir.
    Detay ekranı açıldığında backend'den güncel etkinlik bilgisi alınır.

    Kullanılan API'ler:

    1. events/event_detail.php
       Etkinlik detayını getirir.

    2. tickets/ticket_buy.php
       Kullanıcı için bilet oluşturur.
*/
struct EventDetailView: View {

    let eventId: Int

    @State private var event: Event?

    @State private var isLoading: Bool = false
    @State private var isBuying: Bool = false

    @State private var statusMessage: String = "Etkinlik detayı yükleniyor..."

    @State private var showAlert: Bool = false
    @State private var alertTitle: String = "Uyarı"
    @State private var alertMessage: String = ""

    /*
        Bilet alındıktan sonra Biletlerim ekranına gitmek için.
        MyTicketsView sonraki adımda gerçek yapılacak.
        Şimdilik placeholder'a yönlendireceğiz.
    */
    @State private var goMyTickets: Bool = false

    var body: some View {
        ZStack {
            Color(red: 245 / 255, green: 246 / 255, blue: 250 / 255)
                .ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    statusView

                    if isLoading && event == nil {
                        LoadingView(text: "Etkinlik detayı yükleniyor...")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 18))
                    }

                    if let event {
                        eventContent(event)
                    }
                }
                .padding(14)
            }
        }
        .navigationTitle("Etkinlik Detayı")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            if event == nil {
                await loadEventDetail()
            }
        }
        .navigationDestination(isPresented: $goMyTickets) {
            MyTicketsView()
        }
        .alert(alertTitle, isPresented: $showAlert) {
            Button("Tamam", role: .cancel) {}
        } message: {
            Text(alertMessage)
        }
    }

    // MARK: - Status

    private var statusView: some View {
        HStack(spacing: 8) {
            if isLoading || isBuying {
                ProgressView()
            }

            Text(statusMessage)
                .font(.subheadline)
                .foregroundStyle(.secondary)

            Spacer()
        }
        .padding(.horizontal, 4)
    }

    // MARK: - Content

    private func eventContent(_ event: Event) -> some View {
        VStack(alignment: .leading, spacing: 14) {
            posterView(event)

            VStack(alignment: .leading, spacing: 14) {
                Text(event.title)
                    .font(.title)
                    .bold()
                    .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))

                Text(event.description ?? "Açıklama bulunmuyor.")
                    .font(.body)
                    .foregroundStyle(Color(red: 71 / 255, green: 85 / 255, blue: 105 / 255))

                Divider()

                detailLine(
                    title: "Tarih",
                    value: event.eventDate ?? "-"
                )

                let cityName = event.city?.name ?? event.cityName ?? "-"
                let districtName = event.district?.name ?? event.districtName ?? "-"

                detailLine(
                    title: "Konum",
                    value: "\(cityName) / \(districtName)"
                )

                detailLine(
                    title: "Sahne",
                    value: event.venue?.name ?? "-"
                )

                detailLine(
                    title: "Adres",
                    value: event.venue?.address ?? "-"
                )

                Divider()

                priceQuotaRow(event)

                AppButton(
                    title: buyButtonTitle(event),
                    backgroundColor: buyButtonColor(event),
                    isLoading: isBuying
                ) {
                    Task {
                        await buyTicket()
                    }
                }
                .disabled(!canBuy(event))
                .opacity(canBuy(event) ? 1 : 0.65)
                .padding(.top, 6)
            }
            .padding(16)
            .background(.white)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
        }
    }

    private func posterView(_ event: Event) -> some View {
        ZStack {
            Color(red: 226 / 255, green: 232 / 255, blue: 240 / 255)

            if let url = posterURL(event.posterUrl) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        ProgressView()

                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()

                    case .failure:
                        Image(systemName: "photo")
                            .font(.largeTitle)
                            .foregroundStyle(.secondary)

                    @unknown default:
                        EmptyView()
                    }
                }
            } else {
                Image(systemName: "photo")
                    .font(.largeTitle)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(height: 250)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
    }

    private func detailLine(
        title: String,
        value: String
    ) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.footnote)
                .foregroundStyle(.secondary)

            Text(value)
                .font(.subheadline)
                .bold()
                .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func priceQuotaRow(_ event: Event) -> some View {
        HStack {
            Text("\(Int(event.basePrice ?? 0)) TL")
                .font(.title2)
                .bold()
                .foregroundStyle(.green)

            Spacer()

            Text("Kalan: \(event.remainingQuota ?? 0)")
                .font(.subheadline)
                .bold()
                .foregroundStyle(.blue)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(Color.blue.opacity(0.10))
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Helpers

    private func posterURL(_ path: String?) -> URL? {
        guard let path, !path.isEmpty else {
            return nil
        }

        if path.hasPrefix("http") {
            return URL(string: path)
        } else {
            return URL(string: APIClient.baseURL + path)
        }
    }

    private func canBuy(_ event: Event) -> Bool {
        let remainingQuota = event.remainingQuota ?? 0
        return remainingQuota > 0 && !isBuying && !isLoading
    }

    private func buyButtonTitle(_ event: Event) -> String {
        if isBuying {
            return "Bilet Oluşturuluyor..."
        }

        let remainingQuota = event.remainingQuota ?? 0

        if remainingQuota <= 0 {
            return "Kontenjan Doldu"
        }

        return "Bilet Al"
    }

    private func buyButtonColor(_ event: Event) -> Color {
        let remainingQuota = event.remainingQuota ?? 0

        if remainingQuota <= 0 {
            return .gray
        }

        return .green
    }

    // MARK: - API

    private func loadEventDetail() async {
        guard eventId > 0 else {
            statusMessage = "Etkinlik bilgisi alınamadı."
            return
        }

        isLoading = true
        statusMessage = "Etkinlik detayı yükleniyor..."

        do {
            let response = try await APIService.shared.getEventDetail(
                apiToken: SessionManager.shared.apiToken,
                eventId: eventId
            )

            isLoading = false

            guard response.success else {
                statusMessage = response.message
                return
            }

            guard let loadedEvent = response.data else {
                statusMessage = "Etkinlik bilgisi alınamadı."
                return
            }

            event = loadedEvent
            statusMessage = "Etkinlik detayı getirildi."

        } catch {
            isLoading = false
            statusMessage = error.localizedDescription
        }
    }

    private func buyTicket() async {
        guard let event else {
            showMessage(
                title: "Uyarı",
                message: "Etkinlik bilgisi bulunamadı."
            )
            return
        }

        let remainingQuota = event.remainingQuota ?? 0

        guard remainingQuota > 0 else {
            showMessage(
                title: "Uyarı",
                message: "Bu etkinlik için kontenjan kalmamış."
            )
            return
        }

        isBuying = true
        statusMessage = "Bilet oluşturuluyor..."

        do {
            let response = try await APIService.shared.buyTicket(
                apiToken: SessionManager.shared.apiToken,
                eventId: event.id
            )

            isBuying = false

            guard response.success else {
                statusMessage = response.message
                showMessage(
                    title: "Uyarı",
                    message: response.message
                )
                return
            }

            let ticketCode = response.data?.ticketCode ?? "-"

            statusMessage = "Bilet başarıyla oluşturuldu."

            showMessage(
                title: "Bilet Alındı",
                message: "Bilet kodu: \(ticketCode)"
            )

            /*
                Alert kapandıktan sonra yönlendirme kontrolünü sonraki adımda
                daha profesyonel yapabiliriz. Şimdilik kısa gecikme ile
                Biletlerim placeholder ekranına geçiriyoruz.
            */
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.7) {
                goMyTickets = true
            }

        } catch {
            isBuying = false
            statusMessage = error.localizedDescription

            showMessage(
                title: "Bağlantı Hatası",
                message: error.localizedDescription
            )
        }
    }

    private func showMessage(
        title: String,
        message: String
    ) {
        alertTitle = title
        alertMessage = message
        showAlert = true
    }
}
