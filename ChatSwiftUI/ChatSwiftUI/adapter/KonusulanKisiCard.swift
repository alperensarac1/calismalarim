import Foundation
import SwiftUI

struct KonusulanKisiCard: View {
    let kisi: KonusulanKisi

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(kisi.ad)
                    .font(.title3.weight(.semibold))

                Text(kisi.son_mesaj.prefix(30) + (kisi.son_mesaj.count > 30 ? "…" : ""))
                    .font(.body)
                    .lineLimit(1)
                    .foregroundStyle(.secondary)
            }
            Spacer(minLength: 8)
            Text(kisi.tarih)
                .font(.footnote)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 8)
        .padding(.horizontal, 16)
        .contentShape(Rectangle())
    }
}
