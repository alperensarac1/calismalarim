//
//  District.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 25.06.2026.
//

import Foundation

/*
    District

    İlçe modelidir.

    Backend:
    city_id
*/
struct District: Codable {

    let id: Int
    let cityId: Int
    let name: String

    enum CodingKeys: String, CodingKey {
        case id
        case cityId = "city_id"
        case name
    }
}
