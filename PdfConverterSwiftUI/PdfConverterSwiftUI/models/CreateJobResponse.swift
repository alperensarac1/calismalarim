//
//  CreateJobResponse.swift
//  PdfConverterSwiftUI
//
//  Created by Alperen Saraç on 26.03.2026.
//

import Foundation
struct CreateJobResponse: Decodable {
    let success: Bool
    let job_id: Int?
    let message: String?
}
