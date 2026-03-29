//
//  JobItem.swift
//  PdfConverterSwift
//
//  Created by Alperen Saraç on 27.03.2026.
//

import Foundation

struct JobItem: Decodable {
    let job_id: Int?
    let job_type: String?
    let status: String?
    let source_file_url: String?
    let result_file_url: String?
    let error_message: String?
    let created_at: String?
}
