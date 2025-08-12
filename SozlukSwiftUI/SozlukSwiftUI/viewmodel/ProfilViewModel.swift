import Foundation
import SwiftUI
@MainActor
final class ProfilViewModel: ObservableObject {
    private let dao = SozlukDao.shared

    @Published private(set) var allEntries: [Entry] = []
    @Published var filteredEntries: [Entry] = []
    @Published var searchQuery: String = "" { didSet { filterEntries() } }
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil
    @Published var lastDeleteResult: SimpleResponse? = nil

    private func filterEntries() {
        let q = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !q.isEmpty else {
            filteredEntries = allEntries
            return
        }
        filteredEntries = allEntries.filter { $0.title.range(of: q, options: .caseInsensitive) != nil }
    }

    func loadUserEntries(userId: Int) {
        isLoading = true
        errorMessage = nil
        dao.getEntriesByUser(userId: userId) { [weak self] result in
            guard let self else { return }
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let entries):
                    self.allEntries = entries
                    self.filterEntries()
                case .failure:
                    self.errorMessage = "Bağlantı hatası"
                }
            }
        }
    }

    func deleteEntry(entryId: Int, userId: Int) {
        isLoading = true
        errorMessage = nil
        dao.deleteEntry(entryId: entryId) { [weak self] result in
            guard let self else { return }
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let resp):
                    self.lastDeleteResult = resp
                    // Silme sonrası listeyi yenile
                    self.loadUserEntries(userId: userId)
                case .failure:
                    self.lastDeleteResult = SimpleResponse(success: false, message: "Bağlantı hatası")
                }
            }
        }
    }
}
