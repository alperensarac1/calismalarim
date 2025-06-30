//
//  AnaekranScreen.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 22.02.2025.
//
import SwiftUI

import SwiftUI

struct AnaekranScreen: View {
    private var const = Const()
    @State private var uygulamalar: [Uygulama] = []
    @State private var seciliUygulama: Uygulama?
    @State private var gosterAlert = false
    
    let gridColumns = [
        GridItem(.adaptive(minimum: 100))
    ]
    
    var body: some View {
        NavigationView {
            VStack {
                ScrollView {
                    LazyVGrid(columns: gridColumns, spacing: 16) {
                        ForEach(uygulamalar) { uygulama in
                            VStack {
                                Image(systemName: uygulama.uygulamaResmi)
                                    .resizable()
                                    .foregroundColor(.cyan)
                                    .scaledToFit()
                                    .frame(width: 60, height: 60)
                                
                                Text(uygulama.uygulamaAdi)
                                    .font(.caption)
                            }
                            .padding()
                            .onTapGesture {
                                seciliUygulama = uygulama
                                
                            }
                            .onLongPressGesture {
                                seciliUygulama = uygulama
                                gosterAlert = true
                            }
                        }
                    }
                    .padding()
                }
            }
            .navigationBarBackButtonHidden(true)
            .onAppear {
                uygulamalar = const.copKutusundaOlmayanUygulamalar
            }
            .confirmationDialog("Uygulamayı kaldırmak istiyor musunuz?", isPresented: $gosterAlert, titleVisibility: .visible) {
                Button("Çöp Kutusuna Taşı", role: .destructive) {
                    if let seciliUygulama = seciliUygulama,
                       let index = uygulamalar.firstIndex(where: { $0.id == seciliUygulama.id }) {
                        const.copKutusunaTasi(indexPathRow: index, uygulama: seciliUygulama)
                        uygulamalar = const.copKutusundaOlmayanUygulamalar
                    }
                }
                Button("İptal", role: .cancel) { }
            }
            .fullScreenCover(item: $seciliUygulama) { uygulama in
                FullScreenUygulamaView(uygulama: uygulama, const: const)
            }
        }
    }
}


struct AnaekranScreen_Previews: PreviewProvider {
    static var previews: some View {
        AnaekranScreen()
    }
}
