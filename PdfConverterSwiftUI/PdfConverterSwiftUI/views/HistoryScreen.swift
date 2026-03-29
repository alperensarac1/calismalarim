//
//  HistoryScreen.swift
//  PdfConverterSwiftUI
//
//  Created by Alperen Saraç on 26.03.2026.
//

import Foundation
import SwiftUI

struct HistoryScreen: View {
    @StateObject private var viewModel = HistoryViewModel()

    var body: some View {
        Group {
            if viewModel.isLoading {
                VStack(spacing: 12) {
                    ProgressView()
                    Text("Geçmiş işlemler yükleniyor...")
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else if let error = viewModel.errorText, !error.isEmpty {
                VStack(spacing: 12) {
                    Text("Hata")
                        .font(.title3)
                        .bold()

                    Text(error)
                        .multilineTextAlignment(.center)
                        .foregroundStyle(.red)

                    Button("Tekrar Dene") {
                        viewModel.loadJobs()
                    }
                    .buttonStyle(.borderedProminent)
                }
                .padding()

            } else if viewModel.jobs.isEmpty {
                VStack(spacing: 12) {
                    Text("Henüz işlem yok")
                        .font(.title3)
                        .bold()

                    Button("Yenile") {
                        viewModel.loadJobs()
                    }
                    .buttonStyle(.bordered)
                }
                .padding()

            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.jobs) { job in
                            JobRowView(job: job)
                        }
                    }
                    .padding()
                }
            }
        }
        .navigationTitle("Geçmiş İşlemler")
        .onAppear {
            viewModel.loadJobs()
        }
    }
}
