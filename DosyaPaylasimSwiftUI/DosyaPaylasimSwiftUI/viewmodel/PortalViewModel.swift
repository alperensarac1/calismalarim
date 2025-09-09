//
//  PortalViewModel.swift
//  DosyaPaylasimSwiftUI
//
//  Created by Alperen Saraç on 8.09.2025.
//

import Foundation
import SwiftUI

@MainActor
final class PortalViewModel: ObservableObject {
    // UI state
    @Published var selectedFileURL: URL?
    @Published var selectedFileName: String = "(yok)"
    @Published var selectedFileSize: Int64 = 0

    @Published var isUploading = false
    @Published var uploadMessage: String = ""
    @Published var lastDownloadURL: String?

    @Published var code: String = ""
    @Published var checkMessage: String = ""

    private let api = ApiService.shared  // önceki ApiService.swift

    func setPickedFile(_ url: URL) {
        selectedFileURL = url
        selectedFileName = url.lastPathComponent
        if let sz = (try? FileManager.default.attributesOfItem(atPath: url.path)[.size]) as? NSNumber {
            selectedFileSize = sz.int64Value
        } else {
            selectedFileSize = 0
        }
    }

    func upload() {
        guard let url = selectedFileURL else {
            uploadMessage = "Önce dosya seçin"
            return
        }
        uploadMessage = "Yükleniyor…"
        isUploading = true

        api.uploadFile(fileURL: url) { [weak self] result in
            guard let self = self else { return }
            Task { @MainActor in
                self.isUploading = false
                switch result {
                case .success(let r):
                    if r.ok == true {
                        self.lastDownloadURL = r.downloadUrl
                        self.uploadMessage =
                            """
                            Yüklendi!
                            Kod: \(r.code ?? "-")
                            İndirme: \(r.downloadUrl ?? "-")
                            Bilgi: \(r.infoUrl ?? "-")
                            Geçerlilik: \(r.expiresAt ?? "-")
                            """
                        self.code = r.code ?? ""
                    } else {
                        self.uploadMessage = "Hata: \(r.error ?? "Bilinmeyen")"
                    }
                case .failure(let e):
                    self.uploadMessage = "İstek hatası: \(e.localizedDescription)"
                }
            }
        }
    }

    func checkCode() {
        let trimmed = code.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        guard trimmed.range(of: #"^[A-Z0-9]{6}$"#, options: .regularExpression) != nil else {
            checkMessage = "Kod 6 haneli olmalı"
            return
        }
        checkMessage = "Sorgulanıyor…"

        api.getLink(code: trimmed) { [weak self] result in
            guard let self = self else { return }
            Task { @MainActor in
                switch result {
                case .success(let r):
                    if r.ok == true {
                        if r.expired == true {
                            self.checkMessage = "Kod: \(r.code ?? "-") — Süresi dolmuş veya pasif."
                        } else {
                            self.checkMessage =
                                """
                                Kod: \(r.code ?? "-")
                                Dosya: \(r.originalName ?? "-")
                                Boyut: \(r.sizeBytes ?? 0)
                                Son Kullanım: \(r.expiresAt ?? "-")
                                Link: \(r.downloadUrl ?? "-")
                                """
                            self.lastDownloadURL = r.downloadUrl
                        }
                    } else {
                        self.checkMessage = "Hata: \(r.error ?? "Bilinmeyen")"
                    }
                case .failure(let e):
                    self.checkMessage = "İstek hatası: \(e.localizedDescription)"
                }
            }
        }
    }
}
