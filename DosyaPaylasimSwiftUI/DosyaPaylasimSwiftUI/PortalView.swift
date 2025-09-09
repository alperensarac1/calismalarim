//
//  PortalView.swift
//  DosyaPaylasimSwiftUI
//
//  Created by Alperen Saraç on 8.09.2025.
//

import Foundation
import SwiftUI

struct PortalView: View {
    @StateObject private var vm = PortalViewModel()
    @State private var showFileImporter = false
    @Environment(\.openURL) private var openURL

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 16) {
                    Text("Dosya Yükleme & Kodla İndirme")
                        .font(.title2).bold()
                        .frame(maxWidth: .infinity, alignment: .leading)

                    // --- 1) Dosya Yükle Kartı ---
                    GroupBox(label: Text("1) Dosya Yükle").font(.headline)) {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Seçili dosya: \(vm.selectedFileName) — \(vm.selectedFileSize) bayt")
                                .font(.subheadline)

                            HStack(spacing: 12) {
                                Button("Dosya Seç") { showFileImporter = true }
                                    .buttonStyle(.borderedProminent)

                                Button("Yükle") { vm.upload() }
                                    .buttonStyle(.bordered)
                                    .disabled(vm.selectedFileURL == nil || vm.isUploading)
                            }

                            if vm.isUploading {
                                ProgressView().progressViewStyle(.linear)
                            }

                            if !vm.uploadMessage.isEmpty {
                                Text(vm.uploadMessage)
                                    .font(.footnote)
                                    .foregroundStyle(.primary)
                            }

                            HStack(spacing: 12) {
                                Button("İndirme Linkini Kopyala") {
                                    if let url = vm.lastDownloadURL, !url.isEmpty {
                                        UIPasteboard.general.string = url
                                    }
                                }
                                .buttonStyle(.bordered)
                                .disabled(vm.lastDownloadURL == nil)

                                Button("Linki Aç") {
                                    if let s = vm.lastDownloadURL, let u = URL(string: s) {
                                        openURL(u)
                                    }
                                }
                                .buttonStyle(.bordered)
                                .disabled(vm.lastDownloadURL == nil)
                            }

                            Text("Yüklenen dosyalar 14 gün sonra otomatik silinir.")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }

                    // --- 2) Kodu Gir & İndir Kartı ---
                    GroupBox(label: Text("2) Kodu Gir & İndir").font(.headline)) {
                        VStack(alignment: .leading, spacing: 12) {

                            TextField("6 Haneli Kod (örn: ABC123)", text: $vm.code)
                                .textInputAutocapitalization(.characters)
                                .autocorrectionDisabled()
                                .onChange(of: vm.code) { new in
                                    vm.code = new.uppercased().prefix(6).description
                                }

                            HStack(spacing: 12) {
                                Button("Linki Kontrol Et") { vm.checkCode() }
                                    .buttonStyle(.borderedProminent)

                                Button("İndir") {
                                    let trimmed = vm.code.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
                                    guard trimmed.range(of: #"^[A-Z0-9]{6}$"#, options: .regularExpression) != nil else {
                                        vm.checkMessage = "Kod 6 haneli olmalı"
                                        return
                                    }
                                    let urlStr = "https://alperensaracdeneme.com/api/download.php?code=\(trimmed)"
                                    if let u = URL(string: urlStr) { openURL(u) }
                                }
                                .buttonStyle(.bordered)
                            }

                            if !vm.checkMessage.isEmpty {
                                Text(vm.checkMessage)
                                    .font(.footnote)
                                    .foregroundStyle(.primary)
                            }
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }
                .padding(16)
            }
            .navigationTitle("Paylaşım Portalı")
        }
        .fileImporter(isPresented: $showFileImporter, allowedContentTypes: [.data], allowsMultipleSelection: false) { result in
            switch result {
            case .success(let urls):
                if let url = urls.first {
                    // security-scoped erişim
                    let needs = url.startAccessingSecurityScopedResource()
                    vm.setPickedFile(url)
                    if needs { url.stopAccessingSecurityScopedResource() }
                }
            case .failure(let err):
                vm.uploadMessage = "Seçim hatası: \(err.localizedDescription)"
            }
        }
    }
}
