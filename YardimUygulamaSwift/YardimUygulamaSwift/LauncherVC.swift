//
//  LauncherVC.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 1.03.2026.
//

import Foundation
import UIKit

final class LauncherVC: UIViewController {
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        if Session.isLoggedIn(), let role = Session.role() {
            if role == .YARDIMCI {
                goToHelper()
            } else {
                goToPatient()
            }
        } else {
            goToLogin()
        }
    }

    private func goToLogin() {
        let vc = storyboard!.instantiateViewController(withIdentifier: "LoginVC")
        navigationController?.setViewControllers([vc], animated: true)
    }

    private func goToPatient() {
        let vc = storyboard!.instantiateViewController(withIdentifier: "PatientVC")
        navigationController?.setViewControllers([vc], animated: true)
    }

    private func goToHelper() {
        let vc = storyboard!.instantiateViewController(withIdentifier: "HelperTabBarController")
        navigationController?.setViewControllers([vc], animated: true)
    }
}
