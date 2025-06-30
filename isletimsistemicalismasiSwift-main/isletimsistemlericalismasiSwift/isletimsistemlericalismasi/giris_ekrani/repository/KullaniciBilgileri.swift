//
//  KullaniciBilgileri.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 2.01.2025.
//

import Foundation

class KullaniciBilgileri{
    var d:UserDefaults
    
    init() {
        d = UserDefaults.standard
    }
    func sifreKaydiOlusturulmusMu()->Bool{
        return d.bool(forKey: "kayitolusturulmusmu") 
    }
    func sifreKaydiOlusturulmusMuTrue(){
        d.set(true, forKey: "kayitolusturulmusmu")
    }
    func sifreKaydiOlusturulmusMuFalse(){
        d.set(false, forKey: "kayitolusturulmusmu")
    }
    func sifreKaydi(sifre:String){
        d.set(sifre, forKey: "sifre")
        d.set(true, forKey: "kayitolusturulmusmu")
    }
    func sifreSorgulama(dogrulanacakSifre:String)->Bool{
        if let alinanSifre = d.string(forKey: "sifre"){
            return dogrulanacakSifre.elementsEqual(alinanSifre)
        }else{
            return false
        }
    }
    func guvenlikSorusuKaydet(guvenlikSorusu:String){
        d.set(guvenlikSorusu, forKey: "guvenliksorusu")
    }
    func guvenlikSorusuGetir()->String{
        return d.string(forKey: "guvenliksorusu") ?? ""
    }
    func guvenlikSorusuCevapKaydet(guvenlikSorusuCevap:String){
        d.set(guvenlikSorusuCevap, forKey: "guvenliksorusucevap")
    }
    func guvenlikSorusuDogrula(dogrulanacakCevap:String)->Bool{
        if let kayitliCevap =  d.string(forKey: "guvenliksorusucevap"){
            return kayitliCevap.elementsEqual(dogrulanacakCevap)
        }else{
            return false
        }
    }
    func sifreSorulsunMuDegistir(sifreSorulsunMu:Bool){
        d.set(sifreSorulsunMu, forKey: "sifresorulsunmu")
    }
    func sifreDegistir(degistirilecekSifre:String){
        d.set(degistirilecekSifre, forKey: "sifre")
    }
    func kullaniciBilgileriniSifirla(){
        d.removeObject(forKey: "sifre")
        d.removeObject(forKey: "sifresorulsunmu")
        d.removeObject(forKey: "guvenliksorusu")
        d.removeObject(forKey: "kayitolusturulmusmu")
    }
}
