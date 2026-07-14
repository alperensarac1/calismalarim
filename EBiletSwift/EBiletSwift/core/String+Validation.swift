//
//  String+Validation.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 25.06.2026.
//

import Foundation

/*
    String validation helper.

    E-posta kontrolünü Login ve Register ekranlarında kullanıyoruz.
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
