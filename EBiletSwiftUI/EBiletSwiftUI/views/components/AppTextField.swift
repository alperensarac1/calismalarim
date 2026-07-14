//
//  AppTextField.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import SwiftUI

/*
    AppTextField

    Projede ortak kullanacağımız text input componentidir.

    isSecure true ise:
        SecureField kullanır.

    isSecure false ise:
        TextField kullanır.
*/
struct AppTextField: View {

    let title: String
    @Binding var text: String
    var isSecure: Bool = false
    var keyboardType: UIKeyboardType = .default

    var body: some View {
        Group {
            if isSecure {
                SecureField(title, text: $text)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled(true)
            } else {
                TextField(title, text: $text)
                    .keyboardType(keyboardType)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled(true)
            }
        }
        .padding(.horizontal, 14)
        .frame(height: 52)
        .background(Color(red: 238 / 255, green: 242 / 255, blue: 255 / 255))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}
