//
//  MainViewModel.swift
//  PdfConverterSwiftUI
//
//  Created by Alperen Saraç on 26.03.2026.
//

import Foundation

@MainActor
final class MainViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var message: String?
    @Published var currentJobId: Int?
    @Published var currentJobStatus: String?
    @Published var resultFileURL: String?
    @Published var errorText: String?

    private let repository = PdfRepository()
    private var pollingTask: Task<Void, Never>?

    let userId = 1

    func createSingleFileJob(jobType: String, fileURL: URL) {
        Task {
            isLoading = true
            message = "Dosya yükleniyor ve job oluşturuluyor..."
            errorText = nil
            resultFileURL = nil

            do {
                let response = try await repository.createSingleFileJob(
                    userId: userId,
                    jobType: jobType,
                    fileURL: fileURL
                )

                isLoading = false

                if response.success, let jobId = response.job_id {
                    currentJobId = jobId
                    message = response.message ?? "Job oluşturuldu"
                    startPolling(jobId: jobId)
                } else {
                    errorText = response.message ?? "Job oluşturulamadı"
                }
            } catch {
                isLoading = false
                errorText = error.localizedDescription
            }
        }
    }

    func createMultiFileJob(jobType: String, fileURLs: [URL]) {
        Task {
            isLoading = true
            message = "Dosyalar yükleniyor ve merge job oluşturuluyor..."
            errorText = nil
            resultFileURL = nil

            do {
                let response = try await repository.createMultiFileJob(
                    userId: userId,
                    jobType: jobType,
                    fileURLs: fileURLs
                )

                isLoading = false

                if response.success, let jobId = response.job_id {
                    currentJobId = jobId
                    message = response.message ?? "Merge job oluşturuldu"
                    startPolling(jobId: jobId)
                } else {
                    errorText = response.message ?? "Job oluşturulamadı"
                }
            } catch {
                isLoading = false
                errorText = error.localizedDescription
            }
        }
    }

    func startPolling(jobId: Int) {
        pollingTask?.cancel()

        pollingTask = Task {
            while !Task.isCancelled {
                do {
                    let statusResponse = try await repository.getJobStatus(jobId: jobId)
                    currentJobId = statusResponse.job_id
                    currentJobStatus = statusResponse.status
                    resultFileURL = statusResponse.result_file_url
                    errorText = statusResponse.error_message
                    message = "Job durumu: \(statusResponse.status ?? "-")"

                    if statusResponse.status == "done" || statusResponse.status == "failed" {
                        break
                    }

                    try await Task.sleep(nanoseconds: 3_000_000_000)
                } catch {
                    errorText = error.localizedDescription
                    break
                }
            }
        }
    }

    func clearState() {
        message = nil
        errorText = nil
    }

    deinit {
        pollingTask?.cancel()
    }
}
