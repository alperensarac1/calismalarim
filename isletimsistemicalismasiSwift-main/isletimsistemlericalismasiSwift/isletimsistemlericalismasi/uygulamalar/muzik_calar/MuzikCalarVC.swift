import UIKit
import AVFoundation

class MuzikCalarVC: UIViewController {
    
    var muzikCalar: AVAudioPlayer?
    var muzikler = ["disfigure","shine", "vidya"]
    var muzikIndex: Int = 0

    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var tvMuzikAd: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // AVAudioSession yapılandırması
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: [])
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("⚠️ AVAudioSession Hatası: \(error.localizedDescription)")
        }

        // TableView bağlamaları
        tableView.delegate = self
        tableView.dataSource = self
        tableView.reloadData()

        // Müzik çalmayı başlat
        DispatchQueue.main.async {
            self.muzikOynat()
        }
    }
    
    @IBAction func musicButtons(_ sender: UIButton) {
        if sender.tag == 0 {  // Önceki şarkı
            if muzikIndex > 0 {
                muzikIndex -= 1
                muzikOynat()
            }
        }
        if sender.tag == 1 {  // Durdur
            muzikCalar?.stop()
            muzikCalar?.currentTime = 0
        }
        if sender.tag == 2 {  // Oynat
            muzikCalar?.play()
        }
        if sender.tag == 3 {  // Duraklat
            muzikCalar?.pause()
        }
        if sender.tag == 4 {  // Sonraki şarkı
            if muzikIndex < muzikler.count - 1 {
                muzikIndex += 1
                muzikOynat()
            }
        }
    }

    func muzikOynat() {
        if let dosyaURL = Bundle.main.url(forResource: muzikler[muzikIndex], withExtension: "mp3") {
            do {
                muzikCalar = try AVAudioPlayer(contentsOf: dosyaURL)
                muzikCalar?.prepareToPlay()
                muzikCalar?.play()
                tvMuzikAd.text = muzikler[muzikIndex]
            } catch {
                print("⚠️ Müzik oynatılamadı: \(error.localizedDescription)")
            }
        } else {
            print("❌ Dosya bulunamadı: \(muzikler[muzikIndex]).mp3")
        }
    }
}

extension MuzikCalarVC: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return muzikler.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "muzikHucre", for: indexPath)
        cell.textLabel?.text = muzikler[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        muzikCalar?.stop()
        muzikIndex = indexPath.row
        muzikOynat()
    }
}
