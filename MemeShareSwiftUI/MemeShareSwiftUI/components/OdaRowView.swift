//
//  OdaRowView.swift
//  MemeShareSwiftUI
//
//  Created by Alperen Saraç on 2.09.2025.
//

import SwiftUI

struct OdaRowView: View {
    let oda: OdaModel

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            // Küçük avatar/ikon
            ZStack {
                Circle()
                    .fill(Color.blue.opacity(0.12))
                    .frame(width: 38, height: 38)
                Text(String(oda.roomCode.prefix(1)))
                    .font(.headline)
                    .foregroundStyle(.blue)
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(oda.roomCode)
                    .font(.headline)
                    .foregroundStyle(.primary)

                Text("#\(oda.odaId) • createdBy: \(oda.createdBy)")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.footnote)
                .foregroundStyle(.tertiary)
        }
        .padding(.vertical, 8)
        .contentShape(Rectangle()) // satırın tamamı tıklanabilir olsun
    }
}
