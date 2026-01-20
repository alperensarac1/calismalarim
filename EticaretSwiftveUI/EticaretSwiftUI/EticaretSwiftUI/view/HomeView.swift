//
//  HomeView.swift
//  EticaretSwiftUI
//
//  Created by Alperen Saraç on 18.01.2026.
//

import Foundation
import SwiftUI

struct HomeView: View {
    @StateObject private var vm = HomeViewModel()
    @EnvironmentObject private var auth: AuthManager

    var body: some View {
        VStack(spacing: 12) {
            // Search
            TextField("Ürün ara", text: $vm.searchText)
                .textFieldStyle(.roundedBorder)
                .padding(.horizontal)
                .onSubmit { Task { await vm.reloadProducts() } }

            // Categories
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 10) {
                    CategoryChip(title: "Tümü", isSelected: vm.selectedCategoryId == nil) {
                        vm.selectedCategoryId = nil
                        Task { await vm.reloadProducts() }
                    }

                    ForEach(vm.categories, id: \.id) { c in
                        CategoryChip(title: c.name, isSelected: vm.selectedCategoryId == c.id) {
                            vm.selectedCategoryId = c.id
                            Task { await vm.reloadProducts() }
                        }
                    }
                }
                .padding(.horizontal)
            }

            // Products
            List {
                ForEach(vm.products, id: \.id) { p in
                    NavigationLink {
                        ProductDetailView(productId: p.id)
                    } label: {
                        ProductRow(p: p)
                    }
                    .task { await vm.loadMoreIfNeeded(current: p) }
                }

                if vm.isLoading {
                    HStack { Spacer(); ProgressView(); Spacer() }
                }
            }
            .listStyle(.plain)
        }
        .navigationTitle("Ürünler")
        .task { await vm.loadInitial() }
        .alert("Hata", isPresented: .constant(vm.errorMessage != nil)) {
            Button("Tamam") { vm.errorMessage = nil }
        } message: {
            Text(vm.errorMessage ?? "")
        }
    }
}

struct CategoryChip: View {
    let title: String
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            Text(title)
                .font(.subheadline)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(isSelected ? Color.primary.opacity(0.15) : Color.secondary.opacity(0.1))
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}

struct ProductRow: View {
    let p: ProductListDto

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: p.image_url ?? "")) { phase in
                if let img = phase.image {
                    img.resizable().scaledToFill()
                } else {
                    Color.secondary.opacity(0.15)
                }
            }
            .frame(width: 56, height: 56)
            .clipShape(RoundedRectangle(cornerRadius: 12))

            VStack(alignment: .leading, spacing: 6) {
                Text(p.name).font(.headline).lineLimit(2)
                Text(String(format: "₺ %.2f", p.sale_price))
                    .font(.subheadline)
            }
            Spacer()
        }
        .padding(.vertical, 6)
    }
}
