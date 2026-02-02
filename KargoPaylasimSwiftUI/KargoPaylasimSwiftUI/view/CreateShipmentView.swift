import SwiftUI

struct CreateShipmentView: View {
    let api: APIClient
    let onCreated: () -> Void

    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm: CreateShipmentVM

    @State private var createdInfo: CreateShipmentData?

    init(api: APIClient, onCreated: @escaping () -> Void) {
        self.api = api
        self.onCreated = onCreated
        _vm = StateObject(wrappedValue: CreateShipmentVM(api: api))
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Alıcı Telefon") {
                    TextField("05xx... veya +905xx...", text: $vm.phone)
                        .keyboardType(.phonePad)

                    Button("Kişiyi Bul") {
                        Task { await vm.lookup() }
                    }
                }

                if let t = vm.lookupText {
                    Section {
                        Text(t)
                        HStack {
                            Button("İptal", role: .cancel) { vm.reset() }
                            Spacer()
                            Button("Onayla") {
                                Task {
                                    if let d = await vm.confirmCreate() {
                                        createdInfo = d
                                        onCreated()
                                    }
                                }
                            }
                            .disabled(!vm.canConfirm)
                        }
                    }
                }

                if let err = vm.errorText {
                    Section { Text(err).foregroundStyle(.red) }
                }
            }
            .overlay { if vm.isLoading { ProgressView() } }
            .navigationTitle("Yeni Gönderi")
            .toolbar {
                ToolbarItem {
                    Button("Kapat") { dismiss() }
                }
            }
            .alert("Gönderi Oluşturuldu", isPresented: .constant(createdInfo != nil), presenting: createdInfo) { d in
                Button("Kodu Kopyala") {
                    UIPasteboard.general.string = d.pickup_code
                    dismiss()
                }
                Button("Tamam") { dismiss() }
            } message: { d in
                Text("Kod: \(d.pickup_code)\nSon geçerlilik: \(d.code_expires_at)")
            }
        }
    }
}
