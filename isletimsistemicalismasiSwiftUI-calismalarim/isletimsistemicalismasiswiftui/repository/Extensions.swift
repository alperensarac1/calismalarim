//
//  Extensions.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 22.02.2025.
//

import Foundation
import UIKit
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
