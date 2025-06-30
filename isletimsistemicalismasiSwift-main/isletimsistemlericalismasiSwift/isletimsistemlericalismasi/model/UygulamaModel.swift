//
//  UygulamaModel.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 19.01.2025.
//
import Foundation
class UygulamaModel{
    var d:UserDefaults
    
    init() {
        d = UserDefaults.standard
    }
    func copKutusunaEkle(uygulama:Uygulama){
        d.set(true, forKey: "\(uygulama.uygulamaAdi)Copkutusu")
    }
    func copKutusundanCikar(uygulama:Uygulama){
        d.set(false,forKey: "\(uygulama.uygulamaAdi)Copkutusu")
    }
    func copKutusundaMi(uygulamaAdi:String)->Bool{
        return d.bool(forKey: "\(uygulamaAdi)Copkutusu")
    }
}
