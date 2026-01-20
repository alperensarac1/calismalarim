//
//  HomeViewModel.swift
//  EticaretSwiftUI
//
//  Created by Alperen Saraç on 18.01.2026.
//

import Foundation


@MainActor
final class HomeViewModel: ObservableObject {
    @Published var categories: [CategoryDto] = []
    @Published var products: [ProductListDto] = []
    @Published var selectedCategoryId: Int? = nil
    @Published var searchText: String = ""
    @Published var isLoading = false
    @Published var errorMessage: String?

    private var page = 1
    private let per = 10
    private var canLoadMore = true

    func loadInitial() async {
        isLoading = true
        errorMessage = nil
        do {
            async let c = ApiClient.shared.getCategories()
            let cats = try await c
            categories = cats

            page = 1
            canLoadMore = true
            let res = try await ApiClient.shared.getProducts(
                cat: selectedCategoryId,
                q: searchText.isEmpty ? nil : searchText,
                sort: "newest",
                page: page,
                per: per
            )
            products = res.items
            canLoadMore = products.count < res.total
            isLoading = false
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }

    func reloadProducts() async {
        isLoading = true
        errorMessage = nil
        do {
            page = 1
            canLoadMore = true
            let res = try await ApiClient.shared.getProducts(
                cat: selectedCategoryId,
                q: searchText.isEmpty ? nil : searchText,
                sort: "newest",
                page: page,
                per: per
            )
            products = res.items
            canLoadMore = products.count < res.total
            isLoading = false
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }

    func loadMoreIfNeeded(current item: ProductListDto) async {
        guard canLoadMore, !isLoading else { return }
        guard products.last?.id == item.id else { return }

        isLoading = true
        do {
            page += 1
            let res = try await ApiClient.shared.getProducts(
                cat: selectedCategoryId,
                q: searchText.isEmpty ? nil : searchText,
                sort: "newest",
                page: page,
                per: per
            )
            products.append(contentsOf: res.items)
            canLoadMore = products.count < res.total
            isLoading = false
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }
}
