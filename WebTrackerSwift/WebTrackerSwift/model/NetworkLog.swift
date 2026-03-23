//
//  NetworkLog.swift
//  WebTrackerSwift
//
//  Created by Alperen Saraç on 22.03.2026.
//

import Foundation

struct NetworkLog: Equatable {
    let method: String
    let url: String
    let host: String
    let time: String
    let headers: [String: String]
    let isMainFrame: Bool
    let resourceType: String
    let requestBody: String?
    let source: String
}
