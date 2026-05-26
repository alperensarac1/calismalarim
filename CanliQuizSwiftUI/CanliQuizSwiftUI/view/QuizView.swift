//
//  QuizView.swift
//  CanliQuizSwiftUI
//
//  Created by Alperen Saraç on 24.05.2026.
//

import Foundation
import SwiftUI

struct QuizView: View {

    @EnvironmentObject var viewModel: QuizViewModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                Text("Quiz")
                    .font(.system(size: 27, weight: .bold))

                if let question = viewModel.currentQuestion {
                    Text("Soru \(question.questionNumber) / \(question.totalQuestions)")
                        .foregroundColor(.gray)

                    Text(viewModel.remainingTime > 0 ? "Süre: \(viewModel.remainingTime)" : "Süre bitti")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(.red)

                    Text(question.questionText)
                        .font(.system(size: 21, weight: .bold))

                    ForEach(question.options.indices, id: \.self) { index in
                        Button {
                            viewModel.submitAnswer(index)
                        } label: {
                            Text("\(indexToLetter(index))) \(question.options[index])")
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(optionColor(index))
                                .foregroundColor(.black)
                                .cornerRadius(14)
                        }
                        .disabled(viewModel.answeredCurrentQuestion || viewModel.remainingTime <= 0)
                    }

                    Text(viewModel.answerResultText)
                        .font(.headline)

                    Text(viewModel.scoreboardText)
                } else {
                    Text("Soru bekleniyor...")
                }
            }
            .padding(24)
        }
        .navigationTitle("Quiz")
    }

    private func optionColor(_ index: Int) -> Color {
        if viewModel.correctAnswerIndex >= 0 && index == viewModel.correctAnswerIndex {
            return Color.green.opacity(0.25)
        }

        if viewModel.correctAnswerIndex >= 0 &&
            index == viewModel.selectedAnswerIndex &&
            index != viewModel.correctAnswerIndex {
            return Color.red.opacity(0.25)
        }

        if viewModel.answeredCurrentQuestion &&
            index == viewModel.selectedAnswerIndex &&
            viewModel.correctAnswerIndex == -1 {
            return Color.yellow.opacity(0.35)
        }

        return Color.white
    }
}
