//
//  AddressCreateView.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation
import SwiftUI

struct AddressCreateView: View {
    let api: APIClient
    let onCreated: () -> Void

    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm: AddressCreateVM

    init(api: APIClient, onCreated: @escaping () -> Void) {
        self.api = api
        self.onCreated = onCreated
        _vm = StateObject(wrappedValue: AddressCreateVM(api: api))
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Adres") {
                    TextField("Adres başlığı (Ev/İş)", text: $vm.title)
                    TextField("Şehir", text: $vm.city)
                    TextField("İlçe", text: $vm.district)
                    TextField("Mahalle (opsiyonel)", text: $vm.neighborhood)

                    TextEditor(text: $vm.addressLine)
                        .frame(minHeight: 90)

                    TextField("Posta kodu (opsiyonel)", text: $vm.postal)
                        .keyboardType(.numberPad)
                }

                if let err = vm.errorText {
                    Section { Text(err).foregroundStyle(.red) }
                }

                Section {
                    Button("Kaydet") {
                        Task {
                            if await vm.save() {
                                onCreated()
                                dismiss()
                            }
                        }
                    }
                    .disabled(vm.isLoading)
                }
            }
            .overlay { if vm.isLoading { ProgressView() } }
            .navigationTitle("Adres Ekle")
            .toolbar {
                ToolbarItem {
                    Button("Kapat") { dismiss() }
                }
            }
        }
    }
}
