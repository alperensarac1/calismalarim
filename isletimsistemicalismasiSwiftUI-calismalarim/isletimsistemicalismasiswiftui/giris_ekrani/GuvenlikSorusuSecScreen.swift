//
//  GuvenlikSorusuSecScreen.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 22.02.2025.
//

import SwiftUI

import SwiftUI

struct GuvenlikSorusuSecScreen: View {
    //MARK: PROPERTIES
    @State private var secilenSoru: String?
    @State private var cevap: String = ""
    @State private var navigateToGirisEkrani = false

    let kullaniciBilgileri = KullaniciBilgileri()
    //MARK: BODY
    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                Text("Güvenlik Sorunuzu Seçin")
                    .font(.title2)
                    .bold()

                RadioButton(
                    label: "En sevdiğiniz renk nedir?",
                    isSelected: secilenSoru == "En sevdiğiniz renk nedir?",
                    onTap: { secilenSoru = "En sevdiğiniz renk nedir?" }
                )

                RadioButton(
                    label: "En sevdiğiniz hayvan nedir?",
                    isSelected: secilenSoru == "En sevdiğiniz hayvan nedir?",
                    onTap: { secilenSoru = "En sevdiğiniz hayvan nedir?" }
                )

                
                RadioButton(
                    label: "En sevdiğiniz müzik grubu nedir?",
                    isSelected: secilenSoru == "En sevdiğiniz müzik grubu nedir?",
                    onTap: { secilenSoru = "En sevdiğiniz müzik grubu nedir?" }
                )

                if secilenSoru != nil {
                    TextField("Cevabınızı girin", text: $cevap)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .padding()
                }

                Button("Onayla") {
                    guvenlikSorusuKaydet()
                }
                .buttonStyle(.borderedProminent)
                .disabled(cevap.isEmpty)

                NavigationLink("", destination: GirisEkraniScreen(), isActive: $navigateToGirisEkrani)
            }//:VStack
            .padding()
            .navigationBarBackButtonHidden(true)
        }//:NavigationStack
    }

    private func guvenlikSorusuKaydet() {
        if let secilenSoru = secilenSoru, !cevap.isEmpty {
            kullaniciBilgileri.guvenlikSorusuCevapKaydet(guvenlikSorusuCevap: cevap)
            kullaniciBilgileri.guvenlikSorusuKaydet(guvenlikSorusu: secilenSoru)
            navigateToGirisEkrani = true
        }
    }
}

struct RadioButton: View {
    let label: String
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack {
                Image(systemName: isSelected ? "largecircle.fill.circle" : "circle")
                    .foregroundColor(.blue)
                Text(label)
                    .foregroundColor(.primary)
            }
        }
        .padding(.vertical, 5)
    }
}
struct GuvenlikSorusuSecScreen_Previews: PreviewProvider {
    static var previews: some View {
        GuvenlikSorusuSecScreen()
    }
}
