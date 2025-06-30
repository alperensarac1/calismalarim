//
//  GirisEkraniScreen.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 22.02.2025.
//


import SwiftUI

struct GirisEkraniScreen: View {
    //MARK: PROPERTIES

    @State private var sifre: String = ""
    @State private var sifreGoster: Bool = false
    @State private var hataMesaji: String?
    @State private var navigateToAnasayfa = false
    @State private var navigateToGuvenlikSorusu = false
    @State private var alertMessage: AlertMessage?

    let kullaniciBilgileri = KullaniciBilgileri()

    //MARK: BODY
    var body: some View {
        NavigationStack {
            VStack(spacing: 40) {
                
                Image("adios").resizable().frame(width: 200,height: 200)
                
                HStack {
                    if sifreGoster {
                        TextField("Şifrenizi girin", text: $sifre)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                    } else {
                        SecureField("Şifrenizi girin", text: $sifre)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                    }

                    Button(action: { sifreGoster.toggle() }) {
                        Image(systemName: sifreGoster ? "eye.slash.fill" : "eye.fill")
                            .foregroundColor(.gray)
                    }//:Button
                    .padding(.vertical,15)
                }//:HStack
                .padding(.horizontal)

                Button("Giriş Yap") {
                    girisYap()
                }//:Button
                .buttonStyle(.borderedProminent).padding(.vertical,15)

                HStack{
                    Spacer()
                    Button("Şifremi Unuttum") {
                        kullaniciBilgileri.sifreKaydiOlusturulmusMuFalse()
                        navigateToGuvenlikSorusu = true
                    }
                }//:HStack
                .foregroundColor(.red)

                NavigationLink("", destination: AnaekranScreen(), isActive: $navigateToAnasayfa)
                NavigationLink("", destination: GuvenlikSorusuScreen(), isActive: $navigateToGuvenlikSorusu)
            }//:VStack
            .padding()
            .navigationBarBackButtonHidden(true)
            .alert(item: $alertMessage) { alert in
                Alert(title: Text("Hata"), message: Text(alert.message), dismissButton: .default(Text("Tamam")))
            }
        }
    }

    private func girisYap() {
        guard !sifre.isEmpty else {
            alertMessage?.message = "Şifre alanı boş bırakılamaz."
            return
        }
        
        if kullaniciBilgileri.sifreSorgulama(dogrulanacakSifre: sifre) {
            navigateToAnasayfa = true
        } else {
            alertMessage?.message = "Şifreler eşleşmiyor."
        }
    }//:Function End
}
struct GirisEkraniScreen_Previews: PreviewProvider {
    static var previews: some View {
        GirisEkraniScreen()
    }
}
struct AlertMessage: Identifiable {
    var id = UUID()
    var message: String
}
