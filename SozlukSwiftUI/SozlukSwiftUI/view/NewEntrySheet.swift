//
//  NewEntrySheet.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//

import Foundation
import SwiftUI

struct NewEntrySheet: View {
    @Environment(\.dismiss) private var dismiss

    @State private var title: String = ""
    @State private var content: String = ""
    @State private var isSubmitting = false
    @State private var alertMessage: String? = nil

    var onSuccess: (() -> Void)?

    var body: some View {
        NavigationStack {
            Form {
                Section("Başlık") {
                    TextField("Başlık", text: $title)
                        .textInputAutocapitalization(.sentences)
                }
                Section("İçerik") {
                    TextField("İçerik", text: $content, axis: .vertical)
                        .lineLimit(3, reservesSpace: true)
                        .textInputAutocapitalization(.sentences)
                }
            }
            .navigationTitle("Yeni Entry")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("İptal") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button {
                        submit()
                    } label: {
                        if isSubmitting { ProgressView() } else { Text("Kaydet") }
                    }
                    .disabled(isSubmitting)
                }
            }
            .alert("Uyarı", isPresented: .constant(alertMessage != nil), actions: {
                Button("Tamam") { alertMessage = nil }
            }, message: {
                Text(alertMessage ?? "")
            })
        }
    }

    private func submit() {
        let t = title.trimmingCharacters(in: .whitespacesAndNewlines)
        let c = content.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !t.isEmpty, !c.isEmpty else {
            alertMessage = "Başlık ve içerik boş olamaz"
            return
        }
        let userId = SessionManager.shared.getUserId()
        isSubmitting = true
        SozlukDao.shared.addEntry(userId: userId, title: t, content: c) { res in
            DispatchQueue.main.async {
                isSubmitting = false
                switch res {
                case .success(let r):
                    if r.success {
                        onSuccess?()
                        dismiss()
                    } else {
                        alertMessage = r.message
                    }
                case .failure:
                    alertMessage = "Bağlantı hatası"
                }
            }
        }
    }
}
