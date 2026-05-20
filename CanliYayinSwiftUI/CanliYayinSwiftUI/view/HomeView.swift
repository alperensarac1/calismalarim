//
//  HomeView.swift
//  CanliYayinSwiftUI
//
//  Created by Alperen Saraç on 14.05.2026.
//

import Foundation
import SwiftUI

struct HomeView: View {
    var body: some View {
        VStack(spacing: 20) {
            Text("Canlı Yayın Uygulaması")
                .font(.title)
                .fontWeight(.bold)

            NavigationLink {
                BroadcasterView()
            } label: {
                Text("Yayın Aç")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)

            NavigationLink {
                RoomListView()
            } label: {
                Text("Yayınları İzle")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
        }
        .padding(24)
        .navigationTitle("Ana Sayfa")
    }
}
