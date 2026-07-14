//
//  ApiResponse.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation

/*
    APIResponse

    PHP backend'den gelen ortak cevap modelidir.

    Başarılı örnek:

    {
        "success": true,
        "message": "İşlem başarılı",
        "data": {...}
    }

    Hatalı örnek:

    {
        "success": false,
        "message": "E-posta veya şifre hatalı"
    }

    T generic tiptir.
    data bazen User, bazen [City], bazen Event, bazen Ticket olabilir.
*/
struct APIResponse<T: Decodable>: Decodable {
    let success: Bool
    let message: String
    let data: T?
}
