//
//  ViewController.swift
//  CookieClickerSwift
//
//  Created by Alperen Saraç on 14.09.2025.
//

import UIKit

class ViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {

    @IBOutlet weak var flyingContainer: UIView!
    @IBOutlet weak var prestigeButton: UIButton!
    @IBOutlet weak var resetButton: UIButton!
    @IBOutlet weak var critButton: UIButton!
    @IBOutlet weak var bottomBar: UIView!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var cookieButton: UIButton!
    @IBOutlet weak var cpsLabel: UILabel!
    @IBOutlet weak var scoreLabel: UILabel!
    // Oyun durumu
        var s = Save.load()
    var tapPower: Int { baseTapWithPerk }

        // Crit
        let critMultiplier = 10
        let critCooldownSec = 30
        var critReady = true
        var critCooldownLeft = 0
        var critTimer: Timer?

        // Döngü (CPS)
        var tickTimer: CADisplayLink?
        let tickInterval: TimeInterval = 0.1
        var lastTick: CFTimeInterval = 0

        // Upgrade listesi
        var upgrades: [Upgrade] = [
            Upgrade(id: 1, title: "Otomatik Tıklayıcı", desc: "Saniyede +1", iconName: "bolt.fill", basePrice: 50, cpsGain: 1, tapGain: 0, level: 0, priceMultiplier: 1.15),
            Upgrade(id: 2, title: "Hızlı Karıştırıcı", desc: "Tıklama +1", iconName: "goforward", basePrice: 75, cpsGain: 0, tapGain: 1, level: 0, priceMultiplier: 1.15),
            Upgrade(id: 3, title: "Minik Fırın", desc: "Saniyede +5", iconName: "flame.fill", basePrice: 250, cpsGain: 5, tapGain: 0, level: 0, priceMultiplier: 1.15),
            Upgrade(id: 4, title: "Çikolata Parçaları", desc: "Tıklama +3", iconName: "square.grid.2x2.fill", basePrice: 300, cpsGain: 0, tapGain: 3, level: 0, priceMultiplier: 1.15),
            Upgrade(id: 5, title: "Pastane", desc: "Saniyede +25", iconName: "building.2.fill", basePrice: 1200, cpsGain: 25, tapGain: 0, level: 0, priceMultiplier: 1.15),
            Upgrade(id: 6, title: "Fabrika", desc: "Saniyede +120", iconName: "gearshape.2.fill", basePrice: 6000, cpsGain: 120, tapGain: 0, level: 0, priceMultiplier: 1.15),
            Upgrade(id: 7, title: "Araştırma Lab.", desc: "Tıklama +10", iconName: "testtube.2", basePrice: 8000, cpsGain: 0, tapGain: 10, level: 0, priceMultiplier: 1.15),
            Upgrade(id: 8, title: "Roket Fırın", desc: "Saniyede +750", iconName: "rocket.fill", basePrice: 42000, cpsGain: 750, tapGain: 0, level: 0, priceMultiplier: 1.15)
        ]

