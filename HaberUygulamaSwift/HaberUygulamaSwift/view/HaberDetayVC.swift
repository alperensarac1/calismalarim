//
//  HaberDetayVC.swift
//  HaberUygulamaSwift
//
//  Created by Alperen Saraç on 19.07.2025.
//

import UIKit
import AVKit
import AVFoundation
class HaberDetayVC: UIViewController {
    
    var player: AVPlayer?
    var playerLayer: AVPlayerLayer?
    var haber:HaberModel!
    
    
    var viewModel = HaberDetayViewModel()
    var yorumlar: [YorumModel] = []
    var son3Haberler: [HaberModel] = []
    
    @IBOutlet weak var tableViewYorumlar: UITableView!
    @IBOutlet weak var btnYorumGonder: UIButton!
    @IBOutlet weak var etYorumIcerik: UITextField!
    @IBOutlet weak var etYorumAd: UITextField!
    @IBOutlet weak var cvSonUcHaber: UICollectionView!
    @IBOutlet weak var tvHaberIcerik: UILabel!
    @IBOutlet weak var tvTarih: UILabel!
    @IBOutlet weak var tvEditorAdMevki: UILabel!
    @IBOutlet weak var imgHaber: UIImageView!
    @IBOutlet weak var btnVideoPlay: UIButton!
    @IBOutlet weak var videoView: UIView!
    override func viewDidLoad() {
        super.viewDidLoad()

        tableViewYorumlar.dataSource = self
        cvSonUcHaber.dataSource = self
        cvSonUcHaber.delegate = self

        tvHaberIcerik.text = haber.icerik
        tvTarih.text = haber.yayinlanma_tarihi
        tvEditorAdMevki.text = "\(haber.ad ?? "") \(haber.soyad ?? "") - \(haber.unvan ?? "")"

        if haber.media_type == "video" {
            imgHaber.isHidden = true
            videoView.isHidden = false
            btnVideoPlay.isHidden = false
        } else {
            imgHaber.isHidden = false
            videoView.isHidden = true
            btnVideoPlay.isHidden = true
            if let url = URL(string: haber.media_url) {
                DispatchQueue.global().async {
                    if let data = try? Data(contentsOf: url),
                       let image = UIImage(data: data) {
                        DispatchQueue.main.async {
                            self.imgHaber.image = image
                        }
                    }
                }
            }
        }

        // ViewModel bağlantıları
        viewModel.yorumlarDidChange = { [weak self] yorumlar in
            self?.yorumlar = yorumlar
            self?.tableViewYorumlar.reloadData()
        }

        viewModel.son3HaberlerDidChange = { [weak self] haberler in
            self?.son3Haberler = haberler
            self?.cvSonUcHaber.reloadData()
        }

        viewModel.loadYorumlar(haberId: haber.id)
        viewModel.loadSon3Haber()
    }
    

    @IBAction func btnVideoOynatTapped(_ sender: Any) {
        guard let url = URL(string: haber.media_url) else { return }

              player = AVPlayer(url: url)
              playerLayer = AVPlayerLayer(player: player)
              playerLayer?.frame = videoView.bounds
              playerLayer?.videoGravity = .resizeAspect
              if let layer = playerLayer {
                  videoView.layer.addSublayer(layer)
              }

              player?.play()
              btnVideoPlay.isHidden = true // Butonu gizle oynatma başlayınca
        }

}

extension HaberDetayVC: UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return yorumlar.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let yorum = yorumlar[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: "YorumCell", for: indexPath)
        cell.textLabel?.text = "\(yorum.kullanici): \(yorum.yorum)"
        return cell
    }
}

extension HaberDetayVC: UICollectionViewDataSource, UICollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return son3Haberler.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "SonUcHaberCell", for: indexPath) as! SonUcHaberCell
        let haber = son3Haberler[indexPath.row]
        cell.tvHaberBaslik.text = haber.baslik
        cell.tvDevaminiOku.text = "Devamını Oku"
        // image veya video gösterimi yapılabilir
        return cell
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let secilenHaber = son3Haberler[indexPath.row]
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        if let detayVC = storyboard.instantiateViewController(withIdentifier: "HaberDetayVC") as? HaberDetayVC {
            detayVC.haber = secilenHaber
            navigationController?.pushViewController(detayVC, animated: true)
        }
    }
}
