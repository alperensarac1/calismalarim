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
    
    var hediyeKahveUpdated: ((Int) -> Void)?
    var kahveSayisiUpdated: ((Int) -> Void)?
    var telefonNumarasiUpdated: ((String?) -> Void)?
    let qrKodVM = QRKodVM()
    let kahveServisDao = ServiceImpl.getInstance()
    let numaraKayitSp = NumaraKayitDao.shared
    
    func kullaniciEkle(numara: String) {
        if numara.isEmpty {
            print("Telefon numarası boş! Kullanıcı eklenemez.")
            return
        }
        
        kahveServisDao.kullaniciEkle(kisiTel: numara) { response in
            DispatchQueue.main.async {
                if let response = response {
                    print("API Yanıtı: \(response.message)")
                    print("API Success: \(response.success)")
                    
                    self.hediyeKahveUpdated?(response.hediyeKahve)
                    self.kahveSayisiUpdated?(response.icilenKahve)
                } else {
                    print("API'den geçerli bir yanıt alınamadı. MAIN VM 36")
                }
            }
        }
    }
    
    func checkTelefonNumarasi() -> Bool {
        let savedPhoneNumber = numaraKayitSp.numaraGetir()
        if savedPhoneNumber.isEmpty {
            print("Telefon numarası kaydedilmemiş!")
            return false
        }
        
        telefonNumarasiUpdated?(savedPhoneNumber)
        kullaniciEkle(numara: savedPhoneNumber)
        kahveServisDao.kodUret(dogrulamaKodu: qrKodVM.yeniKodUret(), kisiTel: savedPhoneNumber) { cevap in
            print("MAIN VM")
            print(cevap?.message)
            print(cevap?.success)
        }
        return true
    }
    
    func numarayiKaydet(numara: String) {
        numaraKayitSp.numaraKaydet(kullaniciNumarasi: numara)
        checkTelefonNumarasi()
        kullaniciEkle(numara: numara)
        kahveServisDao.kodUret(dogrulamaKodu: qrKodVM.yeniKodUret(), kisiTel: numara) { cevap in
            print("MAIN VM")
            print(cevap?.message)
            print(cevap?.success)
        }
        print("Numara kaydedildi: \(numara)")
        telefonNumarasiUpdated?(numara)
    }
}

