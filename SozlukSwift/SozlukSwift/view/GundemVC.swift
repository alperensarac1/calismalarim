//
//  GundemVC.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import UIKit

class GundemVC: UIViewController,SegmentedNavigatable {

    @IBOutlet weak var svGundem: UISearchBar!
    @IBOutlet weak var tabVGundem: UITableView!
    
    private let viewModel = GundemViewModel()
    private var data: [Entry] = []
    let segmentIndex: Int = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()

        
        tabVGundem.dataSource = self
        tabVGundem.delegate = self
        tabVGundem.rowHeight = UITableView.automaticDimension
        tabVGundem.estimatedRowHeight = 88

        svGundem.delegate = self

        viewModel.onEntriesChange = { [weak self] list in
            self?.data = list
            self?.tabVGundem.reloadData()
        }
        viewModel.onError = { [weak self] msg in
            self?.showAlert(title: "Hata", message: msg)
        }

        viewModel.loadMostCommentedEntriesToday()
    }
    

    
     @IBAction func segmentedChanged(_ sender: UISegmentedControl) {
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
            // hedef VC’de entryId property’si olduğunu varsay
            (segue.destination as? EntryDetayVC)?.entryId = entryId
        }
    }
}
extension GundemVC: UITableViewDataSource, UITableViewDelegate {
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

extension GundemVC: UISearchBarDelegate {
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        viewModel.setSearchQuery(searchText)
    }
    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        searchBar.resignFirstResponder()
    }
}
