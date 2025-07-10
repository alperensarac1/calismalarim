//
//  MesajlarScreen.swift
//  ChatSwiftUI
//
//  Created by Alperen Saraç on 9.07.2025.
//

import Foundation
import SwiftUI

import SwiftUI

struct MesajlarScreen: View {
    @StateObject private var vm = SohbetListesiViewModel()
    @State private var showYeniKisi = false
    @State private var secilenNumara: String? = nil
    @State private var gösterHata = false

    let onKisiSecildi: (Int, String) -> Void

    var body: some View {
        List(vm.konusulanKisiler) { kisi in
            KonusulanKisiCard(kisi: kisi)
                .onTapGesture {
                    onKisiSecildi(kisi.id, kisi.ad)
                }
        }
        .navigationTitle("Sohbetler")
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button {
                    showYeniKisi = true
                } label: {
                    Image(systemName: "plus")
                }
            }
        }
        .sheet(isPresented: $showYeniKisi) {
            YeniKisiSheet { numara in
                secilenNumara = numara
            }
        }
        .alert("Hata", isPresented: $gösterHata, actions: {
            Button("Tamam", role: .cancel) { }
        }, message: {
            Text(vm.hataMesaji ?? "")
        })
        .onChange(of: vm.hataMesaji) { _ in
            gösterHata = vm.hataMesaji != nil
        }
        .onChange(of: secilenNumara) { numara in
            guard let numara else { return }
            fetchKisiVeGit(numara: numara)
        }
        .task {
            vm.sohbetListesiniBaslat(kullaniciId: AppConfig.kullaniciId)
        }
    }

    private func fetchKisiVeGit(numara: String) {
        ApiService.shared.kullanicilariGetir { result in
            switch result {
            case .success(let yanit):
                if let kisi = yanit.kullanicilar.first(where: { $0.numara == numara }) {
                    onKisiSecildi(kisi.id, kisi.ad)
                } else {
                    vm.hataMesaji = "Bu numara kayıtlı değil"
                }
            case .failure(let error):
                vm.hataMesaji = "Sunucu hatası: \(error.localizedDescription)"
            }
            secilenNumara = nil
        }
    }
}


struct YeniKisiSheet: View {
    @Environment(\.dismiss) private var dismiss
    @State private var numara: String = ""

    let onConfirm: (String) -> Void

    var body: some View {
        NavigationStack {
            Form {
                Section("Alıcı numarası") {
                    TextField("5xx…", text: $numara)
                        .keyboardType(.phonePad)
                }
            }
            .navigationTitle("Yeni Mesaj")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("İptal") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Gönder") {
                        guard !numara.trimmingCharacters(in: .whitespaces).isEmpty else { return }
                        onConfirm(numara.trimmingCharacters(in: .whitespaces))
                        dismiss()
                    }
                }
            }
        }
    }
}
