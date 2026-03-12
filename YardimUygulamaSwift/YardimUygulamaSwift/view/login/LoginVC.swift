//
//  LoginVC.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 1.03.2026.
//

import Foundation
import UIKit

final class LoginVC: UIViewController {

    @IBOutlet weak var tfPhone: UITextField!
    @IBOutlet weak var tfPass: UITextField!
    @IBOutlet weak var lblInfo: UILabel!
    @IBOutlet weak var btnLogin: UIButton!

    private let vm = LoginVM()

    @IBAction func onLogin(_ sender: UIButton) {
        lblInfo.text = ""
        btnLogin.isEnabled = false

        Task { @MainActor in
            let result = await vm.login(phone: tfPhone.text ?? "", pass: tfPass.text ?? "")
            btnLogin.isEnabled = true

            switch result {
            case .success(let role):
                if role == .YARDIMCI {
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

    @IBAction func onGoRegister(_ sender: UIButton) {
        let vc = storyboard!.instantiateViewController(withIdentifier: "RegisterVC")
        navigationController?.pushViewController(vc, animated: true)
    }
}
