//
//  ProductDetailView.swift
//  EticaretSwiftUI
//
//  Created by Alperen Saraç on 18.01.2026.
//

import Foundation
import SwiftUI

struct ProductDetailView: View {
    let productId: Int
    @StateObject private var vm = ProductDetailViewModel()
    @EnvironmentObject private var auth: AuthManager
    @State private var showLogin = false

    var body: some View {
        Group {
            if vm.isLoading && vm.product == nil {
                ProgressView()
            } else if let p = vm.product {
                ScrollView {
                    VStack(alignment: .leading, spacing: 14) {
                        AsyncImage(url: URL(string: p.image_url ?? "")) { phase in
                            if let img = phase.image {
                                img.resizable().scaledToFill()
                            } else {
                                Color.secondary.opacity(0.15)
                            }
                        }
                        .frame(height: 240)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .padding(.horizontal)

                        Text(p.name)
                            .font(.title2).bold()
                            .padding(.horizontal)

                        Text(String(format: "₺ %.2f", p.sale_price))
                            .font(.title3)
                            .padding(.horizontal)

                        Text(p.description?.isEmpty == false ? p.description! : "SKU: \(p.sku)\nStok: \(p.stock_qty)")
                            .font(.body)
                            .foregroundStyle(.secondary)
                            .padding(.horizontal)

                        Stepper("Adet: \(vm.qty)", value: $vm.qty, in: 1...99)
                            .padding(.horizontal)

                        Button {
                            if !auth.isLoggedIn {
                                showLogin = true
                                return
                            }
                            Task { _ = await vm.addToCart(requireLogin: false) }
                        } label: {
                            Text(p.stock_qty > 0 ? "Sepete Ekle" : "Stok Yok")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .disabled(p.stock_qty <= 0 || vm.isLoading)
                        .padding(.horizontal)
                    }
                    .padding(.vertical)
                }
            } else {
                Text("Ürün bulunamadı.")
            }
        }
        .navigationTitle("Detay")
        .navigationBarTitleDisplayMode(.inline)
        .task { await vm.load(id: productId) }
        .alert("Hata", isPresented: .constant(vm.errorMessage != nil)) {
            Button("Tamam") { vm.errorMessage = nil }
        } message: { Text(vm.errorMessage ?? "") }
        .sheet(isPresented: $showLogin) {
            NavigationStack { LoginView() }
        }
        .overlay(alignment: .top) {
            if let t = vm.toast {
                Text(t).padding(10).background(.ultraThinMaterial).clipShape(Capsule())
                    .padding(.top, 10)
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) { vm.toast = nil }
                    }
            }
        }
    }
}
