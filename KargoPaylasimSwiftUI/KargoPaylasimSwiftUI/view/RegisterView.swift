//
//  RegisterView.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation
import SwiftUI

struct RegisterView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject var vm: AuthVM

    // User fields
    @State private var first = ""
    @State private var last = ""
    @State private var phone = ""
    @State private var tc = ""
    @State private var password = ""

    // Address fields (register ile birlikte)
    @State private var addrTitle = ""
    @State private var city = ""
    @State private var district = ""
    @State private var neighborhood = ""
    @State private var addressLine = ""
    @State private var postal = ""

    @State private var doneAlert = false

    var body: some View {
        NavigationStack {
            Form {

                Section("Kişisel Bilgiler") {
                    TextField("İsim", text: $first)
                    TextField("Soyisim", text: $last)

                    TextField("Telefon (05xx...)", text: $phone)
                        .keyboardType(.phonePad)

                    TextField("TC Kimlik No", text: $tc)
                        .keyboardType(.numberPad)

                    SecureField("Şifre", text: $password)
                }

                Section("Adres Bilgileri") {
                    TextField("Adres başlığı (Ev/İş)", text: $addrTitle)
                    TextField("Şehir", text: $city)
                    TextField("İlçe", text: $district)
                    TextField("Mahalle (opsiyonel)", text: $neighborhood)

                    TextEditor(text: $addressLine)
                        .frame(minHeight: 90)

                    TextField("Posta kodu (opsiyonel)", text: $postal)
                        .keyboardType(.numberPad)
                }

                Section {
                    Button("Kayıt Ol") {
                        Task {
                            let ok = await vm.doRegister(
                                first: first, last: last, phoneRaw: phone, tc: tc, password: password,
                                addrTitle: addrTitle, city: city, district: district,
                                neighborhood: neighborhood, addressLine: addressLine, postal: postal
                            )
                            if ok { doneAlert = true }
                        }
                    }
                    .disabled(vm.isLoading)
                }

                if let err = vm.errorText {
                    Section { Text(err).foregroundStyle(.red) }
                }
            }
            .overlay { if vm.isLoading { ProgressView() } }
            .navigationTitle("Kayıt Ol")
            .toolbar {
                ToolbarItem {
                    Button("Kapat") { dismiss() }
                }
            }
            .alert("Kayıt Başarılı", isPresented: $doneAlert) {
                Button("Girişe Dön") {
                    dismiss()
                }
            } message: {
                Text("Artık giriş yapabilirsin.")
            }
        }
    }
}
