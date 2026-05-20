//
//  String+Json.swift
//  CanliYayinSwiftUI
//
//  Created by Alperen Saraç on 14.05.2026.
//

import Foundation

extension String {
    func toJsonDictionary() -> [String: Any]? {
        guard let data = data(using: .utf8) else {
            return nil
        }

        return try? JSONSerialization.jsonObject(
            with: data,
            options: []
        ) as? [String: Any]
    }
}
