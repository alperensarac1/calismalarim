import SwiftUI

struct EntryDetayView: View {
    let entryId: Int
    @StateObject private var vm = EntryDetayViewModel()

    var body: some View {
        VStack(spacing: 0) {
            if let e = vm.entry {
                VStack(alignment: .leading, spacing: 8) {
                    Text(e.title).font(.title2).bold()
                    HStack {
                        Text(e.username ?? "-")
                        Spacer()
                        Text(e.created_at.asTrDate)
                    }
                    .font(.caption).foregroundStyle(.secondary)
                    Divider()
                    Text(e.content)
                }
                .padding()
            } else if vm.isLoadingEntry {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            }

            List {
                Section("Yorumlar") {
                    ForEach(vm.comments, id: \.id) { c in
                        CommentRowView(comment: c)
                            .swipeActions {
                                Button { vm.vote(commentId: c.id, isLike: true) } label: {
                                    Label("Beğen", systemImage: "hand.thumbsup")
                                }
                                Button(role: .destructive) { vm.vote(commentId: c.id, isLike: false) } label: {
                                    Label("Beğenme", systemImage: "hand.thumbsdown")
                                }
                            }
                    }
                }
            }
            .listStyle(.plain)

            HStack(spacing: 8) {
                TextField("Yorum yaz...", text: Binding(
                    get: { "" }, set: { _ in } // kendi state'inle değiştir
                ))
                .textFieldStyle(.roundedBorder)

                Button("Gönder") {
                    
                }
                .buttonStyle(.borderedProminent)
            }
            .padding(.horizontal).padding(.vertical, 8)
            .background(.thinMaterial)
        }
        .navigationTitle("Entry")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            vm.start(entryId: entryId)
        }
        .alert("Hata",
               isPresented: .constant(vm.errorMessage != nil),
               actions: { Button("Tamam") { vm.errorMessage = nil } },
               message: { Text(vm.errorMessage ?? "") })
        .alert("Bilgi",
               isPresented: .constant(vm.addResponse != nil),
               actions: { Button("Tamam") { vm.addResponse = nil } },
               message: { Text(vm.addResponse?.message ?? "") })
    }
}
