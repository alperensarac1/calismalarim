//
//  RehberAnasayfaVC.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 28.01.2025.
//

import UIKit

class RehberAnasayfaVC: UIViewController {

    @IBOutlet weak var searchBar: UISearchBar!
    @IBOutlet weak var tableView: UITableView!
    var aramaYapiliyorMu = false
    var kisiler : [Kisi] = []
    var rehberDao:RehberDao!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.delegate = self
        tableView.dataSource = self
        searchBar.delegate = self
        DispatchQueue.main.async {
            self.rehberDao = RehberDao()
            self.kisiler = self.rehberDao.kisileriGetir()
            self.tableView.reloadData()
        }
        
    }
    
    
    
    @IBAction func btnEkle(_ sender: Any) {
        let alert = UIAlertController(title: "Kişi Ekle", message: "Eklenecek kişinin adını ve numarasını giriniz.", preferredStyle: .alert)
        alert.addTextField{tf1 in
            tf1.placeholder = "Adı"
        }
        alert.addTextField{tf2 in
            tf2.placeholder = "0xxxxxxxxx"
        }
        let kaydetAction = UIAlertAction(title: "Kaydet", style: .destructive){action in
            let alinanAd = (alert.textFields![0] as UITextField).text!
            let alinanNumara = (alert.textFields![1] as UITextField).text!
            self.rehberDao.kisiEkle(kisi_ad: alinanAd, kisi_tel: alinanNumara)
            self.kisiler = self.rehberDao.kisileriGetir()
            self.tableView.reloadData()
        }
        alert.addAction(kaydetAction)
        self.present(alert, animated: true)
    }
    
}
extension RehberAnasayfaVC:UITableViewDelegate,UITableViewDataSource{
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return kisiler.count
    }
    
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "rehberCell") as! RehberAnasayfaCell
        cell.tvKisi.text = kisiler[indexPath.row].kisi_ad
        cell.numara = kisiler[indexPath.row].kisi_tel
        return cell
    }
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        // Alert göster
        let alert = UIAlertController(title: "Seçenekler", message: "Ne yapmak istersiniz?", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Düzenle", style: .default) { _ in
            let duzenleAlert = UIAlertController(title:"Düzenle",message: "Adını ve Numarasını düzenleyebilirsiniz.",preferredStyle: .alert)
            duzenleAlert.addTextField{tf1 in
                tf1.placeholder = "Adı"
            }
            duzenleAlert.addTextField{tf2 in
                tf2.placeholder = "0xxxxxxxxx"
            }
            let kaydetAction = UIAlertAction(title: "Kaydet", style: .destructive){action in
                let alinanAd = (duzenleAlert.textFields![0] as UITextField).text!
                let alinanNumara = (duzenleAlert.textFields![1] as UITextField).text!
                self.rehberDao.kisiGuncelle(kisi: self.kisiler[indexPath.row], kisi_ad: alinanAd, kisi_tel: alinanNumara)
                self.kisiler = self.rehberDao.kisileriGetir()
                self.tableView.reloadData()
                
            }
            duzenleAlert.addAction(kaydetAction)
            self.present(duzenleAlert, animated: true)
        })
        alert.addAction(UIAlertAction(title: "Sil", style: .destructive) { _ in
            self.rehberDao.kisiSil(kisi: self.kisiler[indexPath.row])
            self.kisiler = self.rehberDao.kisileriGetir()
            self.tableView.reloadData()
        })
        alert.addAction(UIAlertAction(title: "İptal Et", style: .cancel))
        self.present(alert, animated: true)
        
    }
}
extension RehberAnasayfaVC:UISearchBarDelegate{
    
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        print("Arama Sonucu: \(searchText)")
        aramaYapiliyorMu = true
        
        if searchText == ""{
            aramaYapiliyorMu = false //arama kelimeleri silinirse çalışır.
            kisiler = rehberDao.kisileriGetir()
        }else{
            aramaYapiliyorMu = true
            kisiler = rehberDao.kisiGetir(kisi_ad: searchText)
        }
        tableView.reloadData()//arayüzü her seferinde güncellemek için.
        
    }

}
