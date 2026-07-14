//
//  TicketDetailView.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import SwiftUI

/*
    TicketDetailView

    Tek bilet detayını ve QR kodunu gösterir.

    Backend:
    tickets/ticket_detail.php

    POST:
    api_token
    ticket_id

    QR:
    Önce qr_code_text kullanılır.
    Boşsa ticket_code kullanılır.
*/
struct TicketDetailView: View {

    let ticketId: Int

    @State private var ticket: Ticket?

    @State private var isLoading: Bool = false
    @State private var statusMessage: String = "Bilet detayı yükleniyor..."

    @State private var showAlert: Bool = false
    @State private var alertMessage: String = ""

    var body: some View {
        ZStack {
            Color(red: 245 / 255, green: 246 / 255, blue: 250 / 255)
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 14) {
                    statusView

                    if isLoading && ticket == nil {
                        LoadingView(text: "Bilet detayı yükleniyor...")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 18))
                    }

                    if let ticket {
                        ticketContent(ticket)
                    }
                }
                .padding(14)
            }
        }
        .navigationTitle("Bilet Detayı")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            if ticket == nil {
                await loadTicketDetail()
            }
        }
        .alert("Uyarı", isPresented: $showAlert) {
            Button("Tamam", role: .cancel) {}
        } message: {
            Text(alertMessage)
        }
    }

    private var statusView: some View {
        HStack(spacing: 8) {
            if isLoading {
                ProgressView()
            }

            Text(statusMessage)
                .font(.subheadline)
                .foregroundStyle(.secondary)

            Spacer()
        }
        .padding(.horizontal, 4)
    }

    private func ticketContent(_ ticket: Ticket) -> some View {
        VStack(spacing: 16) {
            VStack(spacing: 12) {
                Text(ticket.event?.title ?? ticket.eventTitle ?? "Etkinlik")
                    .font(.title2)
                    .bold()
                    .multilineTextAlignment(.center)
                    .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))

                statusBadge(ticket.status ?? ticket.ticketStatus ?? "-")

                qrView(ticket)

                Text(ticket.ticketCode ?? ticket.qrCodeText ?? "-")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)

                Divider()

                detailLine(
                    title: "Tarih",
                    value: ticket.event?.eventDate ?? "-"
                )

                let venueName =
                    ticket.venue?.name ??
                    ticket.location?.venueName ??
                    ticket.event?.venue?.name ??
                    "-"

                detailLine(
                    title: "Sahne",
                    value: venueName
                )

                let cityName =
                    ticket.city?.name ??
                    ticket.location?.cityName ??
                    ticket.event?.city?.name ??
                    "-"

                let districtName =
                    ticket.district?.name ??
                    ticket.location?.districtName ??
                    ticket.event?.district?.name ??
                    "-"

                detailLine(
                    title: "Konum",
                    value: "\(cityName) / \(districtName)"
                )

                detailLine(
                    title: "Fiyat",
                    value: "\(Int(ticket.price ?? 0)) TL",
                    valueColor: .green
                )

                if let usedAt = ticket.usedAt, !usedAt.isEmpty {
                    Text("Kullanım zamanı: \(usedAt)")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                } else {
                    Text("Bilet henüz kullanılmadı.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
            .padding(18)
            .background(.white)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
        }
    }

    private func statusBadge(_ status: String) -> some View {
        let text: String
        let color: Color

        switch status {
        case "active":
            text = "Aktif Bilet"
            color = .green
        case "used":
            text = "Kullanıldı"
            color = .gray
        case "cancelled":
            text = "İptal Edildi"
            color = .red
        default:
            text = status
            color = .blue
        }

        return Text(text)
            .font(.subheadline)
            .bold()
            .foregroundStyle(color)
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            .background(color.opacity(0.12))
            .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private func qrView(_ ticket: Ticket) -> some View {
        let qrText = ticket.qrCodeText ?? ticket.ticketCode ?? ""

        return ZStack {
            Color.white

            if qrText.isEmpty {
                VStack(spacing: 8) {
                    Image(systemName: "qrcode")
                        .font(.largeTitle)
                        .foregroundStyle(.secondary)

                    Text("QR oluşturulamadı")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            } else if let image = QRCodeGenerator.shared.generate(from: qrText) {
                Image(uiImage: image)
                    .interpolation(.none)
                    .resizable()
                    .scaledToFit()
                    .padding(10)
            } else {
                Text("QR oluşturulamadı")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(width: 260, height: 260)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.06), radius: 6, x: 0, y: 3)
    }

    private func detailLine(
        title: String,
        value: String,
        valueColor: Color = Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255)
    ) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.footnote)
                .foregroundStyle(.secondary)

            Text(value)
                .font(.subheadline)
                .bold()
                .foregroundStyle(valueColor)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    // MARK: - API

    private func loadTicketDetail() async {
        guard ticketId > 0 else {
            statusMessage = "Bilet bilgisi alınamadı."
            return
        }

        isLoading = true
        statusMessage = "Bilet detayı yükleniyor..."

        do {
            let response = try await APIService.shared.getTicketDetail(
                apiToken: SessionManager.shared.apiToken,
                ticketId: ticketId
            )

            isLoading = false

            guard response.success else {
                statusMessage = response.message
                showError(response.message)
                return
            }

            guard let loadedTicket = response.data else {
                statusMessage = "Bilet bilgisi alınamadı."
                return
            }

            ticket = loadedTicket
            statusMessage = "Bilet detayı getirildi."

        } catch {
            isLoading = false
            statusMessage = error.localizedDescription
            showError(error.localizedDescription)
        }
    }

    private func showError(_ message: String) {
        alertMessage = message
        showAlert = true
    }
}
