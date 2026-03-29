//
//  JobItem.swift
//  PdfConverterSwiftUI
//
//  Created by Alperen Saraç on 26.03.2026.
//

import Foundation
struct JobItem: Decodable, Identifiable {
    let job_id: Int?
    let job_type: String?
    let status: String?
    let source_file_url: String?
    let result_file_url: String?
    let error_message: String?
    let created_at: String?

    var id: Int {
        job_id ?? -1
    }
}
