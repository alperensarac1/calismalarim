//
//  Tarayici.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//

import SwiftUI

import SwiftUI
import WebKit

struct WebView: UIViewRepresentable {
    let url: URL
    
    func makeUIView(context: Context) -> WKWebView {
        return WKWebView()
    }
    
    func updateUIView(_ webView: WKWebView, context: Context) {
        let request = URLRequest(url: url)
        webView.load(request)
    }
}

struct Tarayici: View {
    @State private var searchText = ""
    @State private var searchURL: URL? = URL(string: "https://www.google.com")
    
    var body: some View {
        VStack {
            HStack {
                TextField("Arama yap...", text: $searchText)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .padding()
                
                Button(action: {
                    if let encodedQuery = searchText.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
                        searchURL = URL(string: "https://www.google.com/search?q=\(encodedQuery)")
                    }
                }) {
                    Image(systemName: "magnifyingglass")
                        .padding()
                        .background(Color.blue.opacity(0.2))
                        .cornerRadius(8)
                }
            }
            
            if let url = searchURL {
                WebView(url: url)
                    .edgesIgnoringSafeArea(.all)
            }
        }
    }
}




struct Tarayici_Previews: PreviewProvider {
    static var previews: some View {
        Tarayici()
    }
}
