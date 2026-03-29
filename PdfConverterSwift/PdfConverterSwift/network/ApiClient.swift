//
//  ApiClient.swift
//  PdfConverterSwift
//
//  Created by Alperen Saraç on 27.03.2026.
//

import Foundation

final class ApiClient {
    static let shared = ApiClient()

    private init() {}

    private let baseURL = "https://alperensaracdeneme.com/pdf/api/"

    func createSingleFileJob(
        userId: Int,
        jobType: String,
        fileURL: URL
    ) async throws -> CreateJobResponse {
        let endpoint = baseURL + "create_job.php"
        guard let url = URL(string: endpoint) else {
            throw URLError(.badURL)
        }

        let fileData = try Data(contentsOf: fileURL)
        let fileName = fileURL.lastPathComponent
        let mimeType = mimeTypeForFileExtension(fileURL.pathExtension)

        let builder = MultipartFormDataBuilder()
        builder.addTextField(named: "job_type", value: jobType)
        builder.addTextField(named: "user_id", value: "\(userId)")
        builder.addFileField(named: "file", fileName: fileName, mimeType: mimeType, fileData: fileData)

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(builder.contentType, forHTTPHeaderField: "Content-Type")
        request.httpBody = builder.build()

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse,
              200...299 ~= httpResponse.statusCode else {
            throw URLError(.badServerResponse)
        }

        return try JSONDecoder().decode(CreateJobResponse.self, from: data)
    }

    func createMultiFileJob(
        userId: Int,
        jobType: String,
        fileURLs: [URL]
    ) async throws -> CreateJobResponse {
        let endpoint = baseURL + "create_job.php"
        guard let url = URL(string: endpoint) else {
            throw URLError(.badURL)
        }

        let builder = MultipartFormDataBuilder()
        builder.addTextField(named: "job_type", value: jobType)
        builder.addTextField(named: "user_id", value: "\(userId)")

        for fileURL in fileURLs {
            let fileData = try Data(contentsOf: fileURL)
            let fileName = fileURL.lastPathComponent
            let mimeType = mimeTypeForFileExtension(fileURL.pathExtension)

            builder.addFileField(
                named: "files[]",
                fileName: fileName,
                mimeType: mimeType,
                fileData: fileData
            )
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(builder.contentType, forHTTPHeaderField: "Content-Type")
        request.httpBody = builder.build()

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse,
              200...299 ~= httpResponse.statusCode else {
            throw URLError(.badServerResponse)
        }

        return try JSONDecoder().decode(CreateJobResponse.self, from: data)
    }

    func getJobStatus(jobId: Int) async throws -> JobStatusResponse {
        let endpoint = baseURL + "job_status.php?job_id=\(jobId)"
        guard let url = URL(string: endpoint) else {
            throw URLError(.badURL)
        }

        let (data, response) = try await URLSession.shared.data(from: url)

        guard let httpResponse = response as? HTTPURLResponse,
              200...299 ~= httpResponse.statusCode else {
            throw URLError(.badServerResponse)
        }

        return try JSONDecoder().decode(JobStatusResponse.self, from: data)
    }

    func listJobs(userId: Int) async throws -> ListJobsResponse {
        let endpoint = baseURL + "list_jobs.php?user_id=\(userId)"
        guard let url = URL(string: endpoint) else {
            throw URLError(.badURL)
        }

        let (data, response) = try await URLSession.shared.data(from: url)

        guard let httpResponse = response as? HTTPURLResponse,
              200...299 ~= httpResponse.statusCode else {
            throw URLError(.badServerResponse)
        }

        return try JSONDecoder().decode(ListJobsResponse.self, from: data)
    }

    private func mimeTypeForFileExtension(_ ext: String) -> String {
        switch ext.lowercased() {
        case "pdf":
            return "application/pdf"
        case "doc":
            return "application/msword"
        case "docx":
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        case "jpg", "jpeg":
            return "image/jpeg"
        case "png":
            return "image/png"
        default:
            return "application/octet-stream"
        }
    }
}
