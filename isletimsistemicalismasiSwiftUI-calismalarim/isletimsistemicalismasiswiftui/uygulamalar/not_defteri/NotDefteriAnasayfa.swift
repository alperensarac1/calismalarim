//
//  NotDefteriAnasayfa.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//

import SwiftUI

struct NotDefteriAnasayfa: View {
    @StateObject private var viewModel = NotDefteriViewModel()
    @State private var secilenNot: Not?

    var body: some View {
        NavigationView {
            ScrollView {
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                    ForEach(viewModel.notlar) { not in
                        NotKart(not: not)
                            .onTapGesture {
                                secilenNot = not
                            }
                            .onLongPressGesture {
                                viewModel.notSil(not: not)
                            }
                    }
                }
                .padding()
            }
            .navigationTitle("Not Defteri")
            .toolbar {
                Button(action: {
                    secilenNot = Not()
                    secilenNot?.not_baslik = ""
                    secilenNot?.not_icerik = ""
                }) {
                    Image(systemName: "plus")
                }
            }
            .sheet(item: $secilenNot) { not in
                NotDefteriDetaysayfa(viewModel: viewModel, not: not)
            }
        }
    }
}

struct NotKart: View {
    var not: Not

    var body: some View {
        VStack(alignment: .leading) {
            Text(not.not_baslik!)
                .font(.headline)
                .lineLimit(1)
            Text(not.not_icerik!)
                .font(.subheadline)
                .lineLimit(3)
                .foregroundColor(.gray)
        }
        .padding()
        .frame(width: 150, height: 100)
        .background(Color.yellow.opacity(0.3))
        .cornerRadius(10)
        .shadow(radius: 3)
    }
}

