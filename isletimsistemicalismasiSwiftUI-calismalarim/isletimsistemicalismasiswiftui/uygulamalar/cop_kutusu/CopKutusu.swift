//
//  CopKutusu.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 24.02.2025.
//

import SwiftUI
struct CopKutusu: View {
    let const :Const
    @State private var copUygulamalar: [Uygulama] = []
    
    let columns: [GridItem] = [
        GridItem(.flexible(), spacing: 16),
        GridItem(.flexible(), spacing: 16),
        GridItem(.flexible(), spacing: 16)
    ]
    
    var body: some View {
        ScrollView {
            LazyVGrid(columns: columns, spacing: 16) {
                ForEach(copUygulamalar.indices, id: \.self) { index in
                    UygulamaCell(uygulama: copUygulamalar[index])
                        .contextMenu {
                            Button("Geri Yükle") {
                                geriYukle(index: index)
                            }
                        }
                }
            }
            .padding()
        }
        .onAppear {
            copUygulamalar = const.copKutusuUygulamalari
        }
    }
    
    private func geriYukle(index: Int) {
        let uygulama = copUygulamalar[index]
        const.copKutusundanCikar(indexPathRow: index, uygulama: uygulama)
        copUygulamalar.remove(at: index)
    }
}

struct UygulamaCell: View {
    let uygulama: Uygulama
    
    var body: some View {
        VStack {
            Image(systemName: uygulama.uygulamaResmi)
                .resizable()
                .scaledToFit()
                .frame(width: 50, height: 50)
            Text(uygulama.uygulamaAdi)
                .font(.headline)
        }
        .padding()
        .background(Color.gray.opacity(0.2))
        .cornerRadius(8)
    }
}

struct CopKutusu_Previews: PreviewProvider {
    static var previews: some View {
        CopKutusu(const: Const())
    }
}
