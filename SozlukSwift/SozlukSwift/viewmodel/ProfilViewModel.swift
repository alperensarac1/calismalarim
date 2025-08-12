//
//  ProfilViewModel.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import Foundation

final class ProfilViewModel {

    private let dao = SozlukDao.shared
    private var allEntries: [Entry] = []

    var onEntriesChange: (([Entry]) -> Void)?
    var onDeleteResult: ((SimpleResponse) -> Void)?
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

    func loadUserEntries(userId: Int) {
        dao.getEntriesByUser(userId: userId) { [weak self] result in
            guard let self else { return }
            switch result {
            case .success(let entries):
                self.allEntries = entries
                self.filterEntries()
            case .failure:
                DispatchQueue.main.async { self.onError?("Bağlantı hatası") }
            }
        }
    }

    func deleteEntry(entryId: Int, userId: Int) {
        dao.deleteEntry(entryId: entryId) { [weak self] result in
            guard let self else { return }
            switch result {
            case .success(let resp):
                DispatchQueue.main.async { self.onDeleteResult?(resp) }
                // Silme sonrası listeyi yenile
                self.loadUserEntries(userId: userId)
            case .failure:
                DispatchQueue.main.async { self.onDeleteResult?(SimpleResponse(success: false, message: "Bağlantı hatası")) }
            }
        }
    }
}
