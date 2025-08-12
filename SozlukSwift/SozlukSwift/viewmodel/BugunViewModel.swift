//
//  BugunViewModel.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import Foundation

final class BugunViewModel {

    private let dao = SozlukDao.shared
    private var allEntries: [Entry] = []

    var onEntriesChange: (([Entry]) -> Void)?
    var onError: ((String) -> Void)?

    private(set) var searchQuery: String = ""

    func setSearchQuery(_ query: String) {
        searchQuery = query
        filterEntries()
    }

    private func filterEntries() {
        let filtered: [Entry]
        if searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            filtered = allEntries
        } else {
            filtered = allEntries.filter { $0.title.range(of: searchQuery, options: .caseInsensitive) != nil }
        }
        DispatchQueue.main.async { self.onEntriesChange?(filtered) }
    }

    func loadTodayEntries() {
        dao.getAllEntries { [weak self] result in
            guard let self else { return }
            switch result {
            case .success(let entries):
                // created_at ISO ise string olarak da sıralanabilir; emin olmak için istersen tarih parse edebilirsin
                self.allEntries = entries.sorted { $0.created_at > $1.created_at }
                self.filterEntries()
            case .failure:
                DispatchQueue.main.async { self.onError?("Bağlantı hatası") }
            }
        }
    }
}
