//
//  GuvenlikSorusuScreen.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 22.02.2025.
//

import SwiftUI

import SwiftUI

struct GuvenlikSorusuScreen: View {
    //MARK: PROPERTIES
    @State private var guvenlikSorusu: String = ""
    @State private var cevap: String = ""
    @State private var navigateToSifreOlustur = false
    @State private var showAlert = false
    @State private var alertMessage = ""

    let kullaniciBilgileri = KullaniciBilgileri()
    //MARK: BODY
    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                Text("Güvenlik Sorusu")
                    .font(.title2)
                    .bold()

                Text(guvenlikSorusu)
                    .font(.headline)
                    .padding()

                TextField("Cevabınızı girin", text: $cevap)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .padding()

                Button("Onayla") {
                    guvenlikSorusuDogrula()
                }//:Button
                .buttonStyle(.borderedProminent)
                .disabled(cevap.isEmpty)

                NavigationLink("", destination: SifreOlusturScreen(), isActive: $navigateToSifreOlustur)
            }//:VStack
            .padding()
            .onAppear {
                guvenlikSorusu = kullaniciBilgileri.guvenlikSorusuGetir() ?? "Bilinmeyen Soru"
            }
            .alert(isPresented: $showAlert) {
                Alert(title: Text("Hata"), message: Text(alertMessage), dismissButton: .default(Text("Tamam")))
            }
        }//:NavigationStack
    }

    private func guvenlikSorusuDogrula() {
        if kullaniciBilgileri.guvenlikSorusuDogrula(dogrulanacakCevap: cevap) {
            navigateToSifreOlustur = true
        } else {
            alertMessage = "Cevabınız eşleşmiyor."
            showAlert = true
        }
    }//:Function End
}
struct GuvenlikSorusuScreen_Previews: PreviewProvider {
    static var previews: some View {
        GuvenlikSorusuScreen()
    }
}
