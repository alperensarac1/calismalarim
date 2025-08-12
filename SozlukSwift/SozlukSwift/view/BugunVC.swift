//
//  BugunVC.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import UIKit

class BugunVC: UIViewController,SegmentedNavigatable {

    @IBOutlet weak var svBugun: UISearchBar!
    @IBOutlet weak var segmentedBugun: UISegmentedControl!
    @IBOutlet weak var tabVBugun: UITableView!
    private let viewModel = BugunViewModel()
    let segmentIndex: Int = 1
    
      private var data: [Entry] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        
        tabVBugun.dataSource = self
                tabVBugun.delegate = self
                tabVBugun.rowHeight = UITableView.automaticDimension
                tabVBugun.estimatedRowHeight = 88

                svBugun.delegate = self

                viewModel.onEntriesChange = { [weak self] list in
                    self?.data = list
                    self?.tabVBugun.reloadData()
                }
                viewModel.onError = { [weak self] msg in
                    self?.showAlert(title: "Hata", message: msg)
                }

                viewModel.loadTodayEntries()
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
extension BugunVC: UITableViewDataSource, UITableViewDelegate {
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

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let entryId = data[indexPath.row].id
        performSegue(withIdentifier: "toEntryDetay", sender: entryId)
    }
}

extension BugunVC: UISearchBarDelegate {
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        viewModel.setSearchQuery(searchText)
    }
    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        searchBar.resignFirstResponder()
    }
}
