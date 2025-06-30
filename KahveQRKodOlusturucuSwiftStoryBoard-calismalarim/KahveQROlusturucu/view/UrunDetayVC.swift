//
//  UrunDetayVC.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 4.04.2025.
//

import UIKit

class UrunDetayVC: UIViewController {
    
    @IBOutlet weak var imgGeri: UIImageView!
    @IBOutlet weak var tvUrunAciklama: UILabel!
    @IBOutlet weak var tvUrunIndirimliFiyay: UILabel!
    @IBOutlet weak var tvUrunIndirimsizFiyat: UILabel!
    @IBOutlet weak var imgUrun: UIImageView!
    @IBOutlet weak var tvUrunAd: UILabel!
    var urun:Urun = Urun(id: "", urun_ad: "", urun_resim: "", urun_aciklama: "", urun_kategori_id: "", urun_indirim: "", urun_fiyat: "", urun_indirimli_fiyat: "")

    override func viewDidLoad() {
          super.viewDidLoad()
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(geriDon))
            imgGeri.isUserInteractionEnabled = true // Dokunmayı aktif et
           imgGeri.addGestureRecognizer(tapGesture)
          
          
          if let url = URL(string: urun.urun_resim.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "") {
              indirVeGosterResim(from: url)
          } else {
              imgUrun.image = UIImage(named: "kahve") // Varsayılan resim
          }

         
          tvUrunAd.text = urun.urun_ad
          tvUrunAciklama.text = urun.urun_aciklama

          if let fiyat = Double(urun.urun_fiyat) {
              tvUrunIndirimsizFiyat.text = "\(Int(fiyat)) TL"
              tvUrunIndirimliFiyay.textColor = UIColor(named: "textColor")
          }

         
          if urun.urun_indirim == "1", let indirimliFiyat = Double(urun.urun_indirimli_fiyat) {
              
              tvUrunIndirimliFiyay.text = "\(Int(indirimliFiyat)) TL"
              tvUrunIndirimliFiyay.isHidden = false
              tvUrunIndirimliFiyay.textColor = .red
              
              
              let attributeString: NSMutableAttributedString = NSMutableAttributedString(string: tvUrunIndirimsizFiyat.text ?? "")
              attributeString.addAttribute(.strikethroughStyle, value: 2, range: NSMakeRange(0, attributeString.length))
              tvUrunIndirimsizFiyat.attributedText = attributeString
              
          } else {
              
              tvUrunIndirimliFiyay.isHidden = true
          }
          
        
      }
      
      
      func indirVeGosterResim(from url: URL) {
          URLSession.shared.dataTask(with: url) { data, response, error in
              if let data = data, let image = UIImage(data: data) {
                  DispatchQueue.main.async {
                      self.imgUrun.image = image
                  }
              } else {
                  print("❌ Resim yüklenemedi, hata: \(error?.localizedDescription ?? "Bilinmeyen hata")")
                  DispatchQueue.main.async {
                      self.imgUrun.image = UIImage(named: "kahve") // Hata durumunda varsayılan resim göster
                  }
              }
          }.resume()
      }
      
      @objc func geriDon() {
          self.dismiss(animated: true, completion: nil) // Modal kapatma
      }
    



}
