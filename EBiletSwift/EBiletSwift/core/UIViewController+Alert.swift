//
//  UIViewController+Alert.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 25.06.2026.
//

import Foundation
import UIKit

/*
    UIViewController alert helper.

    Her ekranda tekrar tekrar UIAlertController yazmamak için
    ortak extension kullanıyoruz.
*/
extension UIViewController {

    func showAlert(
        title: String = "Uyarı",
        message: String,
        completion: (() -> Void)? = nil
    ) {
        let alert = UIAlertController(
            title: title,
            message: message,
            preferredStyle: .alert
        )

        let okAction = UIAlertAction(
            title: "Tamam",
            style: .default
        ) { _ in
            completion?()
        }

        alert.addAction(okAction)

        present(alert, animated: true)
    }
}
