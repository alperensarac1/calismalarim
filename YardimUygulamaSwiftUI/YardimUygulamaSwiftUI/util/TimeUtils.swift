//
//  TimeUtils.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation

enum TimeUtils {
    static func formatRemainingSeconds(_ sec: Int) -> String {
        let s = max(0, sec)
        let mm = s / 60
        let ss = s % 60
        return String(format: "%02d:%02d", mm, ss)
    }
}
