//
//  MasaCard.swift
//  AdisyonUygulamaSwiftUI
//
//  Created by Alperen Saraç on 9.06.2025.
//

import Foundation
import SwiftUI
struct MasaCard: View {
    let masa: Masa
    let onClick: (Masa) -> Void

    var body: some View {
        Button(action: {
            onClick(masa)
        }) {
            VStack(spacing: 8) {
                Text("Masa \(masa.id)")
                    .font(.headline)
                    .foregroundColor(.primary)

                Text(masa.toplamFiyat.fiyatYaz())
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            .padding()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(masa.acikMi == 1 ? Color.green.opacity(0.2) : Color.gray.opacity(0.2))
            .cornerRadius(10)
            .shadow(radius: 2)
        }
        .buttonStyle(PlainButtonStyle()) // default mavi efekt kalkar
    }
}

