//
//  Extensions.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import Foundation
import UIKit
extension UIViewController {
    func showAlert(title: String, message: String?) {
        let ac = UIAlertController(title: title, message: message, preferredStyle: .alert)
        ac.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(ac, animated: true)
    }
}
extension String {
    var asTrDate: String {
        let inFmt = DateFormatter()
        inFmt.locale = Locale(identifier: "tr_TR")
        inFmt.dateFormat = "yyyy-MM-dd HH:mm:ss"
        let outFmt = DateFormatter()
        outFmt.locale = Locale(identifier: "tr_TR")
        outFmt.dateFormat = "dd.MM.yyyy"
        if let d = inFmt.date(from: self) { return outFmt.string(from: d) }
        // saat yoksa sadece ilk 10 karakterden dene
        let short = String(self.prefix(10))
        inFmt.dateFormat = "yyyy-MM-dd"
        if let d = inFmt.date(from: short) { return outFmt.string(from: d) }
        return short
    }
}

protocol SegmentedNavigatable where Self: UIViewController {
    var segmentIndex: Int { get }
}
extension UIViewController {
    func navigateToSegment(index: Int) {
        // Aynı ekrandaysak gitme
        if let current = self as? SegmentedNavigatable, current.segmentIndex == index { return }

        let sb = UIStoryboard(name: "Main", bundle: nil)
        let next: UIViewController
        switch index {
        case 0:
            next = sb.instantiateViewController(withIdentifier: "GundemVC") as! GundemVC
        case 1:
            next = sb.instantiateViewController(withIdentifier: "BugunVC") as! BugunVC
        case 2:
            next = sb.instantiateViewController(withIdentifier: "ProfilVC") as! ProfilVC
        default:
            return
        }

        // Aynı nav içinde stack'i tek ekrana indirip değiştir
        if let nav = self.navigationController {
            nav.setViewControllers([next], animated: false)
        } else {
            // Nav yoksa modally sun (genelde nav vardır)
            present(next, animated: false)
        }
    }
}
