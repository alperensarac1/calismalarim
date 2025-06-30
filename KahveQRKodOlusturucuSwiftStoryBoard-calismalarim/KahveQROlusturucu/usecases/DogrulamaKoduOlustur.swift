//
//  DogrulamaKoduOlustur.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 29.03.2025.
//

import Foundation
import Combine

class DogrulamaKoduOlustur: ObservableObject {
    @Published var dogrulamaKodu: String = ""

    func dogrulamaKodunuOlustur() {
        dogrulamaKodu = randomIdOlustur()
    }

    func randomIdOlustur() -> String {
        return UUID().uuidString
    }
}
