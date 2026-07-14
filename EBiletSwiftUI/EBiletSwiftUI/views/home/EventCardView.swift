//
//  EventCardView.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import SwiftUI

/*
    EventCardView

    Ana ekranda tek etkinlik kartını gösterir.

    Android/Compose tarafındaki EventCard karşılığıdır.

    Görevleri:
    - Poster gösterme
    - Etkinlik adı
    - Tarih
    - Sahne
    - Konum
    - Fiyat
    - Kalan kontenjan
*/
struct EventCardView: View {

    let event: Event

    private var posterURL: URL? {
        guard let posterPath = event.posterUrl,
              !posterPath.isEmpty else {
            return nil
        }

        if posterPath.hasPrefix("http") {
            return URL(string: posterPath)
        } else {
            return URL(string: APIClient.baseURL + posterPath)
        }
    }

    private var venueName: String {
        event.venue?.name ?? "-"
    }

    private var cityName: String {
        event.cityName ?? event.city?.name ?? "-"
    }

    private var districtName: String {
        event.districtName ?? event.district?.name ?? "-"
    }

    private var priceText: String {
        "\(Int(event.basePrice ?? 0)) TL"
    }

    private var quotaText: String {
        "Kalan: \(event.remainingQuota ?? 0)"
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            posterView

            VStack(alignment: .leading, spacing: 8) {
                Text(event.title)
                    .font(.title3)
                    .bold()
                    .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))
                    .lineLimit(2)

                Text("Tarih: \(event.eventDate ?? "-")")
                    .font(.subheadline)
                    .foregroundStyle(Color(red: 71 / 255, green: 85 / 255, blue: 105 / 255))

                Text("Sahne: \(venueName)")
                    .font(.subheadline)
                    .foregroundStyle(Color(red: 71 / 255, green: 85 / 255, blue: 105 / 255))

                Text("\(cityName) / \(districtName)")
                    .font(.footnote)
                    .foregroundStyle(.secondary)

                HStack {
                    Text(priceText)
                        .font(.title3)
                        .bold()
                        .foregroundStyle(.green)

                    Spacer()

                    Text(quotaText)
                        .font(.subheadline)
                        .bold()
                        .foregroundStyle(.blue)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 7)
                        .background(Color.blue.opacity(0.10))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                }
                .padding(.top, 6)
            }
            .padding(14)
        }
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 4)
    }

    /*
        Poster alanı.

        AsyncImage:
        SwiftUI içinde internetten görsel yüklemek için hazır componenttir.

        Poster URL yoksa gri placeholder gösteriyoruz.
    */
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
        .frame(height: 190)
        .clipped()
    }
}
