//
//  TicketCardView.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation

import SwiftUI

/*
    TicketCardView

    Biletlerim ekranındaki tek bilet kartıdır.

    Gösterilen bilgiler:
    - Etkinlik adı
    - Tarih
    - Sahne
    - Konum
    - Fiyat
    - Bilet durumu
*/
struct TicketCardView: View {

    let ticket: Ticket

    private var eventTitle: String {
        ticket.event?.title ?? ticket.eventTitle ?? "Etkinlik bilgisi yok"
    }

    private var eventDate: String {
        ticket.event?.eventDate ?? "-"
    }

    private var venueName: String {
        ticket.location?.venueName ??
        ticket.venue?.name ??
        ticket.event?.venue?.name ??
        "-"
    }

    private var cityName: String {
        ticket.location?.cityName ??
        ticket.city?.name ??
        ticket.event?.city?.name ??
        "-"
    }

    private var districtName: String {
        ticket.location?.districtName ??
        ticket.district?.name ??
        ticket.event?.district?.name ??
        "-"
    }

    private var priceText: String {
        "\(Int(ticket.price ?? 0)) TL"
    }

    private var statusValue: String {
        ticket.status ?? ticket.ticketStatus ?? "-"
    }

    private var statusText: String {
        switch statusValue {
        case "active":
            return "Aktif"
        case "used":
            return "Kullanıldı"
        case "cancelled":
            return "İptal"
        default:
            return statusValue
        }
    }

    private var statusColor: Color {
        switch statusValue {
        case "active":
            return .green
        case "used":
            return .gray
        case "cancelled":
            return .red
        default:
            return .blue
        }
    }

    private var posterURL: URL? {
        guard let path = ticket.event?.posterUrl,
              !path.isEmpty else {
            return nil
        }

        if path.hasPrefix("http") {
            return URL(string: path)
        } else {
            return URL(string: APIClient.baseURL + path)
        }
    }

    var body: some View {
        HStack(spacing: 12) {
            posterView

            VStack(alignment: .leading, spacing: 6) {
                Text(eventTitle)
                    .font(.headline)
                    .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))
                    .lineLimit(2)

                Text("Tarih: \(eventDate)")
                    .font(.footnote)
                    .foregroundStyle(.secondary)

                Text("Sahne: \(venueName)")
                    .font(.footnote)
                    .foregroundStyle(.secondary)

                Text("\(cityName) / \(districtName)")
                    .font(.caption)
                    .foregroundStyle(.secondary)

                HStack {
                    Text(priceText)
                        .font(.headline)
                        .bold()
                        .foregroundStyle(.green)

                    Spacer()

                    Text(statusText)
                        .font(.caption)
                        .bold()
                        .foregroundStyle(statusColor)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(statusColor.opacity(0.12))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                }
                .padding(.top, 4)
            }
        }
        .padding(12)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
    }

    private var posterView: some View {
        ZStack {
            Color(red: 226 / 255, green: 232 / 255, blue: 240 / 255)

            if let posterURL {
                AsyncImage(url: posterURL) { phase in
                    switch phase {
                    case .empty:
                        ProgressView()

                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()

                    case .failure:
                        Image(systemName: "ticket")
                            .font(.title2)
                            .foregroundStyle(.secondary)

                    @unknown default:
                        EmptyView()
                    }
                }
            } else {
                Image(systemName: "ticket")
                    .font(.title2)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(width: 95, height: 120)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
