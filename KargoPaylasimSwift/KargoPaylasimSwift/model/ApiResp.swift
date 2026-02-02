//
//  ApiResp.swift
//  KargoPaylasimSwift
//
//  Created by Alperen Saraç on 28.01.2026.
//

import Foundation

struct ApiResp<T: Decodable>: Decodable {
    let ok: Bool
    let data: T?
    let error: String?
}
