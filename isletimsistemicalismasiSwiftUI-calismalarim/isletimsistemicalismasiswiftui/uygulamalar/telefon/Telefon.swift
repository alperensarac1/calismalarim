//
//  Telefon.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//



import SwiftUI

struct Telefon: View {
    @State private var phoneNumber: String = ""
    @State private var showCallAlert = false
    @State private var showSaveAlert = false
    @State private var contactName: String = ""
    var rehberDao = RehberDao()

    var body: some View {
        VStack(spacing: 20) {
            TextField("Telefon Numarası", text: $phoneNumber)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .keyboardType(.phonePad)
                .padding()

            HStack {
                Button(action: {
                    showCallAlert = true
                }) {
                    Label("Ara", systemImage: "phone.fill")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .tint(.green)

                Button(action: sendMessage) {
                    Label("Mesaj Gönder", systemImage: "message.fill")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .tint(.blue)
            }
            .padding()

            Button(action: {
                showSaveAlert = true
            }) {
                Label("Rehbere Ekle", systemImage: "person.crop.circle.badge.plus")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .tint(.orange)
            .padding()

        }
        .alert("Arama Yap", isPresented: $showCallAlert) {
            Button("İptal", role: .cancel) {}
            Button("Evet", action: callNumber)
        } message: {
            Text("\(phoneNumber) numarasını aramak istiyor musunuz?")
        }
        .alert("Rehbere Ekle", isPresented: $showSaveAlert) {
            TextField("Kişinin İsmi", text: $contactName)
            Button("İptal", role: .cancel) {}
            Button("Kaydet", action: saveContact)
        } message: {
            Text("Rehbere eklenecek kişinin ismini giriniz.")
        }
    }

    func callNumber() {
        guard let url = URL(string: "tel://\(phoneNumber)"),
              UIApplication.shared.canOpenURL(url) else {
            print("Arama yapılamıyor")
            return
        }
        UIApplication.shared.open(url)
    }

    func sendMessage() {
        guard let url = URL(string: "sms:\(phoneNumber)"),
              UIApplication.shared.canOpenURL(url) else {
            print("SMS gönderilemiyor")
            return
        }
        UIApplication.shared.open(url)
    }

    func saveContact() {
        print("Kişi Kaydedildi: \(contactName) - \(phoneNumber)")
        rehberDao.kisiEkle(kisi_ad: contactName, kisi_tel: phoneNumber)
    }
}


struct Telefon_Previews: PreviewProvider {
    static var previews: some View {
        Telefon()
    }
}
