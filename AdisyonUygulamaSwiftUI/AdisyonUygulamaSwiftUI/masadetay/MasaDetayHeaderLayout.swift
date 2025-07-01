//
//  MasaDetayHeaderLayout.swift
//  AdisyonUygulamaSwiftUI
//
//  Created by Alperen Saraç on 10.06.2025.
//

import Foundation
import SwiftUI

struct MasaDetayHeaderLayout: View {
    let masa: Masa
    let onBackPressed: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Button(action: onBackPressed) {
                Image("ic_remove")
                    .renderingMode(.original)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 24, height: 24)
            }
            Text("Masa \(masa.id)")
                .font(.system(size: 20, weight: .medium))
            Spacer()
        }
        .padding(.horizontal, 16)
        .frame(minHeight: 100)
        .background(Color.white)
    }
}

