//
//  Extensions.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import Foundation
import UIKit

extension String {
    var asTrDate: String {
        let inFmt = DateFormatter()
        inFmt.locale = Locale(identifier: "tr_TR")
        inFmt.dateFormat = "yyyy-MM-dd HH:mm:ss"
        let outFmt = DateFormatter()
        outFmt.locale = Locale(identifier: "tr_TR")
        outFmt.dateFormat = "dd.MM.yyyy"
        if let d = inFmt.date(from: self) { return outFmt.string(from: d) }
        // saat yoksa sadece ilk 10 karakterden dene
        let short = String(self.prefix(10))
        inFmt.dateFormat = "yyyy-MM-dd"
        if let d = inFmt.date(from: short) { return outFmt.string(from: d) }
        return short
    }
}


