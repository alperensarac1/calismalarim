import UIKit

class NotDefteriAnasayfaVC: UIViewController {

    @IBOutlet weak var collectionView: UICollectionView!
    var notlar: [Not] = []
    var secilenNot: Not?
    var notDao: NotDefteriDAO!

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "toNotDetay" {
            let destinationVC = segue.destination as! NotDefteriDetaysayfaVC
            // Eğer secilenNot nil değilse, değeri aktar
            if let not = secilenNot {
                destinationVC.gelenNot = not
            } else {
                // Yeni bir not ekliyorsanız gelenNot nil olacak
                destinationVC.gelenNot = nil
            }
        }
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        DispatchQueue.main.async {
            self.notlar = self.notDao.notlariGetir()
            self.collectionView.reloadData()
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        collectionView.delegate = self
        collectionView.dataSource = self
        notDao = NotDefteriDAO()
        
        DispatchQueue.main.async {
            self.notlar = self.notDao.notlariGetir()
            self.collectionView.reloadData()
        }
        
        // Long press gesture ekle
        let longPressGesture = UILongPressGestureRecognizer(target: self, action: #selector(handleLongPress))
        collectionView.addGestureRecognizer(longPressGesture)
    }

    @objc func handleLongPress(gesture: UILongPressGestureRecognizer) {
        if gesture.state == .began {
            let point = gesture.location(in: collectionView)

            // Basılan noktanın hangi indexPath'e denk geldiğini buluyoruz
            if let indexPath = collectionView.indexPathForItem(at: point) {
                print("Hücreye uzun basıldı: \(notlar[indexPath.row])")
                
                // Alert göster
                let alert = UIAlertController(title: "Seçenekler", message: "Ne yapmak istersiniz?", preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "Düzenle", style: .default) { _ in
                    self.secilenNot = self.notlar[indexPath.row]
                    self.performSegue(withIdentifier: "toNotDetay", sender: nil)
                })
                alert.addAction(UIAlertAction(title: "Sil", style: .destructive) { _ in
                    self.notDao.notSil(not: self.notlar[indexPath.row])
                    self.notlar = self.notDao.notlariGetir()
                    self.collectionView.reloadData()
                })
                alert.addAction(UIAlertAction(title: "İptal Et", style: .cancel))
                self.present(alert, animated: true)
            }
        }
    }

    @IBAction func btnNotEkle(_ sender: Any) {
        secilenNot = nil
        performSegue(withIdentifier: "toNotDetay", sender: nil)
    }
}

extension NotDefteriAnasayfaVC: UICollectionViewDataSource, UICollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return notlar.count
    }
    
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "notCell", for: indexPath) as! NotDefteriAnasayfaCell
        cell.tvNotBaslik.text = String(notlar[indexPath.row].not_baslik?.prefix(10) ?? "")
        cell.tvNotIcerik.text = String(notlar[indexPath.row].not_icerik?.prefix(30) ?? "")
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        secilenNot = notlar[indexPath.row]
        performSegue(withIdentifier: "toNotDetay", sender: nil)
    }
}
