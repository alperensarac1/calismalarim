//
//  RehberDetaysayfa.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//

import SwiftUI

struct RehberDetaysayfa: View {
    @ObservedObject var viewModel: RehberViewModel
    @Environment(\.dismiss) var dismiss
    @State var kisi: Kisi = Kisi()
    @State var kisiAd:String = ""
    @State var kisiTel:String = ""

    var body: some View {
        VStack {
            TextField("Adı", text: $kisiAd)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding()

            TextField("Telefon", text: $kisiTel)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .keyboardType(.phonePad)
                .padding()
            
            HStack {
                Button("Kaydet") {
                    kisi.kisi_ad = kisiAd
                    kisi.kisi_tel = kisiTel
                    viewModel.kisiEkleGuncelle(kisi: kisi)
                    dismiss()
                }
                .buttonStyle(.borderedProminent)

                if viewModel.kisiler.contains(where: { $0.id == kisi.id }) {
                    Button("Sil") {
                        viewModel.kisiSil(kisi: kisi)
                        dismiss()
                    }
                    .buttonStyle(.bordered)
                    .foregroundColor(.red)
                }
            }
            .padding()
        }
        .padding()
    }
}
