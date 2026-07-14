//
//  ApiResponse.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 25.06.2026.
//

import Foundation
/*
    APIResponse

    PHP backend'den gelen ortak cevap modelidir.

    Başarılı cevap örneği:

    {
        "success": true,
        "message": "İşlem başarılı",
        "data": {...}
    }

    Hatalı cevap örneği:

    {
        "success": false,
        "message": "E-posta veya şifre hatalı"
    }

    T generic tiptir.
    Yani data bazen User, bazen [City], bazen Ticket olabilir.
*/
struct APIResponse<T: Decodable>: Decodable {

    let success: Bool
    let message: String
    let data: T?
}
