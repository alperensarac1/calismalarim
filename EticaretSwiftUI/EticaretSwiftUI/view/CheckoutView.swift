//
//  CheckoutView.swift
//  EticaretSwiftUI
//
//  Created by Alperen Saraç on 18.01.2026.
//

import Foundation
import SwiftUI

struct CheckoutView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm = CheckoutVM()

    let totalText: String

    var body: some View {
        Form {
            Section("Teslimat Bilgileri") {
                TextField("Ad Soyad", text: $vm.name)
                TextField("Telefon", text: $vm.phone)
                    .keyboardType(.phonePad)
                TextField("Şehir", text: $vm.city)
                TextEditor(text: $vm.address)
                    .frame(minHeight: 90)
            }

            Section("Özet") {
                HStack {
                    Text("Toplam")
                    Spacer()
                    Text(totalText).bold()
                }
            }

            Section {
                Button(vm.isLoading ? "Gönderiliyor..." : "Siparişi Onayla") {
                    Task { await vm.submit() }
                }
                .disabled(vm.isLoading)
            }

            if let err = vm.errorMessage {
                Section { Text(err).foregroundStyle(.red) }
            }
        }
        .navigationTitle("Ödeme")
        .alert("Başarılı", isPresented: .constant(vm.successMessage != nil)) {
            Button("Tamam") {
                vm.successMessage = nil
                dismiss()
            }
        } message: {
            Text(vm.successMessage ?? "")
        }
    }
}
