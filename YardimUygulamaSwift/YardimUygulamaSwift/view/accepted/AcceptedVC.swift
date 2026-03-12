//
//  AcceptedVC.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 9.03.2026.
//

import Foundation
import UIKit

final class AcceptedVC: UIViewController {

    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var lblPhone: UILabel!
    @IBOutlet weak var lblService: UILabel!
    @IBOutlet weak var lblRoom: UILabel!
    @IBOutlet weak var lblRemain: UILabel!

    private let vm = AcceptedVM()
    private var timer: Timer?
    private var item: AcceptedHelpItem?
    private var helperId: Int { Session.userId() }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        startPolling()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        stopPolling()
    }

    private func startPolling() {
        stopPolling()
        timer = Timer.scheduledTimer(withTimeInterval: 2.0, repeats: true) { [weak self] _ in
            self?.fetch()
        }
        fetch()
    }

    private func stopPolling() {
        timer?.invalidate()
        timer = nil
    }

    private func fetch() {
        Task { @MainActor in
            item = await vm.fetchAccepted(helperId: helperId)
            render()
        }
    }

    private func render() {
        guard let a = item else {
            lblName.text = "Aktif kabul yok"
            lblPhone.text = "-"
            lblService.text = "-"
            lblRoom.text = "-"
            lblRemain.text = "-"
            return
        }
        lblName.text = "Hasta: \(a.patient_name ?? "-") (\(a.patient_age ?? 0))"
        lblPhone.text = "Telefon: \(a.patient_phone ?? "-")"
        lblService.text = "Servis: \(a.servis_adi ?? "-")"
        lblRoom.text = "Oda: \(a.oda_no ?? "-")"
        lblRemain.text = "Kalan: \(TimeUtils.formatRemainingSeconds(a.remaining_seconds ?? 0))"
    }

    @IBAction func onCall(_ sender: UIButton) {
        guard let p = item?.patient_phone, !p.isEmpty,
              let url = URL(string: "tel:\(p)") else { return }
        UIApplication.shared.open(url)
    }

    @IBAction func onMap(_ sender: UIButton) {
        guard let a = item else { return }
        let ll = "\(a.lat),\(a.lng)"
        let label = "Hasta Konumu".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "Konum"
        let urlStr = "http://maps.apple.com/?q=\(label)&ll=\(ll)"
        guard let url = URL(string: urlStr) else { return }
        UIApplication.shared.open(url)
    }
}
