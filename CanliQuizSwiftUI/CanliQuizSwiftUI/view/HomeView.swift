//
//  HomeView.swift
//  CanliQuizSwiftUI
//
//  Created by Alperen Saraç on 24.05.2026.
//

import Foundation
import SwiftUI

struct HomeView: View {

    @EnvironmentObject var viewModel: QuizViewModel

    var body: some View {
        VStack(spacing: 16) {
            Text("Canlı Quiz")
                .font(.system(size: 34, weight: .bold))

            Text("Oda oluştur veya oda kodu ile quize katıl.")
                .font(.system(size: 16))
                .foregroundColor(.gray)

            Spacer().frame(height: 24)

            Button("Oda Oluştur") {
                viewModel.openCreateRoom()
            }

            Button("Odaya Giriş Yap") {
                viewModel.openJoinRoom()
            }
        }
        .padding(24)
        .navigationTitle("")
    }
}
