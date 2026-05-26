//
//  WinnerView.swift
//  CanliQuizSwiftUI
//
//  Created by Alperen Saraç on 24.05.2026.
//

import Foundation
import SwiftUI

struct WinnerView: View {

    @EnvironmentObject var viewModel: QuizViewModel

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                Text("Quiz Bitti")
                    .font(.system(size: 32, weight: .bold))

                Text("Bunlar Kazandı")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.purple)

                Text(viewModel.winnersText)
                    .font(.system(size: 20, weight: .bold))
                    .multilineTextAlignment(.center)

                Text(viewModel.finalScoreboardText)
                    .font(.system(size: 15))
                    .frame(maxWidth: .infinity, alignment: .leading)

                Button("Ana Sayfaya Dön") {
                    viewModel.disconnectAndHome()
                }
            }
            .padding(24)
        }
        .navigationTitle("Sonuç")
    }
}
