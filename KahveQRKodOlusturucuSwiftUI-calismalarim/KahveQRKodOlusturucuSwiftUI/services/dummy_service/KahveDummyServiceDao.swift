//
//  KahveDummyServiceDao.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 2.04.2025.
//

import Foundation
class KahveDummyServiceDao:Service{
    static let shared = KahveDummyServiceDao()
   

    private init() {}
    
    func kullaniciEkle(kisiTel: String, completion: @escaping (CRUDCevap?) -> Void) {
        completion(CRUDCevap(success: 1, message: "Kullanıcı Eklendi", icilenKahve: 1, hediyeKahve: 0))
    }
    
    func kahveWithKategoriId(kategori: UrunKategori, completion: @escaping (UrunCevap?) -> Void) {
        completion(UrunCevap(kahveUrun: dummyUrunler.filter { $0.urun_kategori_id == "\(kategori.kategoriKodu())" }, success: 1))
    }
    
    func tumKahveler(completion: @escaping (UrunCevap?) -> Void) {
        completion(UrunCevap(kahveUrun: dummyUrunler, success: 1))
    }
    
    func kodUret(dogrulamaKodu: String, kisiTel: String, completion: @escaping (KodUretCevap?) -> Void) {
        completion(KodUretCevap(success: 1, message: "Doğrulama kodu oluşturuldu"))
    }
    
    
}
