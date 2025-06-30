//
//  ViewController.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 28.03.2025.
//

import UIKit
import CoreImage
class MainVC: UIViewController {

    @IBOutlet weak var tvHediyeKahve: UILabel!
    @IBOutlet weak var tvKullanimlar: UILabel!
    @IBOutlet weak var container: UIView!
    @IBOutlet weak var segmentedControls: UISegmentedControl!
    private let viewModel = MainVM()
    
    var QRKodVC: UIViewController!
    var UrunlerimizVC: UIViewController!

    
    @IBOutlet weak var containerVC: UIView!
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
       

            DispatchQueue.main.async {
                if !self.viewModel.checkTelefonNumarasi() {
                    self.showInputDialog()
                }
                self.QRKodVC = self.storyboard?.instantiateViewController(identifier: "QRKodVC")
                self.UrunlerimizVC = self.storyboard?.instantiateViewController(identifier: "UrunlerimizVC")
                
                // İlk olarak firstVC'yi göster
                self.addChildVC(childVC: self.QRKodVC)
            }
        
       observeViewModel()
        
           
       
    }
    private func observeViewModel() {
        viewModel.hediyeKahveUpdated = { [weak self] hediyeKahve in
            DispatchQueue.main.async {
                self?.tvHediyeKahve.text = "Hediye Kahve: \(hediyeKahve)"
            }
        }
        
        viewModel.kahveSayisiUpdated = { [weak self] kahveSayisi in
            DispatchQueue.main.async {
                self?.tvKullanimlar.text = "Kullanımlar: \(kahveSayisi)/5"
            }
        }
        
        viewModel.telefonNumarasiUpdated = { [weak self] numara in
            DispatchQueue.main.async {
                if numara == nil {
                    self?.showInputDialog()
                } else {
                    self?.viewModel.kullaniciEkle(numara: numara!)
                }
            }
        }
    }
    
    private func showInputDialog() {
        let alert = UIAlertController(title: "Bilgi Girişi", message: "Lütfen telefon numaranızı giriniz", preferredStyle: .alert)
        alert.addTextField { textField in
            textField.placeholder = "Telefon numarası"
            textField.keyboardType = .numberPad
        }
        
        alert.addAction(UIAlertAction(title: "Tamam", style: .default, handler: { [weak self] _ in
            if let userInput = alert.textFields?.first?.text, !userInput.isEmpty {
                self?.viewModel.numarayiKaydet(numara: userInput)
                
            }
        }))
        
        alert.addAction(UIAlertAction(title: "İptal", style: .cancel, handler: { _ in
            exit(0) // Uygulamayı kapat
        }))
        
        present(alert, animated: true)
    }
    

    @IBAction func segmentControlAction(_ sender: UISegmentedControl) {
        if sender.selectedSegmentIndex == 0 {
                    switchChildVC(to: QRKodVC)
                } else {
                    switchChildVC(to: UrunlerimizVC)
                }
    }
    
      func addChildVC(childVC: UIViewController) {
          // Var olan çocuk ViewController'ları kaldır
          removeChildVC()
          
          // Yeni ViewController'ı ekle
          addChild(childVC)
          childVC.view.frame = container.bounds
          container.addSubview(childVC.view)
          childVC.didMove(toParent: self)
      }
      
      func removeChildVC() {
          if let currentVC = children.first {
              currentVC.willMove(toParent: nil)
              currentVC.view.removeFromSuperview()
              currentVC.removeFromParent()
          }
      }

      func switchChildVC(to newVC: UIViewController) {
          removeChildVC()
          addChildVC(childVC: newVC)
      }
    
    

   


}

