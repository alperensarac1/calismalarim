//
//  SifreOlusturScreen.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 22.02.2025.
//

import SwiftUI

struct SifreOlusturScreen: View {
    
    //MARK: PROPERTIES
    
    @State private var sifre: String = ""
    @State private var sifreTekrar: String = ""
    @State private var sifreGoster: Bool = false
    @State private var sifreTekrarGoster: Bool = false
    @State private var showAlert = false
    @State private var alertMessage = ""
    @State private var navigateToGuvenlikSorusu = false
    @State private var navigateToGiris = false
    
    let kullaniciBilgileri = KullaniciBilgileri()
    
    //MARK: BODY
    var body: some View {
        NavigationStack {
            
            VStack(spacing: 40) {
                
                Image("adios").resizable().frame(width: 200,height: 200)
                
                

                VStack(alignment: .leading) {
                    Text("Şifre")
                    HStack {
                        if sifreGoster {
                            TextField("Şifrenizi girin", text: $sifre)
                        } else {
                            SecureField("Şifrenizi girin", text: $sifre)
                        }
                        Button(action: {
                            sifreGoster.toggle()
                        }) {
                            Image(systemName: sifreGoster ? "eye.slash.fill" : "eye.fill")
                                .foregroundColor(.gray)
                        }
                    }//:HStack
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                }//:VStack


                VStack(alignment: .leading) {
                    Text("Şifre Tekrar")
                    HStack {
                        if sifreTekrarGoster {
                            TextField("Şifrenizi tekrar girin", text: $sifreTekrar)
                        } else {
                            SecureField("Şifrenizi tekrar girin", text: $sifreTekrar)
                        }
                        Button(action: {
                            sifreTekrarGoster.toggle()
                        }) {
                            Image(systemName: sifreTekrarGoster ? "eye.slash.fill" : "eye.fill")
                                .foregroundColor(.gray)
                        }
                    }//:HStack
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                }//:VStack


                Button("Kaydet") {
                    sifreKaydet()
                }//:Button
                .buttonStyle(.borderedProminent)
                .disabled(sifre.isEmpty || sifreTekrar.isEmpty)

                NavigationLink("", destination: GuvenlikSorusuSecScreen(), isActive: $navigateToGuvenlikSorusu)
                NavigationLink("", destination: GirisEkraniScreen(), isActive: $navigateToGiris)
            }
            .padding()
            .navigationBarBackButtonHidden(true)
            .alert(isPresented: $showAlert) {
                Alert(title: Text("Hata"), message: Text(alertMessage), dismissButton: .default(Text("Tamam")))
            }
            .onAppear {
                if kullaniciBilgileri.sifreKaydiOlusturulmusMu() {
                    navigateToGiris = true
                }
            }
        }
    }

    private func sifreKaydet() {
        guard !sifre.isEmpty, !sifreTekrar.isEmpty else {
            alertMessage = "Şifre alanları boş bırakılamaz."
            showAlert = true
            return
        }

        if sifre == sifreTekrar {
            kullaniciBilgileri.sifreKaydi(sifre: sifre)
            navigateToGuvenlikSorusu = true
        } else {
            alertMessage = "Şifreler eşleşmiyor."
            showAlert = true
        }
    }//:Function End
}
struct SifreOlusturScreen_Previews: PreviewProvider {
    static var previews: some View {
        SifreOlusturScreen()
    }
}
