//
//  UrunlerimizVM.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 31.03.2025.
//

import Foundation

class UrunlerimizVM: ObservableObject {
    @Published var kampanyalar: [Urun] = []
    @Published var icecekler: [Urun] = []
    @Published var atistirmaliklar: [Urun] = []

    private let kahveServis: Service

    init(kahveServis: Service) {
        self.kahveServis = kahveServis
        fetchIndirimliUrunler()
        fetchIcecekler()
        fetchAtistirmaliklar()
    }

    func fetchIndirimliUrunler() {
        kahveServis.tumKahveler { urunCevap in
            DispatchQueue.main.async {
                if let urunler = urunCevap?.kahveUrun {
                    self.kampanyalar = urunler.filter { $0.urun_indirim == "1" }
                }
            }
        }
    }

    func fetchIcecekler() {
        kahveServis.kahveWithKategoriId(kategori: .ICECEKLER) { urunCevap in
            DispatchQueue.main.async {
                if let urunler = urunCevap?.kahveUrun {
                    self.icecekler = urunler
                }
            }
        }
    }

    func fetchAtistirmaliklar() {
        kahveServis.kahveWithKategoriId(kategori: .ATISTIRMALIKLAR) { urunCevap in
            DispatchQueue.main.async {
                if let urunler = urunCevap?.kahveUrun {
                    self.atistirmaliklar = urunler
                }
            }
        }
    }
}
