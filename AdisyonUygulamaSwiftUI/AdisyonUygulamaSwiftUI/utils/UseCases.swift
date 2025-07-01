//
//  UseCases.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 6.06.2025.
//

import Foundation

extension Float {
    func fiyatYaz() -> String {
        let normalized: Float = (abs(self) < Float.ulpOfOne) ? 0.0 : self
        return String(format: "%.2f ₺", normalized)
    }
}

