//
//  PhoneUtil.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation

enum PhoneUtil {
    static func normalizeTrToE164(_ input: String) -> String {
        let digits = input.filter { $0.isNumber }
        if digits.isEmpty { return "" }

        if digits.count == 11, digits.first == "0" {
            return "+90" + digits.dropFirst()
        }
        if digits.count == 10, digits.first == "5" {
            return "+90" + digits
        }
        if digits.hasPrefix("90"), digits.count >= 12 {
            return "+" + digits
        }
        if digits.count >= 12 {
            return "+" + digits
        }
        return input.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    static func isLikelyTrPhoneE164(_ phone: String) -> Bool {
        let digits = phone.filter { $0.isNumber }
        return digits.count == 12 && digits.hasPrefix("90") && digits.dropFirst(2).first == "5"
    }
}

private extension String {
    func hasPrefix(_ prefix: String) -> Bool { self.starts(with: prefix) }
}
