//
//  CreateJobResponse.swift
//  PdfConverterSwift
//
//  Created by Alperen Saraç on 27.03.2026.
//

import Foundation

struct CreateJobResponse: Decodable {
    let success: Bool
    let job_id: Int?
    let message: String?
}
