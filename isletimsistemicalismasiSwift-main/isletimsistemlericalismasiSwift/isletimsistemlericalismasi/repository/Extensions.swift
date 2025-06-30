//
//  Extensions.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 25.01.2025.
//

import Foundation
import  UIKit

extension UIViewController {
    func performTransition(segue: UIStoryboardSegue, withIdentifier identifier: String, viewControllerType: UIViewController.Type) {
        if segue.identifier == identifier {
            if let destinationVC = segue.destination as? UIViewController {
                // Tam ekran sunum
                destinationVC.modalPresentationStyle = .fullScreen
                // Yumuşak geçiş animasyonu (cross dissolve)
                destinationVC.modalTransitionStyle = .crossDissolve
            }
        }
    }
        func setupPasswordToggle(for textField: UITextField, with imageView: UIImageView) {
            // UIImageView'in kullanıcı etkileşimini etkinleştir
            imageView.isUserInteractionEnabled = true
            
            // Gesture recognizer ekle
            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handlePasswordToggle(_:)))
            imageView.addGestureRecognizer(tapGesture)
            
            // Gesture ile ilişkilendirilecek TextField'i sakla
            objc_setAssociatedObject(imageView, &AssociatedKeys.textFieldKey, textField, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
        }

        @objc private func handlePasswordToggle(_ sender: UITapGestureRecognizer) {
            // ImageView'e bağlı TextField'i al
            guard let imageView = sender.view as? UIImageView,
                  let textField = objc_getAssociatedObject(imageView, &AssociatedKeys.textFieldKey) as? UITextField else { return }
            
            // Şifre alanını gizle/göster
            textField.isSecureTextEntry.toggle()
            
            // İkonu değiştir
            if textField.isSecureTextEntry {
                imageView.image = UIImage(systemName: "eye.slash")
            } else {
                imageView.image = UIImage(systemName: "eye")
            }
        }
    
    }

    // Associated Keys - TextField ile ImageView'i bağlamak için
    private struct AssociatedKeys {
        static var textFieldKey = "textFieldKey"
    }

extension UIApplication {
    func makeCall(to phoneNumber: String) {
        let cleanedNumber = phoneNumber.replacingOccurrences(of: " ", with: "")
        
        guard !cleanedNumber.isEmpty else {
            print("Contact number is not valid")
            return
        }
        
        guard let phoneCallURL = URL(string: "tel://\(cleanedNumber)"),
              let phonePromptURL = URL(string: "telprompt://\(cleanedNumber)") else {
            print("Invalid phone number format")
            return
        }
        
        if UIDevice.current.userInterfaceIdiom == .pad {
            print("Your device doesn't support this feature.")
            return
        }
        
        if self.canOpenURL(phonePromptURL) {
            self.open(phonePromptURL, options: [:], completionHandler: nil)
        } else if self.canOpenURL(phoneCallURL) {
            self.open(phoneCallURL, options: [:], completionHandler: nil)
        } else {
            print("Cannot make a call on this device.")
        }
    }
}

extension UIResponder {
    func getViewController() -> UIViewController? {
        var responder: UIResponder? = self
        while let nextResponder = responder?.next {
            responder = nextResponder
            if let viewController = responder as? UIViewController {
                return viewController
            }
        }
        return nil
    }
}
