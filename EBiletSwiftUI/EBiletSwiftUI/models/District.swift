//
//  District.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation

/*
    District

    İlçe modelidir.

    Önemli not:
    Bazı endpointlerde district şöyle döner:

    {
        "id": 1,
        "city_id": 1,
        "name": "Kadıköy"
    }

    Ama event_detail.php içinde bazen şöyle döner:

    {
        "id": 1,
        "name": "Kadıköy"
    }

    Yani city_id her zaman gelmeyebilir.
    Bu yüzden cityId alanını optional yaptık.
*/
struct District: Codable, Identifiable, Hashable {

    let id: Int
    let cityId: Int?
    let name: String

    enum CodingKeys: String, CodingKey {
        case id
        case cityId = "city_id"
        case name
    }
}
