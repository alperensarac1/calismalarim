//
//  ListJobsResponse.swift
//  PdfConverterSwift
//
//  Created by Alperen Saraç on 27.03.2026.
//

import Foundation


struct ListJobsResponse: Decodable {
    let success: Bool
    let jobs: [JobItem]?
}
