//
//  ImageFullScreen.swift
//  MemeShareSwiftUI
//
//  Created by Alperen Saraç on 2.09.2025.
//

import Foundation
import SwiftUI

struct ImageFullScreen: View, Identifiable {
    let id = UUID()
    let url: URL
    let onClose: () -> Void

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            RemoteImageView(url: url)
                .scaledToFit()
                .ignoresSafeArea()
            VStack {
                HStack {
                    Spacer()
                    Button {
                        onClose()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 28))
                            .foregroundStyle(.white)
                            .shadow(radius: 4)
                            .padding()
                    }
                }
                Spacer()
            }
        }
    }
}
