//
//  BugunViewModel.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//
import SwiftUI
import Foundation
@MainActor
final class BugunViewModel: ObservableObject {
    private let dao = SozlukDao.shared

    @Published private(set) var allEntries: [Entry] = []
    @Published var filteredEntries: [Entry] = []
    @Published var searchQuery: String = "" { didSet { filterEntries() } }
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    private func filterEntries() {
        let q = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !q.isEmpty else {
            filteredEntries = allEntries
            return
        }
        filteredEntries = allEntries.filter { $0.title.range(of: q, options: .caseInsensitive) != nil }
    }

    /// created_at ISO ise string karşılaştırması çoğu durumda yeterli; istersen aşağıdaki parse'i kullan.
    func loadTodayEntries() {
        isLoading = true
        errorMessage = nil
        dao.getAllEntries { [weak self] result in
            guard let self else { return }
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let entries):
                    // created_at'a göre yeni -> eski
                    self.allEntries = entries.sorted { lhs, rhs in
                        // İstersen ISO8601 parse et, yoksa string karşılaştırma:
                        lhs.created_at > rhs.created_at
                    }
                    self.filterEntries()
                case .failure:
                    self.errorMessage = "Bağlantı hatası"
                }
            }
        }
    }
}
