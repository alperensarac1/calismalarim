//
//  UrunlerimizVC.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 28.03.2025.
//
import UIKit
class UrunlerimizVC: UIViewController, UICollectionViewDelegate,UICollectionViewDataSource {
    
   
    
    @IBOutlet var contentView: UIView!
    @IBOutlet weak var scrollView: UIScrollView!
    
    @IBOutlet weak var atistirmaliklarCollectionView: UICollectionView!
    @IBOutlet weak var kampanyalarCollectionView: UICollectionView!
    @IBOutlet weak var iceceklerCollectionView: UICollectionView!
   
    private let viewModel = UrunlerimizVM(kahveServis: ServiceImpl.getInstance())
    override func viewDidLoad() {
        super.viewDidLoad()
        scrollView.contentSize = CGSize(width: scrollView.frame.width, height: scrollView.frame.height + 250)
        scrollView.setContentOffset(scrollView.contentOffset, animated: false)
        
        
        fetchData() 
        setupCollectionViews()
        
        print("📌 Kampanyalar CollectionView Tag: \(kampanyalarCollectionView.tag)")

        
    }
   
    private func setupCollectionViews() {
         let collectionViews = [kampanyalarCollectionView, iceceklerCollectionView, atistirmaliklarCollectionView]
         let layout = UICollectionViewFlowLayout()
         layout.scrollDirection = .horizontal
         layout.itemSize = CGSize(width: 150, height: 200)
         layout.minimumInteritemSpacing = 10
         layout.minimumLineSpacing = 10
         
         for collectionView in collectionViews {
             collectionView?.delegate = self
             collectionView?.dataSource = self
             collectionView?.reloadData()
             collectionView?.collectionViewLayout = layout
             
         }
     }

    private func fetchData() {
        viewModel.configureIndirimliCollectionView(collectionView: kampanyalarCollectionView)
        viewModel.configureCollectionView(for: .ICECEKLER, collectionView: iceceklerCollectionView)
        viewModel.configureCollectionView(for: .ATISTIRMALIKLAR, collectionView: atistirmaliklarCollectionView)

        DispatchQueue.main.asyncAfter(deadline: .now() + 1) { // Asenkron veri gelme süresi için 1 sn bekletiyoruz
            let layout = UICollectionViewFlowLayout()
            layout.scrollDirection = .horizontal
            layout.itemSize = CGSize(width: 150, height: 200)
            layout.minimumInteritemSpacing = 10
            layout.minimumLineSpacing = 10

            self.kampanyalarCollectionView.collectionViewLayout = layout
            self.iceceklerCollectionView.collectionViewLayout = layout
            self.atistirmaliklarCollectionView.collectionViewLayout = layout
            
            print("📌 Tüm CollectionView'lar reloadData çağrıldı")
            
            self.kampanyalarCollectionView.reloadData()
            self.iceceklerCollectionView.reloadData()
            self.atistirmaliklarCollectionView.reloadData()
        }
    }

     // MARK: - UICollectionViewDataSource
     func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
         return viewModel.getUrunler(for: collectionView).count
     }
     
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "Cell", for: indexPath) as! UrunCollectionViewCell
        let urun = viewModel.getUrunler(for: collectionView)[indexPath.row]
        
        // Hücreyi temizle
        cell.tvUrunAd.text = urun.urun_ad
        cell.imgUrun.image = UIImage(named: "mocha") // Varsayılan resim koy

        // 🔹 URL'yi güvenli şekilde oluştur
        guard let urlString = urun.urun_resim.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let url = URL(string: urlString) else {
            print("⚠️ Geçersiz URL: \(urun.urun_resim)")
            return cell
        }

        // 🔹 Resmi arka planda indir ve hücreye ekle
        URLSession.shared.dataTask(with: url) { data, response, error in
            if let error = error {
                print("❌ Resim indirilemedi: \(error.localizedDescription)")
                return
            }

            if let data = data, let image = UIImage(data: data) {
                DispatchQueue.main.async {
                    cell.imgUrun.image = image
                }
            }
        }.resume()
        
        // **İndirim Kontrolü**
        if urun.urun_indirim == "1" {
            // 🔹 İndirimsiz fiyatı çiz
            let attributeString = NSMutableAttributedString(string: "\(urun.urun_fiyat) TL")
            attributeString.addAttribute(.strikethroughStyle, value: NSUnderlineStyle.single.rawValue, range: NSMakeRange(0, attributeString.length))
            cell.tvIndirimsizFiyat.attributedText = attributeString
            
            // 🔹 İndirimli fiyatı kırmızı yap
            cell.tvFiyat.text = "\(urun.urun_indirimli_fiyat) TL"
            cell.tvFiyat.textColor = UIColor.red
            
            // 🔹 İndirim yüzdesini göster
            cell.tvIndirimYuzde.text = "%-\(indirimYuzdeHesapla(indirimsizFiyat: urun.urun_fiyat, indirimliFiyat: urun.urun_indirimli_fiyat))"
            cell.viewIndirimVarMi.isHidden = false
            cell.tvIndirimYuzde.isHidden = false
            cell.tvIndirimsizFiyat.isHidden = false
        } else {
            // 🔹 İndirim yoksa sadece normal fiyat göster
            cell.viewIndirimVarMi.isHidden = true
            cell.tvFiyat.text = "\(urun.urun_fiyat) TL"
            cell.tvFiyat.textColor = UIColor.black
            cell.tvIndirimYuzde.isHidden = true
            cell.tvIndirimsizFiyat.isHidden = true
        }

        return cell
    }





    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let urun = viewModel.getUrunler(for: collectionView)[indexPath.row]

        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        if let urunDetayVC = storyboard.instantiateViewController(withIdentifier: "UrunDetayVC") as? UrunDetayVC {
            urunDetayVC.urun = urun
            urunDetayVC.modalPresentationStyle = .fullScreen // Tam ekran açılmasını sağlar
            present(urunDetayVC, animated: true, completion: nil)
        }
    }
    
}
   
