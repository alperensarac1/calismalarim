//
//  DetailSheetView.swift
//  WebTrackerSwiftUI
//
//  Created by Alperen Saraç on 21.03.2026.
//

import Foundation
import SwiftUI

struct DetailSheetView: View {
    let log: NetworkLog
    let text: String

    var body: some View {
        NavigationView {
            ScrollView {
                Text(text)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()
            }
            .navigationTitle("İstek Detayı")
            .toolbar {
                ToolbarItem {
                    Button("Kopyala") {
                        UIPasteboard.general.string = text
                    }
                }
            }
        }
    }
}
