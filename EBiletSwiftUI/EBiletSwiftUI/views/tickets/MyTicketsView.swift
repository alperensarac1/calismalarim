//
//  MyTicketsView.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import SwiftUI

/*
    MyTicketsView

    Kullanıcının satın aldığı biletleri listeler.

    Backend:
    tickets/my_tickets.php

    POST:
    api_token
*/
struct MyTicketsView: View {

    @State private var tickets: [Ticket] = []

    @State private var isLoading: Bool = false
    @State private var statusMessage: String = "Biletler yükleniyor..."

    @State private var showAlert: Bool = false
    @State private var alertMessage: String = ""

    var body: some View {
        ZStack {
            Color(red: 245 / 255, green: 246 / 255, blue: 250 / 255)
                .ignoresSafeArea()

            ScrollView {
                LazyVStack(spacing: 14) {
                    headerView

                    statusView

                    if isLoading && tickets.isEmpty {
                        LoadingView(text: "Biletler yükleniyor...")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 18))
                    }

                    if tickets.isEmpty && !isLoading {
                        emptyView
                    }

                    ForEach(tickets, id: \.resolvedTicketId) { ticket in
                        NavigationLink {
                            TicketDetailView(ticketId: ticket.resolvedTicketId)
                        } label: {
                            TicketCardView(ticket: ticket)
                                .foregroundStyle(.primary)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(14)
            }
        }
        .navigationTitle("Biletlerim")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await loadMyTickets()
        }
        .refreshable {
            await loadMyTickets()
        }
        .alert("Uyarı", isPresented: $showAlert) {
            Button("Tamam", role: .cancel) {}
        } message: {
            Text(alertMessage)
        }
    }

    private var headerView: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Biletlerim")
                .font(.title)
                .bold()
                .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))

            Text("Satın aldığın biletleri ve QR kodlarını buradan görüntüleyebilirsin.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
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

    private var emptyView: some View {
        VStack(spacing: 12) {
            Image(systemName: "ticket")
                .font(.largeTitle)
                .foregroundStyle(.secondary)

            Text("Henüz biletin yok")
                .font(.headline)

            Text("Bir etkinlik seçip bilet satın aldığında burada görünecek.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(24)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
    }

    // MARK: - API

    private func loadMyTickets() async {
        isLoading = true
        statusMessage = "Biletler yükleniyor..."

        do {
            let response = try await APIService.shared.getMyTickets(
                apiToken: SessionManager.shared.apiToken
            )

            isLoading = false

            guard response.success else {
                statusMessage = response.message
                showError(response.message)
                return
            }

            tickets = response.data ?? []

            if tickets.isEmpty {
                statusMessage = "Henüz satın alınmış biletin yok."
            } else {
                statusMessage = "\(tickets.count) bilet listelendi."
            }

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
