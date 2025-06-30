//
//  NumaraKayitDao.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 30.03.2025.
//

import Foundation

class NumaraKayitDao {
    static let shared = NumaraKayitDao()
    private let defaults = UserDefaults.standard

    private init() {} // Dışarıdan yeni bir instance oluşturmayı engeller

    func numaraKaydedildi() {
        defaults.set(true, forKey: "kullaniciNumarasiKayitliMi")
    }

    func numaraKaydedildiMi() -> Bool {
        return defaults.bool(forKey: "kullaniciNumarasiKayitliMi")
    }

    func numaraGetir() -> String {
        return defaults.string(forKey: "numara") ?? ""
    }

    func numaraKaydet(kullaniciNumarasi: String) {
        defaults.set(kullaniciNumarasi, forKey: "numara")
        numaraKaydedildi()
    }
}

