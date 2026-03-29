//
//  PdfRepository.swift
//  PdfConverterSwift
//
//  Created by Alperen Saraç on 27.03.2026.
//

import Foundation

final class PdfRepository {
    private let api = ApiClient.shared

    func createSingleFileJob(
        userId: Int,
        jobType: String,
        fileURL: URL
    ) async throws -> CreateJobResponse {
        try await api.createSingleFileJob(userId: userId, jobType: jobType, fileURL: fileURL)
    }

    func createMultiFileJob(
        userId: Int,
        jobType: String,
        fileURLs: [URL]
    ) async throws -> CreateJobResponse {
        try await api.createMultiFileJob(userId: userId, jobType: jobType, fileURLs: fileURLs)
    }

    func getJobStatus(jobId: Int) async throws -> JobStatusResponse {
        try await api.getJobStatus(jobId: jobId)
    }

    func listJobs(userId: Int) async throws -> ListJobsResponse {
        try await api.listJobs(userId: userId)
    }
}
