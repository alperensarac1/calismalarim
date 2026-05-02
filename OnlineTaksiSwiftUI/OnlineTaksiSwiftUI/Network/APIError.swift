//
//  APIError.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation

enum APIError: Error {
    case invalidURL
    case invalidResponse
    case decodingFailed
    case serverError(String)
    case unknown(String)
}
