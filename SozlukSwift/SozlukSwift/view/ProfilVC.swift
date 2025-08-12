//
//  ProfilVC.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import UIKit

class ProfilVC: UIViewController,SegmentedNavigatable {
    let segmentIndex: Int = 2
    @IBOutlet weak var svProfil: UISearchBar!
    @IBOutlet weak var segmentedProfil: UISegmentedControl!
    @IBOutlet weak var tabVProfil: UITableView!
    
    private let viewModel = ProfilViewModel()
       private var data: [Entry] = []
       private let userId = SessionManager.shared.getUserId()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        tabVProfil.dataSource = self
                tabVProfil.delegate = self
                tabVProfil.rowHeight = UITableView.automaticDimension
                tabVProfil.estimatedRowHeight = 88

                svProfil.delegate = self

                viewModel.onEntriesChange = { [weak self] list in
                    self?.data = list
                    self?.tabVProfil.reloadData()
                }
                viewModel.onDeleteResult = { [weak self] resp in
                    self?.showAlert(title: resp.success ? "Silindi" : "Hata", message: resp.message)
                }
                viewModel.onError = { [weak self] msg in
                    self?.showAlert(title: "Hata", message: msg)
                }

                viewModel.loadUserEntries(userId: userId)
    }
    

    @IBAction func segmentChanged(_ sender: UISegmentedControl) {
        navigateToSegment(index: sender.selectedSegmentIndex)
    }
    @IBAction func btnEntryEkle(_ sender: Any) {
        let ac = UIAlertController(title: "Yeni Entry", message: nil, preferredStyle: .alert)
            ac.addTextField { $0.placeholder = "Başlık" }
            ac.addTextField { tf in
                tf.placeholder = "İçerik"
                tf.autocapitalizationType = .sentences
            }
            ac.addAction(UIAlertAction(title: "İptal", style: .cancel))
            ac.addAction(UIAlertAction(title: "Kaydet", style: .default, handler: { _ in
                let title = ac.textFields?[0].text ?? ""
                let content = ac.textFields?[1].text ?? ""
                if title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ||
                   content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                    self.showAlert(title: "Uyarı", message: "Başlık ve içerik boş olamaz")
                    return
                }
                let userId = SessionManager.shared.getUserId()
                SozlukDao.shared.addEntry(userId: userId, title: title, content: content) { res in
                    DispatchQueue.main.async {
                        switch res {
                        case .success(let r):
                            self.showAlert(title: r.success ? "Tamam" : "Hata", message: r.message)
                            // listeni yenile (ör: viewModel.loadMostCommentedEntriesToday())
                        case .failure:
                            self.showAlert(title: "Hata", message: "Bağlantı hatası")
                        }
                    }
                }
            }))
            present(ac, animated: true)
    }
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
          if segue.identifier == "toEntryDetay",
             let entryId = sender as? Int {
              (segue.destination as? EntryDetayVC)?.entryId = entryId
          }
      }
}
extension ProfilVC: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { data.count }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {

        guard let cell = tableView.dequeueReusableCell(withIdentifier: "EntryCell", for: indexPath) as? EntryCell else {
            return UITableViewCell()
        }
        let item = data[indexPath.row]
        cell.tvEntry.text = item.title + "\n" + item.content
        cell.tvYazanKisi.text = item.username
        cell.tvTarih.text = item.created_at.asTrDate
        return cell
    }

    // Detaya git
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        performSegue(withIdentifier: "toEntryDetay", sender: data[indexPath.row].id)
    }

    // Swipe to delete (iOS 11+)
    func tableView(_ tableView: UITableView,
                   trailingSwipeActionsConfigurationForRowAt indexPath: IndexPath) -> UISwipeActionsConfiguration? {
        let sil = UIContextualAction(style: .destructive, title: "Sil") { [weak self] _, _, done in
            guard let self else { return }
            let entryId = self.data[indexPath.row].id
            self.viewModel.deleteEntry(entryId: entryId, userId: self.userId)
            done(true)
        }
        return UISwipeActionsConfiguration(actions: [sil])
    }
}

extension ProfilVC: UISearchBarDelegate {
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        viewModel.setSearchQuery(searchText)
    }
    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        searchBar.resignFirstResponder()
    }
}
