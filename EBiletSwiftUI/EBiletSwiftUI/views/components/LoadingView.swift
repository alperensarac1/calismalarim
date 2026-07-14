//
//  LoadingView.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import SwiftUI

/*
    LoadingView

    Küçük yükleniyor componenti.
*/
struct LoadingView: View {

    let text: String

    init(text: String = "Yükleniyor...") {
        self.text = text
    }

    var body: some View {
        HStack(spacing: 10) {
            ProgressView()

            Text(text)
                .foregroundStyle(.secondary)
        }
        .padding()
    }
}
