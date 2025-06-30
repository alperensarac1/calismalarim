//
//  MainVM.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 30.03.2025.
//

import Foundation
import Combine
import Alamofire

class MainVM: ObservableObject {
    
    @Published var hediyeKahve: Int = 0
    @Published var kahveSayisi: Int = 0
    @Published var telefonNumarasi: String? = nil
    
    let qrKodVM = QRKodVM()
    private let kahveServisDao = ServiceImpl.getInstance()
    private let numaraKayitSp = NumaraKayitDao.shared
    
    func kullaniciEkle(numara: String) {
        guard !numara.isEmpty else {
            print("Telefon numarası boş! Kullanıcı eklenemez.")
            return
        }
        
        kahveServisDao.kullaniciEkle(kisiTel: numara) { response in
            DispatchQueue.main.async {
                if let response = response {
                    print("API Yanıtı: \(response.message)")
                    print("API Success: \(response.success)")
                    
                    self.hediyeKahve = response.hediyeKahve
                    self.kahveSayisi = response.icilenKahve
                } else {
                    print("API'den geçerli bir yanıt alınamadı.")
                }
            }
        }
    }
    
    func checkTelefonNumarasi() -> Bool {
        let savedPhoneNumber = numaraKayitSp.numaraGetir()
        guard !savedPhoneNumber.isEmpty else {
            print("Telefon numarası kaydedilmemiş!")
            return false
        }
        
        self.telefonNumarasi = savedPhoneNumber
        kullaniciEkle(numara: savedPhoneNumber)
        
        kahveServisDao.kodUret(dogrulamaKodu: qrKodVM.dogrulamaKodu, kisiTel: savedPhoneNumber) { cevap in
            print("Kod üretildi")
            print(cevap?.message ?? "Mesaj yok")
            print(cevap?.success ?? 0)
        }
        return true
    }
    
    func numarayiKaydet(numara: String) {
        numaraKayitSp.numaraKaydet(kullaniciNumarasi: numara)
        self.telefonNumarasi = numara
        kullaniciEkle(numara: numara)
        
        kahveServisDao.kodUret(dogrulamaKodu: qrKodVM.yeniKodUret(), kisiTel: numara) { cevap in
            print("Kod üretildi")
            print(cevap?.message ?? "Mesaj yok")
            print(cevap?.success ?? 0)
        }
        
        print("Numara kaydedildi: \(numara)")
    }
}
