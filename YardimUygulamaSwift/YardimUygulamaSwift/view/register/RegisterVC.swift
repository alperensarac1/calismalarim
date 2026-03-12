//
//  RegisterVC.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 1.03.2026.
//

import Foundation
import UIKit

final class RegisterVC: UIViewController {

    @IBOutlet weak var segRole: UISegmentedControl!
    @IBOutlet weak var tfAd: UITextField!
    @IBOutlet weak var tfSoyad: UITextField!
    @IBOutlet weak var tfYas: UITextField!
    @IBOutlet weak var tfTelefon: UITextField!
    @IBOutlet weak var tfSifre: UITextField!

    @IBOutlet weak var lblLocation: UILabel!
    @IBOutlet weak var lblInfo: UILabel!
    @IBOutlet weak var btnRegister: UIButton!

    private let vm = RegisterVM()
    private var city: String?
    private var district: String?
    private var lastLat: Double?
    private var lastLng: Double?

    override func viewDidLoad() {
        super.viewDidLoad()
        lblLocation.text = "Konum: tespit edilmedi"
        lblInfo.text = ""
        fetchLocation()
    }

    @IBAction func onLocation(_ sender: UIButton) {
        fetchLocation()
    }

    private func fetchLocation() {
        lblLocation.text = "Konum alınıyor..."
        LocationService.shared.requestOnce { [weak self] lat, lng, c, d in
            guard let self else { return }
            self.lastLat = lat; self.lastLng = lng
            self.city = c; self.district = d
            self.lblLocation.text = (c != nil && d != nil) ? "Tespit edilen: \(c!) / \(d!)" : "Şehir/ilçe tespit edilemedi"
        } onFailure: { [weak self] msg in
            self?.lblLocation.text = msg
        }
    }

    @IBAction func onRegister(_ sender: UIButton) {
        lblInfo.text = ""

        let role: Role = (segRole.selectedSegmentIndex == 1) ? .YARDIMCI : .HASTA
        let ad = (tfAd.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let soyad = (tfSoyad.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let telefon = (tfTelefon.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let sifre = tfSifre.text ?? ""
        let yasInt = Int((tfYas.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines))

        guard !ad.isEmpty, !soyad.isEmpty, !telefon.isEmpty, !sifre.isEmpty else {
            lblInfo.text = "Ad, soyad, telefon, şifre zorunlu"
            return
        }
        guard let c = city, let d = district, !c.isEmpty, !d.isEmpty else {
            lblInfo.text = "Şehir/ilçe tespit edilemedi (Konumdan Al)"
            return
        }

        btnRegister.isEnabled = false
        Task { @MainActor in
            let body = RegisterBody(role: role, ad: ad, soyad: soyad, yas: yasInt, telefon: telefon, il: c, ilce: d, sifre: sifre)
            let result = await vm.register(body: body)
            btnRegister.isEnabled = true

            switch result {
            case .success(let r):
                if r == .YARDIMCI {
                    let vc = storyboard!.instantiateViewController(withIdentifier: "HelperTabBarController")
                    navigationController?.setViewControllers([vc], animated: true)
                } else {
                    let vc = storyboard!.instantiateViewController(withIdentifier: "PatientVC")
                    navigationController?.setViewControllers([vc], animated: true)
                }
            case .failure(let msg):
                lblInfo.text = msg.localizedDescription
            }
        }
    }
}
