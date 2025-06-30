//
//  AnaekranVC.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 19.01.2025.
//

import UIKit

class AnaekranVC: UIViewController {

    @IBOutlet weak var collectionView: UICollectionView!
    var uygulamalar : [Uygulama] = []
    let consts = Consts()
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "toCopKutusu"{
            let gidilecekVC = segue.destination as! CopkutusuVC
            gidilecekVC.consts = self.consts
        }
        self.performTransition(segue: segue, withIdentifier: "toCopKutusu", viewControllerType: CopkutusuVC.self)
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
        DispatchQueue.main.async {
            
            self.uygulamalar = self.consts.copKutusundaOlmayanUygulamalar
            self.collectionView.reloadData()
        }

        collectionView.delegate = self
        collectionView.dataSource = self
        let longPressGesture = UILongPressGestureRecognizer(target: self, action: #selector(longPressAction(_:)))
           collectionView.addGestureRecognizer(longPressGesture)
       
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        DispatchQueue.main.async {
            self.uygulamalar = self.consts.copKutusundaOlmayanUygulamalar
            self.collectionView.reloadData()
        }
    }
    
}
extension AnaekranVC:UICollectionViewDelegate,UICollectionViewDataSource{
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return uygulamalar.count
    }
    
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "anaekranCell", for: indexPath) as! AnaEkranCollectionViewCell
        cell.tvUygulama.text = uygulamalar[indexPath.row].uygulamaAdi
        cell.imgUygulama.image = UIImage(systemName: uygulamalar[indexPath.row].uygulamaResmi)
        return cell
    }
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        performSegue(withIdentifier: uygulamalar[indexPath.row].uygulamaGecisId, sender: nil)
    }
    
    // 📌 Long Press Tetiklendiğinde Çalışan Fonksiyon
    @objc func longPressAction(_ gesture: UILongPressGestureRecognizer) {
        let touchPoint = gesture.location(in: collectionView)
        
        if let indexPath = collectionView.indexPathForItem(at: touchPoint) {
            if gesture.state == .began { // Sadece ilk basıldığında çalışsın
                print("Uzun basıldı: \(uygulamalar[indexPath.row])")
                
                // 💡 Aksiyonlar menüsü oluştur (Opsiyonel)
                let alert = UIAlertController(title: "Uygulamayı kaldırmak istiyor musunuz ?", message: "\(uygulamalar[indexPath.row].uygulamaAdi)", preferredStyle: .actionSheet)
                alert.addAction(UIAlertAction(title: "Çöp Kutusuna Taşı", style: .default, handler: { _ in
                    self.consts.copKutusunaTasi(indexPathRow: indexPath.row, uygulama: self.uygulamalar[indexPath.row])
                    self.uygulamalar.remove(at: indexPath.row)
                    self.collectionView.reloadData()
                }))
                alert.addAction(UIAlertAction(title: "İptal", style: .cancel, handler: nil))
                
                present(alert, animated: true, completion: nil)
            }
        }
    }
    
}
