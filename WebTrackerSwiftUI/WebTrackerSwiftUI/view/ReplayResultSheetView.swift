//
//  ReplayResultSheetView.swift
//  WebTrackerSwiftUI
//
//  Created by Alperen Saraç on 21.03.2026.
//

import Foundation
import SwiftUI

struct ReplayResultSheetView: View {
    let text: String

    var body: some View {
        NavigationView {
            ScrollView {
                Text(text)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()
            }
            .navigationTitle("Replay Sonucu")
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
