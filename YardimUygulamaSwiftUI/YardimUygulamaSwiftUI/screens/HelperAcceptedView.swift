//
//  HelperAcceptedView.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation
import SwiftUI

struct HelperAcceptedView: View {
    @ObservedObject var vm: HelperVM
    let helperId: Int

    var body: some View {
        VStack(spacing: 12) {
            if let a = vm.accepted {
                Text("Hasta: \(a.patient_name ?? "-") (\(a.patient_age ?? 0))").bold()
                Text("Telefon: \(a.patient_phone ?? "-")")
                Text("Servis: \(a.servis_adi ?? "-")")
                Text("Oda: \(a.oda_no ?? "-")")
                Text("Kalan: \(TimeUtils.formatRemainingSeconds(a.remaining_seconds ?? 0))")

                Button("Ara") {
                    guard let p = a.patient_phone, let url = URL(string: "tel:\(p)") else { return }
                    UIApplication.shared.open(url)
                }
                .buttonStyle(.borderedProminent)
                .disabled((a.patient_phone ?? "").isEmpty)

                Button("Haritada Aç") {
                    MapUtils.openInAppleMaps(lat: a.lat, lng: a.lng, label: "Hasta Konumu")
                }
                .buttonStyle(.bordered)

                Text("Not: 5 dk içinde arayıp hasta onaylatmalı.").font(.footnote)
            } else {
                Text("Aktif kabul yok.")
            }

            Spacer()
        }
        .padding()
        .onAppear { vm.startAcceptedPolling(helperId: helperId) }
        .onDisappear { vm.stopAcceptedPolling() }
    }
}
