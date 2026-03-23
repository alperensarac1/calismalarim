//
//  ActionSheetView.swift
//  WebTrackerSwiftUI
//
//  Created by Alperen Saraç on 21.03.2026.
//

import Foundation
import SwiftUI

struct ActionSheetView: View {
    let log: NetworkLog
    let onDetail: () -> Void
    let onReplay: () -> Void
    let onCopy: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Text("İstek İşlemleri")
                .font(.headline)

            Text(log.url)
                .font(.footnote)

            Button("Detay", action: onDetail)
            Button("Replay", action: onReplay)
            Button("Kopyala", action: onCopy)
        }
        .padding()
    }
}
