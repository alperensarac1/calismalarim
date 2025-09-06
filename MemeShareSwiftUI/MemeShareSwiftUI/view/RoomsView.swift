import SwiftUI
import Combine
import AVKit

// Net bir route enum'u
private enum Route: Hashable {
    case room(id: Int)
}

struct RoomsView: View {
    let userId: Int

    @StateObject private var vm = OdaViewModel()
    @State private var rooms: [OdaModel] = []
    @State private var toast: Toast? = nil
    @State private var showJoinSheet = false
    @State private var isLoading = false

    // NavigationStack path'i
    @State private var path = NavigationPath()

    var body: some View {
        NavigationStack(path: $path) {
            content
                .navigationTitle("Odalar")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItemGroup {
                        Button {
                            vm.createRoom(userId: userId)
                        } label: {
                            Label("Oda Oluştur", systemImage: "plus.circle.fill")
                        }

                        Button {
                            showJoinSheet = true
                        } label: {
                            Label("Odaya Katıl", systemImage: "rectangle.and.pencil.and.ellipsis")
                        }
                    }
                }
                .toast($toast)

                // userId değişirse tekrar çek
                .task(id: userId) { await fetchOdalar() }

                // Oda oluşturma sonucunu dinle
                .onReceive(vm.$odaOlusturmaSonucu.compactMap { $0 }) { res in
                    if res.success, let roomId = res.roomId, let code = res.roomCode {
                        let yeni = OdaModel(odaId: roomId, roomCode: code, createdBy: userId)
                        withAnimation {
                            rooms.append(yeni)
                            toast = Toast(message: "Oda oluşturuldu: \(code)")
                        }
                    } else {
                        withAnimation {
                            toast = Toast(message: "Hata: \(res.message ?? "Bilinmeyen hata")")
                        }
                    }
                }

                // Katıl modalı
                .sheet(isPresented: $showJoinSheet) {
                    JoinRoomSheet(
                        onJoin: { code in
                            showJoinSheet = false
                            Task { await joinRoom(code: code) }
                        },
                        onCancel: { showJoinSheet = false }
                    )
                }

                // Navigation
                .navigationDestination(for: Route.self) { route in
                    switch route {
                    case .room(let id):
                        OdaViewScreen(roomId: id, userId: userId)
                    }
                }
        }
    }

    // MARK: - İçerik

    @ViewBuilder
    private var content: some View {
        if isLoading && rooms.isEmpty {
            ProgressView("Yükleniyor…")
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if rooms.isEmpty {
            EmptyStateView()
                .overlay(alignment: .bottom) {
                    // Teşhis için
                    Text("userId=\(userId) • rooms=\(rooms.count)")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .padding(.bottom, 8)
                }
        } else {
            List {
                Section(footer:
                    Text("Toplam oda: \(rooms.count)")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                ) {
                    ForEach(rooms) { oda in
                        Button {
                            path.append(Route.room(id: oda.odaId))
                        } label: {
                            OdaRowView(oda: oda)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .listStyle(.plain)
            .refreshable { await fetchOdalar() }
        }
    }

    // MARK: - Actions

    private func fetchOdalar() async {
        await MainActor.run { isLoading = true }
        defer { Task { await MainActor.run { isLoading = false } } }

        do {
            let list = try await APIService.shared.getJoinedRooms(userId: userId)
            await MainActor.run { rooms = list }
        } catch let apiErr as APIError {
            await MainActor.run {
                switch apiErr {
                case .server(let status):
                    toast = Toast(message: "Sunucu hatası (\(status))")
                case .decodeFailed:
                    toast = Toast(message: "Yanıt çözümlenemedi (decode)")
                default:
                    toast = Toast(message: "Bağlantı hatası: \(apiErr.localizedDescription)")
                }
            }
        } catch {
            await MainActor.run { toast = Toast(message: "Hata: \(error.localizedDescription)") }
        }
    }

    private func joinRoom(code: String) async {
        guard !code.isEmpty else {
            await MainActor.run { toast = Toast(message: "Kod boş olamaz") }
            return
        }
        do {
            let res = try await APIService.shared.joinRoom(userId: userId, roomCode: code)
            if res.success {
                await MainActor.run { toast = Toast(message: "Odaya katıldınız") }
                await fetchOdalar()
            } else {
                await MainActor.run { toast = Toast(message: "Katılım başarısız") }
            }
        } catch {
            await MainActor.run { toast = Toast(message: "Hata: \(error.localizedDescription)") }
        }
    }
}

// Basit boş durum
private struct EmptyStateView: View {
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: "rectangle.on.rectangle.slash")
                .font(.system(size: 40))
                .foregroundStyle(.secondary)
            Text("Henüz oda yok")
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
