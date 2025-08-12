//
//  EntryDetayViewModel.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import Foundation

final class EntryDetayViewModel {
    private let dao = SozlukDao.shared

      var onEntryChange: ((Entry) -> Void)?
      var onCommentsChange: (([Comment]) -> Void)?
      var onError: ((String) -> Void)?
      var onAddCommentResult: ((Bool, String) -> Void)?
    private(set) var entryId: Int = -1

    private var comments: [Comment] = []

    func start(entryId: Int) {
        self.entryId = entryId
        loadEntry()
        loadComments()
    }

    func loadEntry() {
        guard entryId > 0 else { return }
        dao.getEntryById(entryId: entryId) { [weak self] result in
            switch result {
            case .success(let entryOpt):
                if let e = entryOpt {
                    DispatchQueue.main.async { self?.onEntryChange?(e) }
                }
            case .failure:
                DispatchQueue.main.async { self?.onError?("Entry yüklenemedi") }
            }
        }
    }

    func loadComments() {
        guard entryId > 0 else { return }
        dao.getCommentsByEntry(entryId: entryId) { [weak self] result in
            switch result {
            case .success(let list):
                self?.comments = list
                DispatchQueue.main.async { self?.onCommentsChange?(list) }
            case .failure:
                DispatchQueue.main.async { self?.onError?("Yorumlar yüklenemedi") }
            }
        }
    }

    func vote(commentId: Int, userId: Int, isLike: Bool) {
        dao.voteComment(commentId: commentId, userId: userId, isLike: isLike) { [weak self] _ in
            // Sonuç ne olursa olsun listeyi yenileyelim (isteğe göre kontrol ekleyebilirsin)
            self?.loadComments()
        }
    }
    func addComment(userId: Int, text: String) {
         guard entryId > 0, !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
         dao.addComment(entryId: entryId, userId: userId, commentText: text) { [weak self] result in
             switch result {
             case .success(let resp):
                 DispatchQueue.main.async {
                     self?.onAddCommentResult?(resp.success, resp.message!)
                 }
                 if resp.success { self?.loadComments() }
             case .failure:
                 DispatchQueue.main.async {
                     self?.onAddCommentResult?(false, "Bağlantı hatası")
                 }
             }
         }
     }
}
