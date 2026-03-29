//
//  JobsRowView.swift
//  PdfConverterSwiftUI
//
//  Created by Alperen Saraç on 26.03.2026.
//

import Foundation
import SwiftUI

struct JobRowView: View {
    let job: JobItem
    @StateObject private var downloadHelper = DownloadHelper()

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("İşlem: \(mapJobType(job.job_type))")
                .font(.headline)

            Text("Durum: \(mapStatus(job.status))")
                .font(.subheadline)

            Text("Tarih: \(job.created_at ?? "-")")
                .font(.footnote)
                .foregroundStyle(.secondary)

            if let error = job.error_message, !error.isEmpty {
                Text("Hata: \(error)")
                    .foregroundStyle(.red)
                    .font(.footnote)
            }

            if let resultURL = job.result_file_url, !resultURL.isEmpty {
                HStack(spacing: 10) {
                    if let url = URL(string: resultURL) {
                        Link("Sonucu Aç", destination: url)
                            .buttonStyle(.borderedProminent)
                    }

                    Button("İndir") {
                        downloadHelper.downloadFile(from: resultURL)
                    }
                    .buttonStyle(.bordered)
                    .disabled(downloadHelper.isDownloading)
                }

                if downloadHelper.isDownloading {
                    VStack(alignment: .leading, spacing: 6) {
                        ProgressView(value: downloadHelper.progress)
                        Text("\(Int(downloadHelper.progress * 100))%")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }

                if let downloadedURL = downloadHelper.downloadedFileURL {
                    ShareLink(item: downloadedURL) {
                        Text("İndirilen Dosyayı Paylaş")
                    }
                    .buttonStyle(.bordered)
                }

                if let downloadError = downloadHelper.errorText, !downloadError.isEmpty {
                    Text("İndirme Hatası: \(downloadError)")
                        .foregroundStyle(.red)
                        .font(.caption)
                }
            }
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private func mapJobType(_ jobType: String?) -> String {
        switch jobType {
        case "jpg_to_pdf":
            return "JPG to PDF"
        case "pdf_to_word":
            return "PDF to Word"
        case "word_to_pdf":
            return "Word to PDF"
        case "pdf_merge":
            return "PDF Birleştirme"
        default:
            return jobType ?? "-"
        }
    }

    private func mapStatus(_ status: String?) -> String {
        switch status {
        case "waiting":
            return "Bekliyor"
        case "processing":
            return "İşleniyor"
        case "done":
            return "Tamamlandı"
        case "failed":
            return "Başarısız"
        default:
            return status ?? "-"
        }
    }
}
