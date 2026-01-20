//
//  CartView.swift
//  EticaretSwiftUI
//
//  Created by Alperen Saraç on 18.01.2026.
//

import Foundation
import SwiftUI

struct CartView: View {
    @EnvironmentObject private var auth: AuthManager
    @StateObject private var vm = CartVM()
    @State private var showLogin = false

    var body: some View {
        Group {
            if !auth.isLoggedIn {
                VStack(spacing: 12) {
                    Text("Sepet için giriş gerekli.")
                    Button("Giriş Yap") { showLogin = true }
                        .buttonStyle(.borderedProminent)
                }
            } else if vm.isLoading && vm.cart == nil {
                ProgressView()
            } else {
                List {
                    if let items = vm.cart?.items {
                        ForEach(items, id: \.item_id) { it in
                            VStack(alignment: .leading, spacing: 8) {
                                Text(it.name).font(.headline)
                                Text(String(format: "₺ %.2f", it.sale_price)).foregroundStyle(.secondary)

                                HStack {
                                    Button("-") {
                                        if it.quantity > 1 {
                                            Task { await vm.update(itemId: it.item_id, qty: it.quantity - 1) }
                                        }
                                    }
                                    .buttonStyle(.bordered)

                                    Text("\(it.quantity)").frame(minWidth: 30)

                                    Button("+") {
                                        Task { await vm.update(itemId: it.item_id, qty: it.quantity + 1) }
                                    }
                                    .buttonStyle(.bordered)

                                    Spacer()

                                    Button(role: .destructive) {
                                        Task { await vm.delete(itemId: it.item_id) }
                                    } label: {
                                        Image(systemName: "trash")
                                    }
                                }
                            }
                            .padding(.vertical, 6)
                        }

                        if let total = vm.cart?.total {
                            Section("Özet") {
                                HStack {
                                    Text("Toplam")
                                    Spacer()
                                    Text(String(format: "₺ %.2f", total)).bold()
                                }

                                NavigationLink {
                                    CheckoutView(totalText: String(format: "₺ %.2f", total))
                                } label: {
                                    Text("Ödeme Yap")
                                        .frame(maxWidth: .infinity, alignment: .center)
                                }
                            }
                        }

                    } else {
                        Text("Sepet boş.")
                    }
                }
            }
        }
        .navigationTitle("Sepet")
        .task { if auth.isLoggedIn { await vm.load() } }
        .sheet(isPresented: $showLogin) { NavigationStack { LoginView() } }
        .alert("Hata", isPresented: .constant(vm.errorMessage != nil)) {
            Button("Tamam") { vm.errorMessage = nil }
        } message: { Text(vm.errorMessage ?? "") }.onAppear {
            if auth.isLoggedIn {
                Task { await vm.load() }
            }
        }

    }
}
