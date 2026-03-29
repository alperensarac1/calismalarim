//
//  HistoryViewModel.swift
//  PdfConverterSwift
//
//  Created by Alperen Saraç on 27.03.2026.
//

import Foundation

@MainActor
final class HistoryViewModel {

    private let repository = PdfRepository()

    var onStateChanged: (() -> Void)?

    var jobs: [JobItem] = [] {
        didSet { onStateChanged?() }
    }

    var isLoading: Bool = false {
        didSet { onStateChanged?() }
    }

    var errorText: String? {
        didSet { onStateChanged?() }
    }

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
