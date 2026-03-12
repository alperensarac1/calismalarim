//
//  HelperHistoryView.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation
import SwiftUI

struct HelperHistoryView: View {
    @ObservedObject var vm: HelperVM
    let helperId: Int

    var body: some View {
        NavigationStack {
            List(vm.history) { it in
                VStack(alignment: .leading, spacing: 6) {
                    Text(it.patient_name ?? "-").bold()
                    Text("Telefon: \(it.patient_phone ?? "-")")
                    Text("Servis: \(it.servis_adi ?? "-")")
                    Text("Oda: \(it.oda_no ?? "-")")
                    Text("Onay: \(it.confirmed_at ?? "-")")
                }
                .padding(.vertical, 6)
            }
            .navigationTitle("Geçmiş")
            .onAppear {
                Task { await vm.fetchHistory(helperId: helperId) }
            }
        }
    }
}
