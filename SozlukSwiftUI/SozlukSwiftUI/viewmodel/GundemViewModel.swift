//
//  GundemViewModel.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//

import Foundation
import SwiftUI

@MainActor
final class GundemViewModel: ObservableObject {
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

    /// Backend yoksa: tüm entry'leri çekip id'ye göre desc sıralıyoruz.
    func loadMostCommentedEntriesToday() {
        isLoading = true
        errorMessage = nil
        dao.getAllEntries { [weak self] result in
            guard let self else { return }
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let entries):
                    self.allEntries = entries.sorted { $0.id > $1.id }
                    self.filterEntries()
                case .failure:
                    self.errorMessage = "Bağlantı hatası"
                }
            }
        }
    }
}
