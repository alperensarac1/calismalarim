//
//  HelperOpenView.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation
import SwiftUI

struct HelperOpenView: View {
    @ObservedObject var vm: HelperVM
    let helperId: Int

    var body: some View {
        NavigationStack {
            VStack {
                if !vm.info.isEmpty { Text(vm.info).foregroundColor(.red) }

                List(vm.openItems) { it in
                    VStack(alignment: .leading, spacing: 6) {
                        Text(it.patient_name ?? "-").bold()
                        Text("Yaş: \(it.patient_age ?? 0)")
                        Text("İstek: \(it.created_at ?? "-")")

                        Button("Kabul Et") {
                            Task {
                                let ok = await vm.accept(requestId: it.id, helperId: helperId)
                                if ok { /* kabul tabına geçmek için kullanıcı tabdan bakar */ }
                            }
                        }
                        .buttonStyle(.borderedProminent)
                    }
                    .padding(.vertical, 6)
                }
            }
            .navigationTitle("İlçemde Açık İstekler")
            .onAppear { vm.startOpenPolling(helperId: helperId) }
            .onDisappear { vm.stopOpenPolling() }
        }
    }
}
