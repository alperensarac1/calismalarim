//
//  AppError.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 9.03.2026.
//

import Foundation

enum AppError: Error {
    case message(String)
}

extension AppError: LocalizedError {
    var errorDescription: String? {
        switch self {
        case .message(let msg):
            return msg
        }
    }
}
