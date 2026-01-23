import SwiftUI
import UniformTypeIdentifiers

struct ContentView: View {

    @StateObject private var vm = CsvExplorerViewModel()

    @State private var showImporter = false

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {

                // Buttons
                WrapButtons(vm: vm, showImporter: $showImporter)

                Text(vm.infoText)
                    .font(.headline)
                    .frame(maxWidth: .infinity, alignment: .leading)

                if let err = vm.errorMessage {
                    Text(err)
                        .foregroundStyle(.red)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }

                // Column picker
                Picker("Column", selection: $vm.selectedColumn) {
                    Text("ALL_COLUMNS").tag("ALL_COLUMNS")
                    ForEach(vm.headers, id: \.self) { h in
                        Text(h).tag(h)
                    }
                }
                .pickerStyle(.menu)

                // Search
                TextField("Search", text: $vm.query)
                    .textFieldStyle(.roundedBorder)

                if vm.isLoading {
                    ProgressView().frame(maxWidth: .infinity, alignment: .leading)
                }

                // List
                List(vm.filteredRows) { row in
                    NavigationLink {
                        DetailsView(rowJson: row.json, headers: vm.headers)
                    } label: {
                        RowCard(row: row)
                    }
                }
                .listStyle(.plain)
            }
            .padding(14)
            .navigationTitle("CSV Explorer")
        }
        .fileImporter(
            isPresented: $showImporter,
            allowedContentTypes: [.commaSeparatedText, .plainText, .text],
            allowsMultipleSelection: false
        ) { result in
            switch result {
            case .success(let urls):
                if let url = urls.first {
                    vm.importCsv(fromPickedUrl: url)
                }
            case .failure(let err):
                vm.errorMessage = "Pick error: \(err.localizedDescription)"
            }
        }
    }
}

private struct WrapButtons: View {
    @ObservedObject var vm: CsvExplorerViewModel
    @Binding var showImporter: Bool
    @Environment(\.openURL) private var openURL

    var body: some View {
        // Basit wrap hissi için adaptive grid
        let cols = [GridItem(.adaptive(minimum: 120), spacing: 10)]
        LazyVGrid(columns: cols, spacing: 10) {
            Button("Select CSV") { showImporter = true }
                .buttonStyle(.borderedProminent)
                .disabled(vm.isLoading)

            Button("Get .xls") {
                Task {
                    if let url = await vm.uploadAndOpen() {
                        openURL(url)
                    }
                }
            }
            .buttonStyle(.bordered)
            .disabled(vm.isLoading || vm.lastPickedFileUrl == nil)

            Button("Filter") { vm.applyFilter() }
                .buttonStyle(.bordered)
                .disabled(vm.isLoading)

            Button("Clear") { vm.clearFilter() }
                .buttonStyle(.bordered)
                .disabled(vm.isLoading)

            Button("Clear DB") { vm.clearAll() }
                .buttonStyle(.borderless)
                .disabled(vm.isLoading)
        }
    }
}

private struct RowCard: View {
    let row: CsvRow

    var body: some View {
        let title = buildTitle(row.dict)
        let subtitle = buildSubtitle(row.dict)

        VStack(alignment: .leading, spacing: 6) {
            Text(title).font(.headline)
            Text(subtitle).font(.subheadline).foregroundStyle(.secondary)
        }
        .padding(.vertical, 6)
    }

    private func buildTitle(_ d: [String: String]) -> String {
        let id = d["id"] ?? ""
        let first = d["first_name"] ?? d["firstname"] ?? ""
        let last  = d["last_name"] ?? d["lastname"] ?? ""

        if !id.isEmpty, (!first.isEmpty || !last.isEmpty) {
            return "#\(id)  \((first + " " + last).trimmingCharacters(in: .whitespaces))"
        } else if !id.isEmpty {
            return "#\(id)"
        } else if !first.isEmpty || !last.isEmpty {
            return (first + " " + last).trimmingCharacters(in: .whitespaces)
        }
        return "Row"
    }

    private func buildSubtitle(_ d: [String: String]) -> String {
        let lastSeen = d["last_seen"] ?? ""
        let country = d["country_title"] ?? ""
        let city = d["city_title"] ?? ""

        var parts: [String] = []
        if !lastSeen.isEmpty { parts.append("Last seen: \(lastSeen)") }
        let loc = [country, city].filter { !$0.isEmpty }.joined(separator: " / ")
        if !loc.isEmpty { parts.append(loc) }

        return parts.isEmpty ? "Tap to view details" : parts.joined(separator: " • ")
    }
}
