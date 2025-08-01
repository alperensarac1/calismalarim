//
//  KategoriVC.swift
//  HaberUygulamaSwift
//
//  Created by Alperen Saraç on 19.07.2025.
//

import UIKit
import AVKit

class KategoriVC: UIViewController {

    @IBOutlet weak var cvKategori: UICollectionView!
    var kategoriAd:String!
    var haberler: [HaberModel] = []
    let viewModel = KategorilerViewModel()
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
            cvKategori.delegate = self
                cvKategori.dataSource = self

                viewModel.kategoriHaberleriDidChange = { [weak self] liste in
                    self?.haberler = liste
                    self?.cvKategori.reloadData()
                }

                viewModel.loadKategoriHaberleri(turAd: kategoriAd)
    }
    
    
    

}
extension KategoriVC: UICollectionViewDelegate, UICollectionViewDataSource {
    

    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return haberler.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "KategoriHaberCell", for: indexPath) as? HaberCellCollectionViewCell else {
            return UICollectionViewCell()
        }

        let haber = haberler[indexPath.row]
        cell.tvHaberBaslik.text = haber.baslik
        cell.tvDevaminiOku.text = "Devamını Oku"
        cell.haber = haber
        cell.delegate = self

        if haber.media_type == "video" {
            cell.imageView.isHidden = true
            cell.videoView.isHidden = false
            cell.btnPlay.isHidden = false
        } else {
            cell.imageView.isHidden = false
            cell.videoView.isHidden = true
            cell.btnPlay.isHidden = true

            if let url = URL(string: haber.media_url) {
                DispatchQueue.global().async {
                    if let data = try? Data(contentsOf: url),
                       let image = UIImage(data: data) {
                        DispatchQueue.main.async {
                            if collectionView.indexPath(for: cell) == indexPath {
                                cell.imageView.image = image
                            }
                        }
                    }
                }
            }
        }

        return cell
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let haber = haberler[indexPath.row]

        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        if let detayVC = storyboard.instantiateViewController(withIdentifier: "HaberDetayVC") as? HaberDetayVC {
            detayVC.haber = haber
            navigationController?.pushViewController(detayVC, animated: true)
        }
    }
}
extension KategoriVC: HaberCellDelegate {
    func playButtonTapped(for haber: HaberModel) {
        guard let url = URL(string: haber.media_url) else { return }
        let player = AVPlayer(url: url)
        let playerVC = AVPlayerViewController()
        playerVC.player = player
        self.present(playerVC, animated: true) {
            player.play()
        }
    }
}
