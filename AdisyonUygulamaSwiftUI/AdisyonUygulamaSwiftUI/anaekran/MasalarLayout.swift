//
//  MasalarLayout.swift
//  AdisyonUygulamaSwiftUI
//
//  Created by Alperen Saraç on 9.06.2025.
//

import Foundation
import SwiftUI


import SwiftUI

struct MasalarLayout: View {
    let masaList: [Masa]
    let onMasaClick: (Masa) -> Void

    private let columns: [GridItem] = Array(
        repeating: .init(.flexible(), spacing: 10),
        count: 3
    )

    var body: some View {
        ScrollView {
            LazyVGrid(columns: columns, spacing: 10) {
                ForEach(masaList) { masa in
                    MasaCard(masa: masa, onClick: onMasaClick)
                        .frame(
                            maxWidth: .infinity,
                            minHeight: UIScreen.main.bounds.width / 3 * 1.2
                        )
                }
            }
            .padding(10)
        }
    }
}
