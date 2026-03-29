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

        let mimeType = mimeTypeForFileExtension(fileURL.pathExtension)

        let writer = MultipartFileWriter()
        let multipartFileURL = try writer.createMultipartFile(
            textFields: [
                "job_type": jobType,
                "user_id": "\(userId)"
            ],
            fileFields: [
                (name: "file", fileURL: fileURL, mimeType: mimeType)
            ]
        )

        defer {
            try? FileManager.default.removeItem(at: multipartFileURL)
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(writer.contentType, forHTTPHeaderField: "Content-Type")
        request.timeoutInterval = 300

        let (data, response) = try await URLSession.shared.upload(for: request, fromFile: multipartFileURL)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw URLError(.badServerResponse)
        }

        if !(200...299).contains(httpResponse.statusCode) {
            let serverText = String(data: data, encoding: .utf8) ?? "Sunucu body okunamadı"
            print("HTTP STATUS:", httpResponse.statusCode)
            print("SERVER RESPONSE:", serverText)
            throw NSError(
                domain: "ServerError",
                code: httpResponse.statusCode,
                userInfo: [NSLocalizedDescriptionKey: "HTTP \(httpResponse.statusCode): \(serverText)"]
            )
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

        let writer = MultipartFileWriter()
        let fileFields = fileURLs.map {
            (name: "files[]", fileURL: $0, mimeType: mimeTypeForFileExtension($0.pathExtension))
        }

        let multipartFileURL = try writer.createMultipartFile(
            textFields: [
                "job_type": jobType,
                "user_id": "\(userId)"
            ],
            fileFields: fileFields
        )

        defer {
            try? FileManager.default.removeItem(at: multipartFileURL)
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(writer.contentType, forHTTPHeaderField: "Content-Type")
        request.timeoutInterval = 600

        let (data, response) = try await URLSession.shared.upload(for: request, fromFile: multipartFileURL)

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
