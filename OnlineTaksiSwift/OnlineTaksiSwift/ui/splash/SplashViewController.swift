//
//  SplashViewController.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation
import UIKit

final class SplashViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            self.routeUser()
        }
    }

    private func routeUser() {
        if SessionManager.shared.isLoggedIn {
            if SessionManager.shared.role == "driver" {
                openController(storyboardId: "DriverHomeVC")
            } else {
                openController(storyboardId: "CustomerHomeVC")
            }
        } else {
            openController(storyboardId: "LoginVC")
        }
    }

    private func openController(storyboardId: String) {
        let vc = storyboard!.instantiateViewController(withIdentifier: storyboardId)
        vc.modalPresentationStyle = .fullScreen
        present(vc, animated: true)
    }
}
