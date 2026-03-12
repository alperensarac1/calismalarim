//
//  MapUtils.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation
import UIKit

enum MapUtils {
    static func openInAppleMaps(lat: Double, lng: Double, label: String = "Hasta Konumu") {
        let ll = "\(lat),\(lng)"
        let q = label.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "Konum"
        let urlStr = "http://maps.apple.com/?q=\(q)&ll=\(ll)"
        guard let url = URL(string: urlStr) else { return }
        UIApplication.shared.open(url)
    }
}
