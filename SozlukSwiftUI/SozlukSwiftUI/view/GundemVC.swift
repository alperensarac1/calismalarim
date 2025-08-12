//
//  GundemVC.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//

import Foundation
import SwiftUI
struct GundemView: View {
    @StateObject private var vm = GundemViewModel()
    @State private var showNewEntry = false

    var body: some View {
        NavigationStack {
            Group {
                if vm.isLoading {
                    ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List(vm.filteredEntries, id: \.id) { item in
                        NavigationLink(value: item.id) {
                            EntryRowView(entry: item)
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Gündem")
            .navigationDestination(for: Int.self) { entryId in
                EntryDetayView(entryId: entryId)
            }
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        showNewEntry = true
                    } label: { Image(systemName: "plus") }
                }
            }
            .searchable(text: $vm.searchQuery, placement: .navigationBarDrawer(displayMode: .always), prompt: "Ara")
            .task { vm.loadMostCommentedEntriesToday() }
            .sheet(isPresented: $showNewEntry) {
                NewEntrySheet { vm.loadMostCommentedEntriesToday() }
            }
            .alert("Hata", isPresented: .constant(vm.errorMessage != nil)) {
                Button("Tamam") { vm.errorMessage = nil }
            } message: {
                Text(vm.errorMessage ?? "")
            }
        }
    }
}