        override func viewDidLoad() {
            super.viewDidLoad()
            tableView.dataSource = self
            tableView.delegate = self

            // Cookie görseli
            cookieButton.setImage(UIImage(named: "cookie") ?? UIImage(systemName: "circle.fill"), for: .normal)

            // Kaydedilmiş upgrade seviyelerini yükle (UserDefaults’a basitçe id->level map saklayabilirsin; burada 0 bırakıyoruz)
            updateUI()

            // CPS döngüsü
            let link = CADisplayLink(target: self, selector: #selector(onDisplayLink(_:)))
            link.add(to: .main, forMode: .common)
            tickTimer = link
        }

        deinit {
            tickTimer?.invalidate()
            critTimer?.invalidate()
        }
    @objc private func onDisplayLink(_ link: CADisplayLink) {
        if lastTick == 0 { lastTick = link.timestamp; return }
        let delta = link.timestamp - lastTick
        if delta >= tickInterval {
            let effectiveCps = s.cps * totalMultiplier
            s.score += effectiveCps * delta
            lastTick = link.timestamp
            updateUI()
        }
    }


       private func updateUI() {
           scoreLabel.text = format(s.score)
              let effCps = s.cps * totalMultiplier
              cpsLabel.text = "\(format(effCps)) / s  (x\(String(format: "%.2f", totalMultiplier)))"
              tableView.reloadData()
              Save.save(s)
              updateCritUI()
       }

       private func format(_ v: Double) -> String {
           if v >= 1_000_000 { return String(format: "%.2fM", v/1_000_000) }
           if v >= 1_000 { return String(format: "%.1fk", v/1_000) }
           return String(format: "%.0f", v)
       }

       // MARK: - Actions

    @IBAction func shopTapped(_ sender: Any) {
        let sb = UIStoryboard(name: "Main", bundle: nil)
        let vc = sb.instantiateViewController(withIdentifier: "PrestigeShopViewController")
        navigationController?.pushViewController(vc, animated: true)
    }
    
    @IBAction func cookieButtonClicked(_ sender: UIButton) {
        UIView.animate(withDuration: 0.08, animations: {
               sender.transform = CGAffineTransform(scaleX: 0.95, y: 0.95)
           }) { _ in UIView.animate(withDuration: 0.12) { sender.transform = .identity } }

           var gain = Int(Double(tapPower) * totalMultiplier)
           // pasif crit %chance
           if passiveCritChance > 0 && Int.random(in: 0..<100) < passiveCritChance {
               gain *= 3
               let center = sender.superview!.convert(sender.center, to: flyingContainer)
               spawnFlyingText(text: "CRIT +\(gain)", at: center, isCrit: true)
           } else {
               let center = sender.superview!.convert(sender.center, to: flyingContainer)
               spawnFlyingText(text: "+\(gain)", at: center)
           }
           s.score += Double(gain)
           updateUI()
    }
    @IBAction func critTapped(_ sender: Any) {
        guard critReady else { return }
                critReady = false
                critCooldownLeft = critCooldownSec
                s.score += Double(tapPower * critMultiplier)
                let center = cookieButton.superview!.convert(cookieButton.center, to: flyingContainer)
                spawnFlyingText(text: "CRIT +\(tapPower * critMultiplier)", at: center, isCrit: true)
                updateUI()
                critTimer?.invalidate()
                critTimer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] t in
                    guard let self = self else { return }
                    self.critCooldownLeft -= 1
                    if self.critCooldownLeft <= 0 {
                        self.critReady = true
                        t.invalidate()
                    }
                    self.updateCritUI()
                }
    }
    @IBAction func prestigeTapped(_ sender: Any) {
        let gain = Int(sqrt(s.score / 1000.0))
            guard gain > 0 else { return }

            // mevcut perk store'u çek, puanı artır
            var ps = PerkSave.load()
            ps.points += gain
            PerkSave.save(ps)

            s.prestigeLevel += gain
            s.score = 0; s.cps = 0; s.extraTap = 0
            upgrades = upgrades.map { var m = $0; m.level = 0; return m }
            updateUI()
    }
    @IBAction func resetTapped(_ sender: Any) {
        s.score = 0; s.cps = 0; s.extraTap = 0
                upgrades = upgrades.map { u in var m = u; m.level = 0; return m }
                updateUI()
    }
    private func updateCritUI() {
           critButton.isEnabled = critReady
           let title = critReady ? "Crit" : "\(critCooldownLeft)s"
           critButton.setTitle(title, for: .normal)
       }

       // MARK: - Table

       func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { upgrades.count }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let u = upgrades[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: "UpgradeCell", for: indexPath) as! UpgradeCell

        cell.titleLabel.text = u.level > 0 ? "\(u.title) (Lv \(u.level))" : u.title
        cell.descLabel.text = u.desc
        cell.priceLabel.text = format(u.currentPrice())
        cell.iconImageView.image = UIImage(systemName: u.iconName) ?? UIImage(named: u.iconName)

        
        
        let raw = u.currentPrice()
        let price = raw * (1.0 - discountPct)
        cell.priceLabel.text = format(price)
        let can = s.score >= price
        cell.buyButton.isEnabled = can
        cell.buyButton.alpha = can ? 1.0 : 0.5


        // 🔧 ÖNEMLİ: indexPath'i yakala
        cell.onBuy = { [weak self] in
            self?.buy(at: indexPath)
        }

        return cell
    }


    private func buy(at indexPath: IndexPath) {
        var item = upgrades[indexPath.row]
        let price = item.currentPrice()
        guard s.score >= price else { return }

        s.score -= price
        item.level += 1
        s.cps += item.cpsGain
        s.extraTap += item.tapGain
        upgrades[indexPath.row] = item

        // sadece ilgili satırı yenilemek istersen:
        // tableView.reloadRows(at: [indexPath], with: .fade)
        updateUI()
    }

       // MARK: - Flying text

       private func spawnFlyingText(text: String, at point: CGPoint, isCrit: Bool = false) {
           let label = UILabel()
           label.text = text
           label.font = isCrit ? .boldSystemFont(ofSize: 26) : .boldSystemFont(ofSize: 22)
           label.textColor = isCrit ? UIColor.systemRed : UIColor.white
           label.layer.shadowColor = UIColor.black.cgColor
           label.layer.shadowOpacity = 0.5
           label.layer.shadowRadius = 4
           label.sizeToFit()
           label.center = point
           flyingContainer.addSubview(label)

           UIView.animate(withDuration: 0.6, animations: {
               label.center = CGPoint(x: point.x, y: point.y - 120)
               label.alpha = 0
           }, completion: { _ in
               label.removeFromSuperview()
           })
       }


   }
