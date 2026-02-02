import SwiftUI

struct HomeView: View {
    @StateObject var vm: HomeVM
    let api: APIClient

    enum ActiveSheet: Identifiable {
        case createShipment
        case createAddress
        var id: Int { self == .createShipment ? 1 : 2 }
    }

    enum AddressAction {
        case setDefault(id: Int)
        case delete(id: Int)
    }

    @State private var activeSheet: ActiveSheet?

    var body: some View {
        NavigationView {
            List {
                if let err = vm.errorText {
                    Section { Text(err).foregroundColor(.red) }
                }

                Section(header: Text("Gönderiler")) {
                    ForEach(vm.shipments, id: \.id) { s in
                        NavigationLink(
                            destination: ShipmentDetailView(shipment: s),
                            label: { ShipmentRow(s: s) }
                        )
                    }
                }

                Section(header: Text("Adresler")) {
                    ForEach(vm.addresses, id: \.id) { a in
                        AddressRow(a: a) { action in
                            handleAddressAction(action)
                        }
                    }
                }
            }
            .navigationTitle("Home")
            .toolbar {
                ToolbarItemGroup(placement: .navigationBarTrailing) {
                    Button("+ Adres") { activeSheet = .createAddress }
                    Button("+ Yeni")  { activeSheet = .createShipment }
                }
            }
            .overlay {
                if vm.isLoading { ProgressView() }
            }
            .onAppear {
                vmRefresh()
            }
            .refreshable {
                await vm.refresh()
            }
            .sheet(item: $activeSheet) { sheet in
                switch sheet {
                case .createShipment:
                    CreateShipmentView(api: api) {
                        vmRefresh()
                        activeSheet = nil
                    }
                case .createAddress:
                    AddressCreateView(api: api) {
                        vmRefresh()
                        activeSheet = nil
                    }
                }
            }
        }
    }

    // ✅ Xcode 14 için Task yerine bunu kullanalım
    private func vmRefresh() {
        DispatchQueue.main.async {
            Task { await vm.refresh() }
        }
    }

    // ✅ Xcode 14 için aksiyon handler’ı ayrı fonksiyon
    private func handleAddressAction(_ action: AddressAction) {
        switch action {
        case .setDefault(let id):
            DispatchQueue.main.async {
                Task { await vm.setDefaultAddress(id) }
            }
        case .delete(let id):
            DispatchQueue.main.async {
                Task { await vm.deleteAddress(id) }
            }
        }
    }
}

// MARK: - Rows

struct ShipmentRow: View {
    let s: Shipment
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("ID: #\(s.id) • \(s.status)").font(.headline)
            Text("Kod: \(s.pickup_code)").font(.subheadline).foregroundColor(.secondary)
            if let company = s.cargo_company_name, !company.isEmpty {
                Text("Kargo: \(company)").font(.footnote).foregroundColor(.secondary)
            }
        }
    }
}

struct AddressRow: View {
    let a: Address
    let onAction: (HomeView.AddressAction) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 8) {
                Text(a.title).font(.headline)

                if a.is_default == 1 {
                    Text("Varsayılan")
                        .font(.caption)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Color.green.opacity(0.15))
                        .clipShape(Capsule())
                }

                Spacer()

                if a.is_default != 1 {
                    Button("Varsayılan") {
                        onAction(.setDefault(id: a.id))
                    }
                    .font(.caption)
                }

                Button("Sil") {
                    onAction(.delete(id: a.id))
                }
                .font(.caption)
                .foregroundColor(.red)
            }

            Text("\(a.district) / \(a.city)")
                .font(.subheadline)
                .foregroundColor(.secondary)

            Text(a.address_line)
                .font(.footnote)
                .foregroundColor(.secondary)
                .lineLimit(2)
        }
        .padding(.vertical, 4)
    }
}
