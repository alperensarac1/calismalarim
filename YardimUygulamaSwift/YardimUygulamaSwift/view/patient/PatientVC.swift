//
//  PatientVC.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 3.03.2026.
//

import UIKit

class PatientVC: UIViewController {


    @IBOutlet weak var lblLoc: UILabel!
    @IBOutlet weak var tfServis: UITextField!
    @IBOutlet weak var tfOda: UITextField!
    @IBOutlet weak var lblStatus: UILabel!
    @IBOutlet weak var btnConfirm: UIButton!
    @IBOutlet weak var btnCancel: UIButton!

    private let vm = PatientVM()
    private var timer: Timer?

    private var lat: Double?
    private var lng: Double?
    private var active: HelpActive?
    private var patientId: Int { Session.userId() }

    override func viewDidLoad() {
        super.viewDidLoad()
        lblLoc.text = "Konum: alınmadı"
        lblStatus.text = "Durum: -"
        btnConfirm.isHidden = true
        btnCancel.isHidden = true
        fetchLocation()
    }

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
        timer = Timer.scheduledTimer(withTimeInterval: 2.5, repeats: true) { [weak self] _ in
            self?.fetchActive()
        }
        fetchActive()
    }

    private func stopPolling() {
        timer?.invalidate()
        timer = nil
    }

    @IBAction func onLocation(_ sender: UIButton) {
        fetchLocation()
    }

    private func fetchLocation() {
        lblLoc.text = "Konum alınıyor..."
        LocationService.shared.requestOnce { [weak self] lat, lng, _, _ in
            guard let self else { return }
            self.lat = lat; self.lng = lng
            self.lblLoc.text = "Konum: \(lat), \(lng)"
        } onFailure: { [weak self] msg in
            self?.lblLoc.text = msg
        }
    }

    private func fetchActive() {
        Task { @MainActor in
            let a = await vm.myActive(patientId: patientId)
            active = a
            renderActive()
        }
    }

    private func renderActive() {
        guard let a = active else {
            lblStatus.text = "Durum: Aktif istek yok"
            btnConfirm.isHidden = true
            btnCancel.isHidden = true
            return
        }

        if a.status == "ACCEPTED" {
            let rem = a.remaining_seconds ?? 0
            lblStatus.text = "Durum: ACCEPTED (Kalan: \(TimeUtils.formatRemainingSeconds(rem)))"
            btnConfirm.isHidden = false
        } else {
            lblStatus.text = "Durum: \(a.status)"
            btnConfirm.isHidden = true
        }

        btnCancel.isHidden = !(a.status == "OPEN" || a.status == "ACCEPTED")
    }

    @IBAction func onSend(_ sender: UIButton) {
        let servis = (tfServis.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
         let oda = (tfOda.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)

         guard let la = lat, let lo = lng else {
             lblStatus.text = "Konum alınmadan gönderilemez."
             return
         }

         Task { @MainActor in
             let r = await vm.createHelp(
                 patientId: patientId,
                 servis: servis,
                 oda: oda,
                 lat: la,
                 lng: lo
             )

             switch r {
             case .success(let msg):
                 lblStatus.text = msg
                 fetchActive()

                 let vc = storyboard?.instantiateViewController(withIdentifier: "AcceptedVC") as! AcceptedVC
                 navigationController?.pushViewController(vc, animated: true)

             case .failure(let err):
                 lblStatus.text = err.localizedDescription
             }
         }
    }

    @IBAction func onConfirm(_ sender: UIButton) {
        guard let a = active else { return }
        Task { @MainActor in
            let r = await vm.confirm(requestId: a.id, patientId: patientId)
            switch r {
            case .success(let msg):
                lblStatus.text = msg
            case .failure(let err):
                lblStatus.text = err.localizedDescription
            }
            fetchActive()
        }
    }

    @IBAction func onCancel(_ sender: UIButton) {
        guard let a = active else { return }
        Task { @MainActor in
            let r = await vm.cancel(requestId: a.id, patientId: patientId)
            switch r {
            case .success(let msg):
                lblStatus.text = msg
            case .failure(let err):
                lblStatus.text = err.localizedDescription
            }
            fetchActive()
        }
    }

    @IBAction func onLogout(_ sender: UIButton) {
        Session.clear()
        let vc = storyboard!.instantiateViewController(withIdentifier: "LoginVC")
        navigationController?.setViewControllers([vc], animated: true)
    }

}
