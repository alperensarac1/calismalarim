//
//  ContentView.swift
//  HaberUygulamaSwiftUIi
//
//  Created by Alperen Saraç on 29.07.2025.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        NavigationView {
            HaberlerAnasayfa()
                .navigationTitle("Haberler")
        }
    }
}

