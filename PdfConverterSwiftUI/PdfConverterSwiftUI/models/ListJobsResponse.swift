//
//  ListJobsResponse.swift
//  PdfConverterSwiftUI
//
//  Created by Alperen Saraç on 26.03.2026.
//

import Foundation
struct ListJobsResponse: Decodable {
    let success: Bool
    let jobs: [JobItem]?
}
