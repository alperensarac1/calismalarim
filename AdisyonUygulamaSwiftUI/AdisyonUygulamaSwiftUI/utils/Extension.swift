//
//  Extension.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 6.06.2025.
//

import Foundation
import UIKit

extension UIView {

    func sizeBelirle(genislik: CGFloat = 0, yukseklik: CGFloat = 0) {
        translatesAutoresizingMaskIntoConstraints = false

        if genislik > 0 {
            widthAnchor.constraint(equalToConstant: genislik).isActive = true
        }
        if yukseklik > 0 {
            heightAnchor.constraint(equalToConstant: yukseklik).isActive = true
        }
    }


    func marginEkle(ust: CGFloat? = nil, sol: CGFloat? = nil, alt: CGFloat? = nil, sag: CGFloat? = nil) {
        guard let superview = superview else {
            assertionFailure("marginEkle çağrılmadan önce view’ın mutlaka süperview’a eklenmiş olması gerekir.")
            return
        }

        translatesAutoresizingMaskIntoConstraints = false

        if let u = ust {
            topAnchor.constraint(equalTo: superview.topAnchor, constant: u).isActive = true
        }
        if let l = sol {
            leadingAnchor.constraint(equalTo: superview.leadingAnchor, constant: l).isActive = true
        }
        if let a = alt {
            bottomAnchor.constraint(equalTo: superview.bottomAnchor, constant: -a).isActive = true
        }
        if let s = sag {
            trailingAnchor.constraint(equalTo: superview.trailingAnchor, constant: -s).isActive = true
        }
    }

    func paddingEkle(ust: CGFloat = 0, alt: CGFloat = 0, sol: CGFloat = 0, sag: CGFloat = 0) {
        directionalLayoutMargins = NSDirectionalEdgeInsets(
            top: ust,
            leading: sol,
            bottom: alt,
            trailing: sag
        )
    }

    func paddingEkleAll(_ hepsi: CGFloat = 0) {
        directionalLayoutMargins = NSDirectionalEdgeInsets(
            top: hepsi,
            leading: hepsi,
            bottom: hepsi,
            trailing: hepsi
        )
    }

    func paddingEkleHorizontal(_ yatay: CGFloat = 0) {
        let me = directionalLayoutMargins
        directionalLayoutMargins = NSDirectionalEdgeInsets(
            top: me.top,
            leading: yatay,
            bottom: me.bottom,
            trailing: yatay
        )
    }


    func paddingEkleVertical(_ dikey: CGFloat = 0) {
        let me = directionalLayoutMargins
        directionalLayoutMargins = NSDirectionalEdgeInsets(
            top: dikey,
            leading: me.leading,
            bottom: dikey,
            trailing: me.trailing
        )
    }



    static func dpToPx(_ dp: CGFloat) -> CGFloat {
        // iOS’ta 1 dp ≈ 1 pt, ancak px → pt dönüşümü için:
        return dp * UIScreen.main.scale
    }
}
