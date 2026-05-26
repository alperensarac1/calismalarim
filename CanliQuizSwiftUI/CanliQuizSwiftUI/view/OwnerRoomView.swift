//
//  OwnerRoomView.swift
//  CanliQuizSwiftUI
//
//  Created by Alperen Saraç on 24.05.2026.
//

import SwiftUI

struct OwnerRoomView: View {

    @EnvironmentObject var viewModel: QuizViewModel

    @State private var questionText = ""
    @State private var options: [String] = ["", ""]
    @State private var selectedCorrectIndex = -1

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {

                headerSection

                Divider()

                questionSection

                optionsSection

                actionSection

                statusSection
            }
            .padding(24)
        }
        .navigationTitle("Oda")
    }

    // MARK: - Header Section

    private var headerSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Oda Sahibi Paneli")
                .font(.system(size: 27, weight: .bold))

            Text("Oda Kodu: \(viewModel.roomCode)")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.purple)

            Text("""
            Kullanıcı: \(viewModel.username)
            Soru Süresi: \(viewModel.questionTime) saniye
            """)
            .font(.system(size: 15))
            .foregroundColor(.secondary)

            Text(viewModel.playersText)
                .font(.system(size: 15))
                .foregroundColor(.primary)
        }
    }

    // MARK: - Question Section

    private var questionSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Soru Ekle")
                .font(.title2.bold())

            TextEditor(text: $questionText)
                .frame(height: 110)
                .padding(8)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(Color.gray.opacity(0.4))
                )
        }
    }

    // MARK: - Options Section

    private var optionsSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Şıklar")
                .font(.headline)

            ForEach(options.indices, id: \.self) { index in
                optionRow(index: index)
            }

            Button {
                options.append("")
            } label: {
                Text("+ Şık Ekle")
                    .frame(maxWidth: .infinity)
                    .frame(height: 44)
            }
            .buttonStyle(.bordered)
        }
    }

    private func optionRow(index: Int) -> some View {
        HStack {
            Button {
                selectedCorrectIndex = index
            } label: {
                Image(
                    systemName: selectedCorrectIndex == index
                    ? "largecircle.fill.circle"
                    : "circle"
                )
                .foregroundColor(.purple)
            }

            TextField(
                "Şık \(index + 1)",
                text: Binding(
                    get: {
                        if options.indices.contains(index) {
                            return options[index]
                        } else {
                            return ""
                        }
                    },
                    set: { newValue in
                        if options.indices.contains(index) {
                            options[index] = newValue
                        }
                    }
                )
            )
            .textFieldStyle(.roundedBorder)

            Button {
                deleteOption(at: index)
            } label: {
                Text("Sil")
            }
            .disabled(options.count <= 2)
        }
    }

    // MARK: - Action Section

    private var actionSection: some View {
        VStack(spacing: 12) {
            Button {
                addQuestion()
            } label: {
                Text("Soruyu Ekle")
                    .frame(maxWidth: .infinity)
                    .frame(height: 52)
            }
            .buttonStyle(.borderedProminent)

            Button {
                viewModel.startQuiz()
            } label: {
                Text("Quizi Başlat")
                    .frame(maxWidth: .infinity)
                    .frame(height: 52)
            }
            .buttonStyle(.borderedProminent)
        }
    }

    // MARK: - Status Section

    private var statusSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Eklenen soru: \(viewModel.questionCount)")
                .font(.system(size: 15))

            Text(viewModel.statusText)
                .font(.system(size: 15))
                .foregroundColor(.gray)
        }
    }

    // MARK: - Helper Functions

    private func addQuestion() {
        viewModel.addQuestion(
            questionText: questionText,
            options: options,
            selectedCorrectIndex: selectedCorrectIndex
        )

        /*
            Basit kullanım için formu temizliyoruz.

            Daha profesyonel kullanımda:
            Sadece server "question_added" dönerse temizlemek daha doğru olur.
        */
        questionText = ""
        options = ["", ""]
        selectedCorrectIndex = -1
    }

    private func deleteOption(at index: Int) {
        guard options.count > 2 else {
            return
        }

        options.remove(at: index)

        if selectedCorrectIndex == index {
            selectedCorrectIndex = -1
        } else if selectedCorrectIndex > index {
            selectedCorrectIndex -= 1
        }
    }
}
