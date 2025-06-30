//
//  Const.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 22.02.2025.
//

import Foundation
import SwiftUI
class Const {
    
    var uygulamaModel: UygulamaModel = UygulamaModel()
    var uygulamalar: [Uygulama] = []
    var copKutusuUygulamalari: [Uygulama] = []
    var copKutusundaOlmayanUygulamalar: [Uygulama] = []
    
    init() {
        uygulamaModel = UygulamaModel()
        uygulamalariYükle()
    }
    
    func uygulamalariYükle() {
        let uygulamalar = [
            Uygulama(uygulamaAdi: "Çöp Kutusu", uygulamaResmi: "basket", uygulamaGecisId: nil, copKutusundaMi: false),
            Uygulama(uygulamaAdi: "Telefon", uygulamaResmi: "phone.arrow.up.right", uygulamaGecisId: AnyView(Telefon()), copKutusundaMi: uygulamaModel.copKutusundaMi(uygulamaAdi: "Telefon")),
            Uygulama(uygulamaAdi: "Hesap Makinesi", uygulamaResmi: "number.square", uygulamaGecisId: AnyView(HesapMakinesi()), copKutusundaMi: uygulamaModel.copKutusundaMi(uygulamaAdi: "Hesap Makinesi")),
            Uygulama(uygulamaAdi: "QR Kod Okuyucu", uygulamaResmi: "qrcode", uygulamaGecisId: AnyView(QRKodOkuyucu()), copKutusundaMi: uygulamaModel.copKutusundaMi(uygulamaAdi: "QR Kod Okuyucu")),
            Uygulama(uygulamaAdi: "Rehber", uygulamaResmi: "person.fill", uygulamaGecisId: AnyView(RehberAnasayfa()), copKutusundaMi: uygulamaModel.copKutusundaMi(uygulamaAdi: "Rehber")),
            Uygulama(uygulamaAdi: "Not Defteri", uygulamaResmi: "note.text", uygulamaGecisId: AnyView(NotDefteriAnasayfa()), copKutusundaMi: uygulamaModel.copKutusundaMi(uygulamaAdi: "Not Defteri")),
            Uygulama(uygulamaAdi: "Galeri", uygulamaResmi: "photo.on.rectangle.angled", uygulamaGecisId: AnyView(Galeri()), copKutusundaMi: uygulamaModel.copKutusundaMi(uygulamaAdi: "Galeri")),
            Uygulama(uygulamaAdi: "Kamera", uygulamaResmi: "camera.fill", uygulamaGecisId: AnyView(Kamera()), copKutusundaMi: uygulamaModel.copKutusundaMi(uygulamaAdi: "Kamera")),
            Uygulama(uygulamaAdi: "Tarayıcı", uygulamaResmi: "network", uygulamaGecisId: AnyView(Tarayici()), copKutusundaMi: uygulamaModel.copKutusundaMi(uygulamaAdi: "Tarayıcı")),
            Uygulama(uygulamaAdi: "Çalar Saat", uygulamaResmi: "calendar.badge.clock", uygulamaGecisId: AnyView(Alarm()), copKutusundaMi: uygulamaModel.copKutusundaMi(uygulamaAdi: "Çalar Saat")),
            Uygulama(uygulamaAdi: "Takvim", uygulamaResmi: "calendar", uygulamaGecisId: AnyView(Takvim()), copKutusundaMi: uygulamaModel.copKutusundaMi(uygulamaAdi: "Takvim")),
            Uygulama(uygulamaAdi: "Müzik Çalar", uygulamaResmi: "music.note.list", uygulamaGecisId: AnyView(MuzikCalar()), copKutusundaMi: uygulamaModel.copKutusundaMi(uygulamaAdi: "Müzik Çalar"))
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
