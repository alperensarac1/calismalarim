import UIKit

final class ShipmentDetailViewController: UIViewController {

    // MARK: - Outlets
    @IBOutlet private weak var lblTitle: UILabel!
    @IBOutlet private weak var lblSub: UILabel!

    @IBOutlet private weak var lblStatusValue: UILabel!
    @IBOutlet private weak var lblCompany: UILabel!

    @IBOutlet private weak var cardCode: UIView!
    @IBOutlet private weak var lblCodeValue: UILabel!
    @IBOutlet private weak var lblExpires: UILabel!
    @IBOutlet private weak var btnCopyCode: UIButton!

    @IBOutlet private weak var lblRole: UILabel!
    @IBOutlet private weak var lblReceiverAddrTitle: UILabel!
    @IBOutlet private weak var lblVisibilityHint: UILabel!

    var shipment: Shipment!

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Detay"
        styleCard(cardCode)
        bind()
    }

    private func styleCard(_ v: UIView?) {
        guard let v else { return }
        v.layer.cornerRadius = 16
        v.clipsToBounds = true
    }

    private func bind() {
        guard shipment != nil else { return }
        
        lblTitle.text = "Gönderi Detayı"
        lblSub.text = "ID: #\(shipment.id) • Rol: \(shipment.role)"
        
        lblStatusValue.text = shipment.status
        
        if let name = shipment.cargo_company_name, !name.isEmpty {
            lblCompany.isHidden = false
            lblCompany.text = "Kargo: \(name)"
        } else {
            lblCompany.isHidden = true
        }
        
        lblRole.text = "Rol: \(shipment.role)"
        
        // Status’a göre kod gösterimi (istersen değiştirebilirsin)
        let statusUpper = shipment.status.uppercased()
        let shouldHideCodeForStatus = ["CANCELLED", "EXPIRED"].contains(statusUpper)
        
        if shipment.role == "RECEIVER" {
            if shipment.visible == false {
                // CREATED iken receiver’a gizli (backend kuralın)
                hideCodeCard(reason: "Bu gönderi henüz görünür değil.")
                lblReceiverAddrTitle.isHidden = true
                return
            }
            
            if shouldHideCodeForStatus {
                hideCodeCard(reason: "Bu gönderi için kod aktif değil.")
            } else {
                showCodeCard()
            }
            
            // Receiver kendi receiver_address_title görebilir (visible ise)
            if let t = shipment.receiver_address_title, !t.isEmpty {
                lblReceiverAddrTitle.isHidden = false
                lblReceiverAddrTitle.text = "Adres: \(t)"
            } else {
                lblReceiverAddrTitle.isHidden = true
            }
            
        } else {
            // Sender tarafı
            if shouldHideCodeForStatus {
                hideCodeCard(reason: "Bu gönderi için kod aktif değil.")
            } else {
                showCodeCard()
            }
            
            // Sender alıcı adresini görmez
            lblReceiverAddrTitle.isHidden = true
        }
    }

    private func showCodeCard() {
         lblVisibilityHint.isHidden = true
         cardCode.isHidden = false
         lblCodeValue.text = shipment.pickup_code
         lblExpires.text = "Son geçerlilik: \(shipment.code_expires_at)"
         btnCopyCode.isEnabled = true
     }

     private func hideCodeCard(reason: String) {
         cardCode.isHidden = true
         lblVisibilityHint.isHidden = false
         lblVisibilityHint.text = reason
         btnCopyCode.isEnabled = false
     }
    @IBAction private func copyCodeTapped(_ sender: UIButton) {
        guard cardCode.isHidden == false else { return }
                let code = shipment.pickup_code
                UIPasteboard.general.string = code

                let a = UIAlertController(title: "Kopyalandı", message: "Kod panoya kopyalandı: \(code)", preferredStyle: .alert)
                a.addAction(UIAlertAction(title: "Tamam", style: .default))
                present(a, animated: true)
    }
        
}
