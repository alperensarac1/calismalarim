//
//  Service.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 2.04.2025.
//

import Foundation
protocol Service{
    func kullaniciEkle(kisiTel: String, completion: @escaping (CRUDCevap?) -> Void)
    func kahveWithKategoriId(kategori: UrunKategori, completion: @escaping (UrunCevap?) -> Void)
    func tumKahveler(completion: @escaping (UrunCevap?) -> Void)
    func kodUret(dogrulamaKodu: String, kisiTel: String, completion: @escaping (KodUretCevap?) -> Void)
}
