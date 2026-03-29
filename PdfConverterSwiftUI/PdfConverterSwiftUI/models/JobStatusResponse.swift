//
//  JobStatusResponse.swift
//  PdfConverterSwiftUI
//
//  Created by Alperen Saraç on 26.03.2026.
//

import Foundation
struct JobStatusResponse: Decodable {
    let success: Bool
    let job_id: Int?
    let job_type: String?
    let status: String?
    let error_message: String?
    let created_at: String?
    let updated_at: String?
    let source_file_url: String?
    let result_file_url: String?
}
