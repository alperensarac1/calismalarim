//
//  EntryEkleViewModel.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//

import Foundation
import SwiftUI
@MainActor
final class EntryEkleViewModelSwiftUI: ObservableObject {
    private let dao = SozlukDao.shared
    @Published var isSubmitting = false
    @Published var lastResponse: SimpleResponse? = nil

    func addEntry(userId: Int, title: String, content: String) {
        guard !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
              !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            lastResponse = SimpleResponse(success: false, message: "Başlık ve içerik boş olamaz")
            return
        }
        isSubmitting = true
        dao.addEntry(userId: userId, title: title, content: content) { [weak self] result in
            guard let self else { return }
            DispatchQueue.main.async {
                self.isSubmitting = false
                switch result {
                case .success(let resp):
                    self.lastResponse = resp
                case .failure:
                    self.lastResponse = SimpleResponse(success: false, message: "Bağlantı hatası")
                }
            }
        }
    }
}
