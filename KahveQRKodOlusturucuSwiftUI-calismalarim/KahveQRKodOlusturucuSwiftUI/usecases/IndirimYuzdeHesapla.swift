//
//  IndirimYuzdeHesapla.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 29.03.2025.
//

import Foundation
func indirimYuzdeHesapla(indirimsizFiyat: String, indirimliFiyat: String) -> String {
    
    if let indirimsizFiyatDouble = Double(indirimsizFiyat),let indirimliFiyatDouble = Double(indirimliFiyat){
        if indirimsizFiyatDouble <= 0.0{
            return "0.0"
        }
        let indirimOrani = ((indirimsizFiyatDouble - indirimliFiyatDouble) / indirimsizFiyatDouble) * 100
        return String(format: "%.1f", indirimOrani)
    }
    return "0.0"
}
