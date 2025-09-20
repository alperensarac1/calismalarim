//
//  PrestigeShopViewController.swift
//  CookieClickerSwift
//
//  Created by Alperen Saraç on 15.09.2025.
//

import UIKit

class PrestigeShopViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {
    
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var pointsLabel: UILabel!
    var perksState = PerkSave.load()     // mevcut puan ve seviyeler
    var items: [PrestigePerk] = []        // tabloya bağlanacak modeller
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Prestige Mağazası"
        tableView.dataSource = self
        tableView.delegate = self
        
        // mevcut seviyeleri state'den doldur
        items = [
            PrestigePerk(key: "gprod", title: "Altın Çırpıcı",
                         desc: "%5 üretim çarpanı / seviye (CPS & tap)",
                         baseCost: 1, costScaling: 1.6, level: perksState.gprod, maxLevel: .max),
            PrestigePerk(key: "crit", title: "Uğurlu Tılsım",
                         desc: "%1 pasif crit şansı / seviye (tap x3)",
                         baseCost: 2, costScaling: 1.7, level: perksState.crit, maxLevel: .max),
            PrestigePerk(key: "discount", title: "Toplu Alım",
                         desc: "Upgrade fiyatlarında %2 indirim / seviye (maks %50)",
                         baseCost: 3, costScaling: 1.8, level: perksState.discount, maxLevel: 25),
            PrestigePerk(key: "tapTop", title: "Turbo Tap",
                         desc: "Kalıcı +1 tap gücü / seviye",
                         baseCost: 2, costScaling: 1.5, level: perksState.tapTop, maxLevel: .max),
        ]
        updateHeader()
    }
    
    private func updateHeader() {
        pointsLabel.text = "Prestige Puanı: \(perksState.points)"
    }
    
    func tableView(_ tv: UITableView, numberOfRowsInSection section: Int) -> Int { items.count }
    
    func tableView(_ tv: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let p = items[indexPath.row]
        let cell = tv.dequeueReusableCell(withIdentifier: "PerkCell", for: indexPath) as! PerkCell
        cell.titleLabel.text = p.title
        cell.descLabel.text = p.desc
        cell.metaLabel.text = "Lv \(p.level) • Maliyet: \(p.costForNext())"
        let canAfford = perksState.points >= p.costForNext() && p.level < p.maxLevel
        cell.buyButton.isEnabled = canAfford
        cell.buyButton.alpha = canAfford ? 1.0 : 0.5
        cell.onBuy = { [weak self] in self?.buy(indexPath) }
        return cell
    }
    
    private func buy(_ indexPath: IndexPath) {
        var p = items[indexPath.row]
        let cost = p.costForNext()
        guard perksState.points >= cost, p.level < p.maxLevel else { return }
        perksState.points -= cost
        p.level += 1
        // state'e yaz
        switch p.key {
        case "gprod": perksState.gprod = p.level
        case "crit": perksState.crit = p.level
        case "discount": perksState.discount = p.level
        case "tapTop": perksState.tapTop = p.level
        default: break
        }
        items[indexPath.row] = p
        PerkSave.save(perksState)
        updateHeader()
        tableView.reloadRows(at: [indexPath], with: .none)
        // ana ekrana döndüğünde ViewController perks'i tekrar okuyacak
    }
}

// Perk yardımcı hesaplamalar
extension ViewController {
    var perkStore: PerkStore { PerkSave.load() }

    var totalMultiplier: Double {
        // prestigeLevel başına %10 + gprod başına %5
        let prestige = 1.0 + (Double(s.prestigeLevel) * 0.10)
        let gprod    = 1.0 + (Double(perkStore.gprod) * 0.05)
        return prestige * gprod
    }

    var discountPct: Double {
        min(Double(perkStore.discount) * 0.02, 0.50) // tavan %50
    }

    var passiveCritChance: Int {
        perkStore.crit // %1/level
    }

    var baseTapWithPerk: Int {
        s.baseTap + s.extraTap + perkStore.tapTop
    }
}
