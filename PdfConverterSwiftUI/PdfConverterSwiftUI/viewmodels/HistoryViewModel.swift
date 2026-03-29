//
//  HistoryViewModel.swift
//  PdfConverterSwiftUI
//
//  Created by Alperen Saraç on 26.03.2026.
//

import Foundation

@MainActor
final class HistoryViewModel: ObservableObject {
    @Published var jobs: [JobItem] = []
    @Published var isLoading = false
    @Published var errorText: String?

    private let repository = PdfRepository()
    let userId = 1

    func loadJobs() {
        Task {
            isLoading = true
            errorText = nil

            do {
                let response = try await repository.listJobs(userId: userId)
                isLoading = false

                if response.success {
                    jobs = response.jobs ?? []
                } else {
                    jobs = []
                }
            } catch {
                isLoading = false
                jobs = []
                errorText = error.localizedDescription
            }
        }
    }
}
