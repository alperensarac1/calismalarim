//
//  WaitingRoomView.swift
//  CanliQuizSwiftUI
//
//  Created by Alperen Saraç on 24.05.2026.
//

import Foundation
import SwiftUI

struct WaitingRoomView: View {

    @EnvironmentObject var viewModel: QuizViewModel

    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            Text("Bekleme Odası")
                .font(.system(size: 28, weight: .bold))

            Text("""
            Kullanıcı: \(viewModel.username)
            Oda Kodu: \(viewModel.roomCode)
            Soru Süresi: \(viewModel.questionTime) saniye

            Oda sahibi quizi başlatınca sorular ekrana gelecek.
            """)

            Text(viewModel.statusText)
                .foregroundColor(.purple)
                .bold()

            Text(viewModel.playersText)

            Spacer()
        }
        .padding(24)
        .navigationTitle("Bekleme")
    }
}
