//
//  OdaViewScreen.swift
//  MemeShareSwiftUI
//
//  Created by Alperen Saraç on 2.09.2025.
//

import Foundation
import SwiftUI
import PhotosUI
import AVKit
import UniformTypeIdentifiers

struct OdaViewScreen: View {
    let roomId: Int
    let userId: Int

    private let baseURL = URL(string: "https://alperensaracdeneme.com/meme/")!

    @StateObject private var vm = OdaViewModel()
    @State private var items: [GonderiModel] = []
    @State private var toast: Toast? = nil

    // Picker
    @State private var pickerItem: PhotosPickerItem? = nil
    @State private var pickedImage: UIImage? = nil
    @State private var pickedVideoTempURL: URL? = nil
    @State private var isPickedVideo: Bool = false
    @State private var showCaptionSheet = false

    // Görüntüleme
    @State private var presentImage: URL? = nil
    @State private var presentVideo: URL? = nil
    @State private var showImageFull = false
    @State private var showVideoPlayer = false

    // Grid
    private let columns = [GridItem(.flexible()), GridItem(.flexible())]

    var body: some View {
        ScrollView {
            LazyVGrid(columns: columns, spacing: 12) {
                ForEach(items, id: \.self) { it in
                    GonderiCardView(model: it, baseURL: baseURL, onPlayTapped: {
                        presentVideo = baseURL.appendingPathComponent(it.mediaUrl)
                        showVideoPlayer = true
                    })
                    .onTapGesture {
                        if it.mediaType == "image" {
                            presentImage = baseURL.appendingPathComponent(it.mediaUrl)
                            showImageFull = true
                        } else if it.mediaType == "video" {
                            presentVideo = baseURL.appendingPathComponent(it.mediaUrl)
                            showVideoPlayer = true
                        }
                    }
                }
            }
            .padding()
        }
        .navigationTitle("Oda #\(roomId)")
        .toolbar {
            ToolbarItem {
                PhotosPicker(selection: $pickerItem,
                             matching: .any(of: [.images, .videos]),
                             photoLibrary: .shared()) {
                    Label("Meme Ekle", systemImage: "plus.circle.fill")
                }
            }
        }
        .toast($toast)
        .task {
            vm.getAllMedia(roomId: roomId)
        }
        .onReceive(vm.$gonderiler) { list in
            items = list ?? []
        }
        .onReceive(vm.$uploadResult.compactMap { $0 }) { msg in
            toast = Toast(message: msg)
            vm.getAllMedia(roomId: roomId)
        }

        // Seçim → dosyayı çıkar → caption sheet aç
        .onChange(of: pickerItem) { newItem in
            guard let item = newItem else { return }
            Task { await handlePickedItem(item) }
        }

        // Caption sheet
        .sheet(isPresented: $showCaptionSheet) {
            CaptionSheet(
                onCancel: { showCaptionSheet = false; cleanupPicked() },
                onSend: { caption in
                    showCaptionSheet = false
                    Task { await uploadPicked(caption: caption) }
                }
            )
        }

        // Görsel tam ekran
        .fullScreenCover(item: $presentImage, content: { url in
            ImageFullScreen(url: url) { presentImage = nil }
        })

        // Video player
        .sheet(isPresented: $showVideoPlayer) {
            if let url = presentVideo {
                VideoPlayer(player: AVPlayer(url: url))
                    .ignoresSafeArea()
                    .onDisappear { presentVideo = nil }
            }
        }
    }

    // MARK: - Pick & Upload

    private func handlePickedItem(_ item: PhotosPickerItem) async {
        // Video mu?
        if await itemUniformContains(item, ut: UTType.movie) {
            // Geçici dosyaya kopyala (URL al)
            if let temp = await loadFileToTemp(item: item, ut: UTType.movie) {
                pickedVideoTempURL = temp
                isPickedVideo = true
                showCaptionSheet = true
                return
            } else {
                await MainActor.run { toast = Toast(message: "Video alınamadı") }
                cleanupPicked(); return
            }
        }

        // Görsel mi?
        if await itemUniformContains(item, ut: UTType.image) {
            if let data = try? await item.loadTransferable(type: Data.self),
               let img = UIImage(data: data) {
                pickedImage = img
                isPickedVideo = false
                showCaptionSheet = true
                return
            } else {
                await MainActor.run { toast = Toast(message: "Görsel alınamadı") }
                cleanupPicked(); return
            }
        }

        await MainActor.run { toast = Toast(message: "Desteklenmeyen tür") }
        cleanupPicked()
    }

    private func uploadPicked(caption: String) async {
        if isPickedVideo {
            guard let url = pickedVideoTempURL else { return }
            await MainActor.run { toast = Toast(message: "Video yükleniyor…") }
            VideoUploader.uploadVideo(
                videoName: UUID().uuidString,
                fileURL: url,
                roomId: roomId,
                userId: userId,
                caption: caption,
                uploadURL: baseURL.appendingPathComponent("media-upload-video.php")
            ) { success, _ in
                DispatchQueue.main.async {
                    self.toast = Toast(message: success ? "✅ Video yüklendi" : "⚠️ Yükleme başarısız")
                    self.vm.getAllMedia(roomId: self.roomId)
                    self.cleanupPicked()
                }
            }
        } else {
            guard let image = pickedImage else { return }
            await MainActor.run { toast = Toast(message: "Görsel yükleniyor…") }
            vm.uploadImage(image: image, roomId: roomId, userId: userId, caption: caption)
            cleanupPicked()
        }
    }

    private func cleanupPicked() {
        pickedImage = nil
        pickedVideoTempURL = nil
        isPickedVideo = false
        pickerItem = nil
    }

    // MARK: - Helpers (PhotosPickerItem)

    // 1) Tür kontrolü (async olmasına gerek yok ama imzayı bozmayalım)
    private func itemUniformContains(_ item: PhotosPickerItem, ut: UTType) async -> Bool {
        item.supportedContentTypes.contains { $0 == ut || $0.conforms(to: ut) }
    }

    // 2) Seçilen içeriği temp klasöre yaz (image/video için Data üzerinden)
    // Not: Büyük videolarda Data ile bellek yükü olabilir.
    // Daha sağlam çözüm için Transferable + FileRepresentation kullanılır.
    private func loadFileToTemp(item: PhotosPickerItem, ut: UTType) async -> URL? {
        do {
            if let data = try await item.loadTransferable(type: Data.self) {
                let ext = ut.preferredFilenameExtension ?? "dat"
                let dst = FileManager.default.temporaryDirectory
                    .appendingPathComponent("\(UUID().uuidString).\(ext)")
                try? FileManager.default.removeItem(at: dst)
                try data.write(to: dst, options: .atomic)
                return dst
            }
        } catch {
            print("loadFileToTemp hata:", error)
        }
        return nil
    }
}
