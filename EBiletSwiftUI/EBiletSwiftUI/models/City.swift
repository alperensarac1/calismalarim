//
//  City.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation

/*
    City

    Şehir modelidir.

    Identifiable:
    SwiftUI Picker/List içinde daha rahat kullanmak için.
*/
struct City: Codable, Identifiable, Hashable {
    let id: Int
    let name: String
}
