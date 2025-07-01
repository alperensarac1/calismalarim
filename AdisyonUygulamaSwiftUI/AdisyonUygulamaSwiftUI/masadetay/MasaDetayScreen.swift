//
//  MasaDetayScreen.swift
//  AdisyonUygulamaSwiftUI
//
//  Created by Alperen Saraç on 10.06.2025.
//
import SwiftUI

struct MasaDetayScreen: View {
    let masaId: Int

    @StateObject private var detayVM: MasaDetayViewModel
    @StateObject private var urunVM = UrunViewModel()
    @Environment(\.presentationMode) private var presentationMode
    @State private var showPaymentCompleteAlert = false

    // Initialize detayVM with the incoming masaId
    init(masaId: Int) {
        self.masaId = masaId
        _detayVM = StateObject(wrappedValue: MasaDetayViewModel(masaId: masaId))
    }

    var body: some View {
        Group {
            if let masaGercek = detayVM.masa {
                MasaDetayLayout(
                    masa: masaGercek,
                    detayVM: detayVM,
                    urunVM: urunVM,
                    onBackPressed: { presentationMode.wrappedValue.dismiss() }
                )
            } else {
                ProgressView("Yükleniyor...")
            }
        }
        .task {
            await detayVM.yukleTumVeriler()
            await urunVM.kategorileriYukle()
            await urunVM.urunleriYukle()
        }
        .onReceive(detayVM.$odemeTamamlandi) { tamamlandi in
            if tamamlandi {
                showPaymentCompleteAlert = true
            }
        }
        .alert("Ödeme tamamlandı", isPresented: $showPaymentCompleteAlert) {
            Button("Tamam", role: .cancel) {}
        }
    }

}

// MARK: — Preview

struct MasaDetayScreen_Previews: PreviewProvider {
    static var previews: some View {
        MasaDetayScreen(masaId: 1)
    }
}
