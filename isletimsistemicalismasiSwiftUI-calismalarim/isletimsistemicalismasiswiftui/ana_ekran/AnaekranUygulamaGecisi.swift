//
//  AnaekranUygulamaGecisi.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 24.02.2025.
//
import SwiftUI
import Foundation
struct FullScreenUygulamaView: View {
    @Environment(\.dismiss) var dismiss
    
    var uygulama: Uygulama
    var const: Const
    
    var body: some View {
        VStack {
            HStack {
                Button(action: {
                    dismiss()
                }) {
                    
                    Image(systemName: "chevron.left")
                        .font(.title)
                        .foregroundColor(.blue)
                        .padding()
                    Text("Geri Dön")
                }
                Spacer()
            }
            
            Spacer()
            
            if uygulama.uygulamaResmi == "basket" {
                CopKutusu(const: const)
            } else {
                uygulama.uygulamaGecisId
            }
            
            Spacer()
        }
        .edgesIgnoringSafeArea(.horizontal)
    }
}
