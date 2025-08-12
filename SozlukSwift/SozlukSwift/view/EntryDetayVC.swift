//
//  EntryDetayVC.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import UIKit

class EntryDetayVC: UIViewController {

    @IBOutlet weak var tfYorum: UITextField!
    @IBOutlet weak var tabVYorumlar: UITableView!
    @IBOutlet weak var tvEntryTarih: UILabel!
    @IBOutlet weak var tvEntryYazanKisi: UILabel!
    @IBOutlet weak var tvEntryBaslik: UILabel!
    
    
        var entryId: Int = -1

        private let viewModel = EntryDetayViewModel()
        private var yorumlar: [Comment] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        if entryId > 0 { viewModel.start(entryId: entryId) }

        // Do any additional setup after loading the view.
        tabVYorumlar.dataSource = self
                tabVYorumlar.delegate = self
                tabVYorumlar.rowHeight = UITableView.automaticDimension
                tabVYorumlar.estimatedRowHeight = 100

                // VM bindings
                viewModel.onEntryChange = { [weak self] e in
                    self?.tvEntryBaslik.text = e.title
                    self?.tvEntryYazanKisi.text = e.username
                    self?.tvEntryTarih.text = e.created_at.asTrDate
                }
                viewModel.onCommentsChange = { [weak self] list in
                    self?.yorumlar = list
                    self?.tabVYorumlar.reloadData()
                }
                viewModel.onError = { [weak self] msg in
                    self?.showAlert(title: "Hata", message: msg)
                }

                // başlat
                if entryId > 0 {
                    viewModel.start(entryId: entryId)
                }
        viewModel.onAddCommentResult = { [weak self] ok, msg in
                guard let self else { return }
                if ok {
                    self.tfYorum.text = ""
                } else {
                    self.showAlert(title: "Hata", message: msg)
                }
            }
        viewModel.onError = { [weak self] msg in self?.showAlert(title: "Hata", message: msg) }

                if entryId > 0 { viewModel.start(entryId: entryId) }

                // Klavye insets (opsiyonel ama faydalı)
                NotificationCenter.default.addObserver(self, selector: #selector(kbWillShow(_:)),
                                                      name: UIResponder.keyboardWillShowNotification, object: nil)
                NotificationCenter.default.addObserver(self, selector: #selector(kbWillHide(_:)),
                                                      name: UIResponder.keyboardWillHideNotification, object: nil)

                tfYorum.delegate = self
    }
    
    @IBAction func btnGonder(_ sender: Any) {
        let text = tfYorum.text ?? ""
                let userId = SessionManager.shared.getUserId()
                if text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                    showAlert(title: "Uyarı", message: "Yorum boş olamaz")
                    return
                }
                viewModel.addComment(userId: userId, text: text)
                tfYorum.resignFirstResponder()
    }
    @objc private func kbWillShow(_ n: Notification) {
        guard let kb = (n.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue else { return }
        let bottom = kb.height - view.safeAreaInsets.bottom
        tabVYorumlar.contentInset.bottom = bottom + 8
        tabVYorumlar.scrollIndicatorInsets.bottom = bottom + 8
    }
    @objc private func kbWillHide(_ n: Notification) {
        tabVYorumlar.contentInset.bottom = 0
        tabVYorumlar.scrollIndicatorInsets.bottom = 0
    }

    deinit { NotificationCenter.default.removeObserver(self) }
  
    @IBAction func btnGeri(_ sender: Any) {
        navigationController?.popViewController(animated: true)
    }
    
}
extension EntryDetayVC: UITableViewDataSource, UITableViewDelegate {

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        yorumlar.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {

        guard let cell = tableView.dequeueReusableCell(withIdentifier: "YorumlarCell", for: indexPath) as? YorumlarCell else {
            return UITableViewCell()
        }

        let c = yorumlar[indexPath.row]
        cell.tvYorum.text = c.comment_text
        cell.tvYorumYazanKisi.text = c.username
        cell.tvTarih.text = c.created_at.asTrDate
        cell.tvLike.text = "👍 \(c.likes)"
        cell.tvDislike.text = "👎 \(c.dislikes)"
        return cell
    }

    // Hücreye dokunulunca oy verme sheet'i
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let c = yorumlar[indexPath.row]
        tableView.deselectRow(at: indexPath, animated: true)

        let ac = UIAlertController(title: "Yorumu Oyla", message: nil, preferredStyle: .actionSheet)
        ac.addAction(UIAlertAction(title: "👍 Beğen", style: .default, handler: { [weak self] _ in
            let userId = SessionManager.shared.getUserId()
            self?.viewModel.vote(commentId: c.id, userId: userId, isLike: true)
        }))
        ac.addAction(UIAlertAction(title: "👎 Beğenme", style: .destructive, handler: { [weak self] _ in
            let userId = SessionManager.shared.getUserId()
            self?.viewModel.vote(commentId: c.id, userId: userId, isLike: false)
        }))
        ac.addAction(UIAlertAction(title: "İptal", style: .cancel))
        present(ac, animated: true)
    }
    
}
extension EntryDetayVC: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        btnGonder(btnGonder)
        return true
    }
}
