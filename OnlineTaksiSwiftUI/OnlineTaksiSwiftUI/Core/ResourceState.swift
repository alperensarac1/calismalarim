//
//  ResourceState.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation

enum ResourceState<T> {
    case idle
    case loading
    case success(T)
    case failure(String)
}
