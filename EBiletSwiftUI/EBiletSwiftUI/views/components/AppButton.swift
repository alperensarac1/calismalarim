//
//  AppButton.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import SwiftUI

/*
    AppButton

    Projede tekrar kullanacağımız ana buton componentidir.

    Özellikler:
    - Loading durumunda butonu pasifleştirir.
    - Loading sırasında ProgressView gösterir.
    - Renk dışarıdan verilebilir.
*/
struct AppButton: View {

    let title: String
    let backgroundColor: Color
    let isLoading: Bool
    let action: () -> Void

    init(
        title: String,
        backgroundColor: Color = .blue,
        isLoading: Bool = false,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.backgroundColor = backgroundColor
        self.isLoading = isLoading
        self.action = action
    }

    var body: some View {
        Button {
            action()
        } label: {
            ZStack {
                if isLoading {
                    ProgressView()
                        .tint(.white)
                } else {
                    Text(title)
                        .font(.headline)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(isLoading ? backgroundColor.opacity(0.7) : backgroundColor)
            .foregroundStyle(.white)
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
        .disabled(isLoading)
    }
}
