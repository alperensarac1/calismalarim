//
//  RehberViewModel.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//

import Foundation
class RehberViewModel: ObservableObject {
    var dao = RehberDao()
    @Published var kisiler: [Kisi] = [
        
    ]
    
    @Published var aramaMetni = ""

    func kisiEkleGuncelle(kisi: Kisi) {
        if let index = kisiler.firstIndex(where: { $0.id == kisi.id }) {
            kisiler[index] = kisi
        } else {
            dao.kisiEkle(kisi_ad: kisi.kisi_ad!, kisi_tel: kisi.kisi_tel!)
        }
    }
    
    func kisiSil(kisi: Kisi) {
        dao.kisiSil(kisi: kisi)
    }
    
    
    var filtrelenmisKisiler: [Kisi] {
        if aramaMetni.isEmpty {
            return dao.kisileriGetir()
        } else {
            return dao.kisiGetir(kisi_ad: aramaMetni)
        }
    }
}
