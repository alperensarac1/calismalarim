//
//  MainVC.swift
//  KahveQRKodOlusturucuSwiftUI
//
//  Created by Alperen Saraç on 12.04.2025.
//

import SwiftUI

struct MainVC: View {
    @StateObject private var viewModel = MainVM()
    @State private var selectedTab = 0
    @State private var showInputDialog = false
    @State private var inputPhone = ""

    var body: some View {
        VStack {
            HStack(spacing: 8) {
                Text("Kullanımlar: \(viewModel.kahveSayisi)/5").foregroundColor(Color("textColor")).fontWeight(.bold)
                Text("Hediye Kahve: \(viewModel.hediyeKahve)").foregroundColor(Color("textColor")).fontWeight(.bold)
            }
            .padding()

            // Segment Picker
            Picker("Ekran", selection: $selectedTab) {
                Text("QR Kod").tag(0).foregroundColor(textColor).background(.white)
                Text("Ürünlerimiz").tag(1).foregroundColor(textColor).background(.white)
            }
            .pickerStyle(.segmented)
            .padding()

            // İçerik
            if selectedTab == 0 {
                QRKodVC()
            } else {
                UrunlerimizVC()
            }
        }//:VStack
        .onAppear {
            if !viewModel.checkTelefonNumarasi() {
                showInputDialog = true
            }
        }
        .alert("Bilgi Girişi", isPresented: $showInputDialog) {
            TextField("Telefon numarası", text: $inputPhone)
                .keyboardType(.numberPad)
            Button("Tamam") {
                if !inputPhone.isEmpty {
                    viewModel.numarayiKaydet(numara: inputPhone)
                }
            }
            Button("İptal", role: .cancel) {
                exit(0)
            }
        } message: {
            Text("Lütfen telefon numaranızı giriniz")
        }.background(bgColor)
    }
}
