import Foundation
import UIKit

class StudentSetupVC:UIViewController{
    @IBOutlet weak var etStudentNo: UITextField!
    @IBOutlet weak var btnSave: UIButton!
    override func viewDidLoad() {
        super.viewDidLoad()
        if Prefs.shared.hasStudentNo() {
                   goScan()
        }
    }
    @IBAction func btnSaveTapped(_ sender: Any) {
        let no = etStudentNo.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
               guard !no.isEmpty else {
                   etStudentNo.becomeFirstResponder()
                   return
               }
               Prefs.shared.setStudentNo(no)
               goScan()
    }
    private func goScan() {
           let vc = storyboard!.instantiateViewController(withIdentifier: "ScanVC")
           vc.modalPresentationStyle = .fullScreen
           present(vc, animated: true)
       }
}
