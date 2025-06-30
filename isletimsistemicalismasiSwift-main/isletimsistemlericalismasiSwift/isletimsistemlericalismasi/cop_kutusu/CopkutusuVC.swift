//
//  CopkutusuVC.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 24.01.2025.
//

import UIKit

class CopkutusuVC: UIViewController {
    @IBOutlet weak var collectionView: UICollectionView!
    var copUygulamalar : [Uygulama] = []
    var consts : Consts!
    override func viewDidLoad() {
        super.viewDidLoad()
        
       
        DispatchQueue.main.async {
            
            self.copUygulamalar = self.consts.copKutusuUygulamalari
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
            self.copUygulamalar = self.consts.copKutusuUygulamalari
            self.collectionView.reloadData()
        }
    }
    
   

}
extension CopkutusuVC:UICollectionViewDelegate,UICollectionViewDataSource{
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return copUygulamalar.count
    }
    
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "copkutusuCell", for: indexPath) as! CopkutusuCollectionViewCell
        cell.tvUygulama.text = copUygulamalar[indexPath.row].uygulamaAdi
        cell.imgUygulama.image = UIImage(systemName: copUygulamalar[indexPath.row].uygulamaResmi)
        return cell
    }
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        
    }
    
    @objc func longPressAction(_ gesture: UILongPressGestureRecognizer) {
        let touchPoint = gesture.location(in: collectionView)
        
        if let indexPath = collectionView.indexPathForItem(at: touchPoint) {
            if gesture.state == .began { // Sadece ilk basıldığında çalışsın
                print("Uzun basıldı: \(copUygulamalar[indexPath.row])")
                
                // 💡 Aksiyonlar menüsü oluştur (Opsiyonel)
                let alert = UIAlertController(title: "Uygulamayı geri yüklemek istiyor musunuz ?", message: "\(copUygulamalar[indexPath.row].uygulamaAdi)", preferredStyle: .actionSheet)
                alert.addAction(UIAlertAction(title: "Geri Yükle", style: .default, handler: { _ in
                    self.consts.copKutusundanCikar(indexPathRow: indexPath.row, uygulama: self.copUygulamalar[indexPath.row])
                    self.copUygulamalar.remove(at: indexPath.row)
                    self.collectionView.reloadData()
                }))
                alert.addAction(UIAlertAction(title: "İptal", style: .cancel, handler: nil))
                
                present(alert, animated: true, completion: nil)
            }
        }
    }
    
}
