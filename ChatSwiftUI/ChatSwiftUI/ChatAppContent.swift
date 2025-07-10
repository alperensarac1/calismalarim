import Foundation
import SwiftUI
import Alamofire

import SwiftUI

struct ChatAppContent: View {
    let pref: PrefManager
    @State private var showRegisterDialog = false
    @State private var aliciId: Int? = nil
    @State private var aliciAd: String = ""

    var body: some View {
        NavigationStack {
            MesajlarScreen { id, ad in
                aliciId = id
                aliciAd = ad
            }
            .navigationDestination(isPresented: Binding(
                get: { aliciId != nil },
                set: { newValue in if !newValue { aliciId = nil } }
            )) {
                if let id = aliciId {
                    SingleChatScreen(aliciId: id, aliciAd: aliciAd)
                }
            }
        }
        .onAppear {
            showRegisterDialog = !pref.kullaniciVarMi()
        }
        .sheet(isPresented: $showRegisterDialog) {
            RegistrationSheet(
                onKayitBasarili: { id in
                    AppConfig.kullaniciId = id
                    pref.kaydetKullaniciId(id: id)
                    showRegisterDialog = false
                },
                onCancel: {}
            )
        }
    }
}






struct RegistrationSheet: View {
    @Environment(\.dismiss) private var dismiss
    
    @State private var ad     = ""
    @State private var numara = ""
    @State private var isLoading = false
    @State private var hata: String? = nil
    
    let onKayitBasarili: (Int) -> Void      // geri bildirim
    let onCancel: () -> Void
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Bilgiler") {
                    TextField("Ad", text: $ad)
                    TextField("Numara", text: $numara)
                        .keyboardType(.phonePad)
                }
                
                if isLoading {
                    HStack { Spacer(); ProgressView(); Spacer() }
                }
                
                if let h = hata {
                    Text(h)
                        .foregroundColor(.red)
                }
            }
            .navigationTitle("Kayıt Ol")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("İptal") {
                        onCancel()
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Kayıt Ol") { kayitOl() }
                        .disabled(ad.isEmpty || numara.isEmpty)
                }
            }
        }
    }
    
    private func kayitOl() {
        guard !ad.isEmpty, !numara.isEmpty else {
            hata = "Boş alan bırakma!"
            return
        }
        isLoading = true
        ApiService.shared.kullaniciKayit(ad: ad, numara: numara) { result in
            isLoading = false
            switch result {
            case .success(let resp):
                if resp.success, let id = resp.id {
                    onKayitBasarili(id)
                    dismiss()
                } else {
                    hata = resp.error ?? "Kayıt başarısız"
                }
            case .failure(let err):
                hata = "Hata: \(err.localizedDescription)"
            }
        }
    }
}
