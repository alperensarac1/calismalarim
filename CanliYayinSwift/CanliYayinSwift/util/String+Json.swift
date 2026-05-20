//
//  String+Json.swift
//  CanliYayinSwift
//
//  Created by Alperen Saraç on 13.05.2026.
//

import Foundation
import Foundation

extension String {

    func toJsonDictionary() -> [String: Any]? {
        guard let data = self.data(using: .utf8) else {
            return nil
        }

        return try? JSONSerialization.jsonObject(
            with: data,
            options: []
        ) as? [String: Any]
    }
}
