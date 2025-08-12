//
//  GundemViewModel.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import Foundation

final class GundemViewModel {

    private let dao = SozlukDao.shared
    private var allEntries: [Entry] = []

    // Output’lar (UI bağlayacağın closure’lar)
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

    /// bugün en çok yorum alanlar için backend yoksa, şimdilik tüm entry’leri çekip id’ye göre sıralıyoruz
    func loadMostCommentedEntriesToday() {
        dao.getAllEntries { [weak self] result in
            guard let self else { return }
            switch result {
            case .success(let entries):
                // örnek: id’ye göre desc (gerekirse created_at’e göre de yapabilirsin)
                self.allEntries = entries.sorted { $0.id > $1.id }
                self.filterEntries()
            case .failure:
                DispatchQueue.main.async { self.onError?("Bağlantı hatası") }
            }
        }
    }
}
