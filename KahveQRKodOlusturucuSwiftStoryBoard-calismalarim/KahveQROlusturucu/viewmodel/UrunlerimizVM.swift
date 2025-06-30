//
//  UrunlerimizVM.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 31.03.2025.
//

import Foundation
import UIKit

class UrunlerimizVM {
    private let kahveServis: Service
    private var kampanyalar: [Urun] = []
    private var icecekler: [Urun] = []
    private var atistirmaliklar: [Urun] = []

    init(kahveServis: Service) {
        self.kahveServis = kahveServis
    }
    
    func configureCollectionView(for kategori: UrunKategori, collectionView: UICollectionView) {
        kahveServis.kahveWithKategoriId(kategori: kategori) { urunlerListesi in
            DispatchQueue.main.async {
                print("✅ API'den gelen veri (Kategori: \(kategori.kategoriKodu())): \(urunlerListesi?.kahveUrun ?? [])")
                
                guard let urunlerListesi = urunlerListesi else {
                    print("❌ API'den geçerli veri alınamadı.")
                    return
                }
                
                switch kategori {
                case .ICECEKLER:
                    self.icecekler = urunlerListesi.kahveUrun
                case .ATISTIRMALIKLAR:
                    self.atistirmaliklar = urunlerListesi.kahveUrun
                case .INDIRIMLI:
                    break
                }
                collectionView.reloadData()
                collectionView.collectionViewLayout.invalidateLayout() // ✅ Layout'u güncelle
                
            }
        }
    }

    func configureIndirimliCollectionView(collectionView: UICollectionView) {
        kahveServis.tumKahveler { urun in
            guard let urun = urun else {
                print("❌ İndirimli ürünler alınırken hata oluştu.")
                return
            }
            
            let indirimliUrunler = urun.kahveUrun.filter { $0.urun_indirim == "1" }
            self.kampanyalar = indirimliUrunler
            
            DispatchQueue.main.async {
                print("📌 Kampanyalar Reload Data Çağrıldı. Ürün Sayısı: \(self.kampanyalar.count)")
                collectionView.reloadData()
                collectionView.collectionViewLayout.invalidateLayout() // Layout'u yenile
                
            }
            
        }
    }


    func getUrunler(for collectionView: UICollectionView) -> [Urun] {
        print("📌 CollectionView Tag: \(collectionView.tag)") // Debug log
        
        switch collectionView.tag {
        case 1:
            return kampanyalar
        case 2:
            return icecekler
        case 3:
            return atistirmaliklar
        default:
            return []
        }
    }
   
}
