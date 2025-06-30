//
//  RehberAnasayfa.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//

import SwiftUI

struct RehberAnasayfa: View {
    @StateObject private var viewModel = RehberViewModel()
    @State private var secilenKisi: Kisi?

    var body: some View {
        NavigationView {
            List {
                ForEach(viewModel.filtrelenmisKisiler) { kisi in
                    VStack(alignment: .leading) {
                        Text(kisi.kisi_ad!).font(.headline)
                        Text(kisi.kisi_tel!).font(.subheadline).foregroundColor(.gray)
                    }
                    .onTapGesture {
                        secilenKisi = kisi
                    }
                }
                .onDelete { indexSet in
                    indexSet.forEach { index in
                        viewModel.kisiSil(kisi: viewModel.filtrelenmisKisiler[index])
                    }
                }
            }
            .navigationTitle("Rehber")
            .searchable(text: $viewModel.aramaMetni, prompt: "İsim Ara...")
            .toolbar {
                Button(action: {
                    secilenKisi = Kisi()
                    secilenKisi?.kisi_ad = ""
                    secilenKisi?.kisi_tel = ""
                }) {
                    Image(systemName: "plus")
                }
            }
            .sheet(item: $secilenKisi) { kisi in
                RehberDetaysayfa(viewModel: viewModel, kisi: kisi)
            }
        }
    }
}

struct RehberAnasayfa_Previews: PreviewProvider {
    static var previews: some View {
        RehberAnasayfa()
    }
}
