//
//  String+Validation.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
/*
    E-posta kontrol helper'ı.
*/
extension String {

    var isValidEmail: Bool {
        let pattern = #"^\S+@\S+\.\S+$"#

        return range(
            of: pattern,
            options: .regularExpression
        ) != nil
    }
}
