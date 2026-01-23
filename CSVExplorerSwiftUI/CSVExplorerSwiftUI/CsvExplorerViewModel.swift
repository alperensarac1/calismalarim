
import Foundation
import SwiftUI
import UniformTypeIdentifiers

@MainActor
final class CsvExplorerViewModel: ObservableObject {

    @Published var headers: [String] = []
    @Published var allRows: [CsvRow] = []
    @Published var filteredRows: [CsvRow] = []

    @Published var selectedColumn: String = "ALL_COLUMNS"
    @Published var query: String = ""

    @Published var isLoading: Bool = false
    @Published var infoText: String = "0 records"
    @Published var errorMessage: String? = nil

    @Published var lastPickedFileUrl: URL? = nil

    func clearAll() {
        headers = []
        allRows = []
        filteredRows = []
        selectedColumn = "ALL_COLUMNS"
        query = ""
        lastPickedFileUrl = nil
        infoText = "Database cleared"
        errorMessage = nil
    }

    func importCsv(fromPickedUrl url: URL) {
        isLoading = true
        errorMessage = nil
        infoText = "Importing..."
        lastPickedFileUrl = url

        // Security-scoped read
        let canAccess = url.startAccessingSecurityScopedResource()
        defer { if canAccess { url.stopAccessingSecurityScopedResource() } }

        do {
            let text = try String(contentsOf: url, encoding: .utf8)
            let res = CsvParser.parse(text: text)

            headers = res.headers
            allRows = res.rows
            filteredRows = res.rows
            selectedColumn = "ALL_COLUMNS"
            infoText = "Imported: \(res.rows.count) rows"
        } catch {
            errorMessage = "Import error: \(error.localizedDescription)"
            infoText = "Import failed"
        }

        isLoading = false
    }

    func applyFilter() {
        isLoading = true
        errorMessage = nil
        infoText = "Filtering..."

        let q = query.trimmingCharacters(in: .whitespacesAndNewlines)
        if q.isEmpty {
            filteredRows = allRows
        } else if selectedColumn == "ALL_COLUMNS" {
            filteredRows = allRows.filter { $0.json.localizedCaseInsensitiveContains(q) }
        } else {
            filteredRows = allRows.filter { ($0.dict[selectedColumn] ?? "").localizedCaseInsensitiveContains(q) }
        }

        infoText = "\(filteredRows.count) records (filter: \(selectedColumn))"
        isLoading = false
    }

    func clearFilter() {
        query = ""
        selectedColumn = "ALL_COLUMNS"
        filteredRows = allRows
        infoText = "\(filteredRows.count) records"
        errorMessage = nil
    }

    func uploadAndOpen() async -> URL? {
        guard let url = lastPickedFileUrl else {
            errorMessage = "Önce CSV seçmelisin."
            return nil
        }

        isLoading = true
        errorMessage = nil
        infoText = "Uploading..."

        // security scoped read
        let canAccess = url.startAccessingSecurityScopedResource()
        defer { if canAccess { url.stopAccessingSecurityScopedResource() } }

        do {
            let dl = try await UploadClient.uploadCsv(fileUrl: url)
            infoText = "Upload done."
            isLoading = false
            return dl
        } catch {
            errorMessage = "Upload error: \(error.localizedDescription)"
            infoText = "Upload failed"
            isLoading = false
            return nil
        }
    }
}
