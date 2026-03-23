//
//  FilterOptions.swift
//  WebTrackerSwift
//
//  Created by Alperen Saraç on 22.03.2026.
//

import Foundation

struct FilterOptions {
    var enableFilter: Bool = true
    var onlyApiRequests: Bool = false
    var enableJsHook: Bool = true
    var showOnlyGet: Bool = false
    var showOnlyPost: Bool = false
    var searchQuery: String = ""
}
