//
//  Consts.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 19.01.2025.
//

import Foundation

class Consts {
    
    var uygulamaModel: UygulamaModel = UygulamaModel()
    var uygulamalar: [Uygulama] = []
    var copKutusuUygulamalari: [Uygulama] = []
    var copKutusundaOlmayanUygulamalar: [Uygulama] = []
    
    init() {
        uygulamaModel = UygulamaModel()
        uygulamalariYükle()
    }
    
    func uygulamalariYükle() {
        uygulamalar = [
            Uygulama(uygulamaAdi: "Çöp Kutusu", uygulamaResmi: "basket", uygulamaGecisId: "toCopKutusu", copKutusundaMi: false),
            Uygulama(uygulamaAdi: "Telefon", uygulamaResmi: "phone.arrow.up.right", uygulamaGecisId: "toTelefon", copKutusundaMi: self.uygulamaModel.copKutusundaMi(uygulamaAdi: "Telefon")),
            Uygulama(uygulamaAdi: "Hesap Makinesi", uygulamaResmi: "numbers.rectangle", uygulamaGecisId: "toHesapMakinesi", copKutusundaMi: self.uygulamaModel.copKutusundaMi(uygulamaAdi: "Hesap Makinesi")),
            Uygulama(uygulamaAdi: "QR Kod Okuyucu", uygulamaResmi: "qrcode", uygulamaGecisId: "toQRKodOkuyucu", copKutusundaMi: self.uygulamaModel.copKutusundaMi(uygulamaAdi: "QR Kod Okuyucu")),
            Uygulama(uygulamaAdi: "Rehber", uygulamaResmi: "person.fill", uygulamaGecisId: "toRehber", copKutusundaMi: self.uygulamaModel.copKutusundaMi(uygulamaAdi: "Rehber")),
            Uygulama(uygulamaAdi: "Not Defteri", uygulamaResmi: "note.text", uygulamaGecisId: "toNotDefteri", copKutusundaMi: self.uygulamaModel.copKutusundaMi(uygulamaAdi: "Not Defteri")),
            Uygulama(uygulamaAdi: "Galeri", uygulamaResmi: "photo.on.rectangle.angled", uygulamaGecisId: "toGaleri", copKutusundaMi: self.uygulamaModel.copKutusundaMi(uygulamaAdi: "Galeri")),
            Uygulama(uygulamaAdi: "Kamera", uygulamaResmi: "camera.fill", uygulamaGecisId: "toKamera", copKutusundaMi: self.uygulamaModel.copKutusundaMi(uygulamaAdi: "Kamera")),
            Uygulama(uygulamaAdi: "Tarayıcı", uygulamaResmi: "network", uygulamaGecisId: "toTarayici", copKutusundaMi: self.uygulamaModel.copKutusundaMi(uygulamaAdi: "Tarayıcı")),
            Uygulama(uygulamaAdi: "Çalar Saat", uygulamaResmi: "calendar.badge.clock", uygulamaGecisId: "toCalarSaat", copKutusundaMi: self.uygulamaModel.copKutusundaMi(uygulamaAdi: "Çalar Saat")),
            Uygulama(uygulamaAdi: "Takvim", uygulamaResmi: "calendar", uygulamaGecisId: "toTakvim", copKutusundaMi: self.uygulamaModel.copKutusundaMi(uygulamaAdi: "Takvim")),
            Uygulama(uygulamaAdi: "Müzik Çalar", uygulamaResmi: "music.note.list", uygulamaGecisId: "toMuzikCalar", copKutusundaMi: self.uygulamaModel.copKutusundaMi(uygulamaAdi: "Müzik Çalar")),
        ]
        
        uygulamalar.forEach { uygulama in
            if uygulama.copKutusundaMi == false {
                copKutusundaOlmayanUygulamalar.append(uygulama)
            } else {
                copKutusuUygulamalari.append(uygulama)
            }
        }
    }
    func copKutusunaTasi(indexPathRow:Int,uygulama:Uygulama){
        copKutusundaOlmayanUygulamalar.remove(at: indexPathRow)
        copKutusuUygulamalari.append(uygulama)
        self.uygulamaModel.copKutusunaEkle(uygulama: uygulama)
    }
    func copKutusundanCikar(indexPathRow:Int,uygulama:Uygulama){
        copKutusuUygulamalari.remove(at: indexPathRow)
        copKutusundaOlmayanUygulamalar.append(uygulama)
        self.uygulamaModel.copKutusundanCikar(uygulama: uygulama)
    }
}
