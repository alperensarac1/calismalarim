//
//  JoinRoomSheet.swift
//  MemeShareSwiftUI
//
//  Created by Alperen Saraç on 2.09.2025.
//

import Foundation
import SwiftUI
struct JoinRoomSheet: View {
    @State private var code: String = ""
    let onJoin: (String) -> Void
    let onCancel: () -> Void

    var body: some View {
        NavigationStack {
            Form {
                Section("Oda Katılım") {
                    TextField("Oda kodu", text: $code)
                }
            }
            .navigationTitle("Oda Kodu")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("İptal", action: onCancel)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Katıl") {
                        onJoin(code.trimmingCharacters(in: .whitespacesAndNewlines))
                    }
                    .disabled(code.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
        }
    }
}
