//
//  EntryDetayViewModel.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//

import Foundation
import SwiftUI

@MainActor
final class EntryDetayViewModel: ObservableObject {
    private let dao = SozlukDao.shared

    @Published private(set) var entryId: Int = -1
    @Published var entry: Entry? = nil
    @Published var comments: [Comment] = []
    @Published var isLoadingEntry = false
    @Published var isLoadingComments = false
    @Published var errorMessage: String? = nil
    @Published var addResponse: SimpleResponse? = nil

    func start(entryId: Int) {
        self.entryId = entryId
        loadEntry()
        loadComments()
    }

    func loadEntry() {
        guard entryId > 0 else { return }
        isLoadingEntry = true
        errorMessage = nil

        dao.getEntryById(entryId: entryId) { [weak self] result in
            guard let self else { return }
            DispatchQueue.main.async {
                self.isLoadingEntry = false
                switch result {
                case .success(let entryOpt):
                    self.entry = entryOpt
                    if entryOpt == nil {
                        self.errorMessage = "Entry yüklenemedi"
                    }
                case .failure:
                    self.errorMessage = "Entry yüklenemedi"
                }
            }
        }
    }

    func loadComments() {
        guard entryId > 0 else { return }
        isLoadingComments = true
        errorMessage = nil

        dao.getCommentsByEntry(entryId: entryId) { [weak self] result in
            guard let self else { return }
            DispatchQueue.main.async {
                self.isLoadingComments = false
                switch result {
                case .success(let list):
                    self.comments = list
                case .failure:
                    self.errorMessage = "Yorumlar yüklenemedi"
                }
            }
        }
    }

    func vote(commentId: Int, isLike: Bool) {
        let userId = SessionManager.shared.getUserId()
        dao.voteComment(commentId: commentId, userId: userId, isLike: isLike) { [weak self] _ in
            DispatchQueue.main.async { self?.loadComments() }
        }
    }

    func addComment(text: String) {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard entryId > 0, !trimmed.isEmpty else { return }

        let userId = SessionManager.shared.getUserId()
        dao.addComment(entryId: entryId, userId: userId, commentText: trimmed) { [weak self] result in
            guard let self else { return }
            DispatchQueue.main.async {
                switch result {
                case .success(let resp):
                    self.addResponse = resp
                    if resp.success { self.loadComments() }
                case .failure:
                    self.addResponse = SimpleResponse(success: false, message: "Bağlantı hatası")
                }
            }
        }
    }
}
