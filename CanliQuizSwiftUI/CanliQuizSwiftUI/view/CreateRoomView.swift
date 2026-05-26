//
//  CreateRoomView.swift
//  CanliQuizSwiftUI
//
//  Created by Alperen Saraç on 24.05.2026.
//

import Foundation
import SwiftUI

struct CreateRoomView: View {

    @EnvironmentObject var viewModel: QuizViewModel

    @State private var username = ""
    @State private var questionTimeText = "20"

    var body: some View {
        Form {
            Section("Oda Oluştur") {
                TextField("Kullanıcı adı", text: $username)

                TextField("Soru süresi", text: $questionTimeText)
                    .keyboardType(.numberPad)

                Button("Odayı Oluştur") {
                    let time = Int(questionTimeText) ?? 20
                    viewModel.createRoom(username: username, questionTime: time)
                }
            }

            if !viewModel.statusText.isEmpty {
                Text(viewModel.statusText)
                    .foregroundColor(.gray)
            }
        }
        .navigationTitle("Oda Oluştur")
    }
}
