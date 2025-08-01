//
//  ViewController.swift
//  HaberUygulamaSwift
//
//  Created by Alperen Saraç on 18.07.2025.
//

import UIKit
import AVKit
import AVFoundation

class ViewController: UIViewController {

    @IBOutlet weak var tableViewKategoriler: UITableView!
    @IBOutlet weak var cvGundem: UICollectionView!
    @IBOutlet weak var cvSonDakika: UICollectionView!
    
    let viewModel = HaberlerViewModel()

    var gundemHaberler: [HaberModel] = []
    var sonDakikaHaberler: [HaberModel] = []
    var kategoriler: [HaberTuruModel] = []
    override func viewDidLoad() {
        super.viewDidLoad()

        tableViewKategoriler.delegate = self
        tableViewKategoriler.dataSource = self
        cvGundem.delegate = self
        cvGundem.dataSource = self
        cvSonDakika.delegate = self
        cvSonDakika.dataSource = self

        // Closure bağlantıları
        viewModel.kategorilerDidChange = { [weak self] kategoriler in
            self?.kategoriler = kategoriler
            self?.tableViewKategoriler.reloadData()
        }

        viewModel.gundemDidChange = { [weak self] haberler in
            self?.gundemHaberler = haberler
            self?.cvGundem.reloadData()
        }

        viewModel.sonDakikaDidChange = { [weak self] haberler in
            self?.sonDakikaHaberler = haberler
            self?.cvSonDakika.reloadData()
        }

        // Veri çek
        viewModel.loadKategoriler()
        viewModel.loadGundemHaberler()
        viewModel.loadSonDakikaHaberler()
    }


}

extension ViewController: UITableViewDelegate, UITableViewDataSource {

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return kategoriler.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "kategoriCell", for: indexPath)
        cell.textLabel?.text = kategoriler[indexPath.row].tur_adi
        return cell
    }
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let secilenHaber = collectionView == cvGundem ? gundemHaberler[indexPath.row] : sonDakikaHaberler[indexPath.row]

        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        if let detayVC = storyboard.instantiateViewController(withIdentifier: "HaberDetayVC") as? HaberDetayVC {
            detayVC.haber = secilenHaber
            navigationController?.pushViewController(detayVC, animated: true)
        }
    }


    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        if let kategoriVC = storyboard.instantiateViewController(withIdentifier: "KategoriVC") as? KategoriVC {
            kategoriVC.kategoriAd = kategoriler[indexPath.row].tur_adi
            navigationController?.pushViewController(kategoriVC, animated: true)
        }
    }

}
extension ViewController: UICollectionViewDelegate, UICollectionViewDataSource, HaberCellDelegate {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return collectionView == cvGundem ? gundemHaberler.count : sonDakikaHaberler.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        
        if collectionView == cvGundem {
            guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "gundemHaberCell", for: indexPath) as? HaberCellCollectionViewCell else {
                return UICollectionViewCell()
            }
            let haber = gundemHaberler[indexPath.row]
            cell.tvHaberBaslik.text = haber.baslik
            cell.tvDevaminiOku.text = "Devamını Oku"
            cell.delegate = self
            cell.haber = haber
            // video/fotoğraf kontrolü
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

        } else {
            guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "sonDakikaHaberCell", for: indexPath) as? HaberCellCollectionViewCell else {
                return UICollectionViewCell()
            }
            let haber = sonDakikaHaberler[indexPath.row]
            cell.tvHaberBaslik.text = haber.baslik
            print(haber.baslik)
            cell.tvDevaminiOku.text = "Devamını Oku"
            cell.delegate = self
            cell.haber = haber
            // video/fotoğraf kontrolü
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
    }


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
