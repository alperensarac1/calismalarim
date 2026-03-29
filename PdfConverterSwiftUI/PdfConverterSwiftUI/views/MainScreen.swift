//
//  MainScreen.swift
//  PdfConverterSwiftUI
//
//  Created by Alperen Saraç on 26.03.2026.
//

import Foundation
import SwiftUI

struct MainScreen: View {
    @StateObject var viewModel = MainViewModel()

    @State private var showSinglePicker = false
    @State private var showMultiPicker = false
    @State private var pendingJobType: String?

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 14) {
                    Button("JPG to PDF") {
                        pendingJobType = "jpg_to_pdf"
                        showSinglePicker = true
                    }
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity)

                    Button("PDF to Word") {
                        pendingJobType = "pdf_to_word"
                        showSinglePicker = true
                    }
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity)

                    Button("Word to PDF") {
                        pendingJobType = "word_to_pdf"
                        showSinglePicker = true
                    }
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity)

                    Button("PDF Birleştir") {
                        showMultiPicker = true
                    }
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity)

                    NavigationLink("Geçmiş İşlemler") {
                        HistoryScreen()
                    }
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity)

                    Divider().padding(.vertical, 8)

                    VStack(alignment: .leading, spacing: 10) {
                        Text("Durum")
                            .font(.headline)

                        if viewModel.isLoading ||
                            viewModel.currentJobStatus == "waiting" ||
                            viewModel.currentJobStatus == "processing" {
                            ProgressView()
                        }

                        Text(statusText)
                            .frame(maxWidth: .infinity, alignment: .leading)

                        if let urlString = viewModel.resultFileURL,
                           let url = URL(string: urlString) {
                            Link("Sonucu Aç", destination: url)
                                .buttonStyle(.borderedProminent)
                        }
                    }
                    .padding()
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                .padding()
            }
            .navigationTitle("PDF Dönüştürücü")
        }
        .sheet(isPresented: $showSinglePicker) {
            FilePicker(allowsMultipleSelection: false) { urls in
                guard let url = urls.first, let jobType = pendingJobType else { return }
                viewModel.createSingleFileJob(jobType: jobType, fileURL: url)
            }
        }
        .sheet(isPresented: $showMultiPicker) {
            FilePicker(allowsMultipleSelection: true) { urls in
                guard !urls.isEmpty else { return }
                viewModel.createMultiFileJob(jobType: "pdf_merge", fileURLs: urls)
            }
        }
    }

    private var statusText: String {
        if let error = viewModel.errorText, !error.isEmpty {
            return "Hata: \(error)"
        }

        if let status = viewModel.currentJobStatus {
            switch status {
            case "waiting":
                return "İş sıraya alındı, worker bekleniyor..."
            case "processing":
                return "Dönüştürme işlemi devam ediyor..."
            case "done":
                return "İşlem tamamlandı. Sonuç hazır."
            case "failed":
                return "İşlem başarısız."
            default:
                return "Durum: \(status)"
            }
        }

        return viewModel.message ?? "Hazır"
    }
}
