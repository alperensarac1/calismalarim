//
//  ShipmentDetailView.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation
import SwiftUI

struct ShipmentDetailView: View {
    let shipment: Shipment

    var body: some View {
        Form {
            Section("Özet") {
                Text("ID: #\(shipment.id)")
                Text("Rol: \(shipment.role)")
                Text("Durum: \(shipment.status)")
                if let c = shipment.cargo_company_name, !c.isEmpty {
                    Text("Kargo: \(c)")
                }
            }

            if shouldShowCode {
                Section("Alım Kodu") {
                    Text(shipment.pickup_code).font(.system(.body, design: .monospaced))
                    Text("Son geçerlilik: \(shipment.code_expires_at)")
                        .foregroundStyle(.secondary)

                    Button("Kodu Kopyala") {
                        UIPasteboard.general.string = shipment.pickup_code
                    }
                }
            } else {
                Section {
                    Text("Bu gönderi henüz görünür değil / kod aktif değil.")
                        .foregroundStyle(.secondary)
                }
            }

            if shipment.role == "RECEIVER",
               (shipment.visible ?? true),
               let t = shipment.receiver_address_title,
               !t.isEmpty {
                Section("Adres") {
                    Text(t)
                }
            }
        }
        .navigationTitle("Gönderi Detayı")
    }

    private var shouldShowCode: Bool {
        let st = shipment.status.uppercased()
        if ["CANCELLED", "EXPIRED"].contains(st) { return false }
        if shipment.role == "RECEIVER" && (shipment.visible == false) { return false }
        return true
    }
}
