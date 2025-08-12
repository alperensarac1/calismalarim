//
//  ProfilVC.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//

import Foundation
import SwiftUI

struct ProfilView: View {
    @StateObject private var vm = ProfilViewModel()
    @State private var showNewEntry = false
    let userId: Int

    init(userId: Int = SessionManager.shared.getUserId()) {
        self.userId = userId
    }

    var body: some View {
        NavigationStack {
            Group {
                if vm.isLoading {
                    ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        ForEach(vm.filteredEntries, id: \.id) { item in
                            NavigationLink(value: item.id) {
                                EntryRowView(entry: item)
                            }
                            .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                                Button(role: .destructive) {
                                    vm.deleteEntry(entryId: item.id, userId: userId)
                                } label: { Label("Sil", systemImage: "trash") }
                            }
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Profil")
            .navigationDestination(for: Int.self) { entryId in
                EntryDetayView(entryId: entryId)
            }
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button { showNewEntry = true } label: { Image(systemName: "plus") }
                }
            }
            .searchable(text: $vm.searchQuery, placement: .navigationBarDrawer(displayMode: .always), prompt: "Ara")
            .task { vm.loadUserEntries(userId: userId) }
            .sheet(isPresented: $showNewEntry) {
                NewEntrySheet { vm.loadUserEntries(userId: userId) }
            }
            .alert("Bilgi", isPresented: .constant(vm.lastDeleteResult != nil)) {
                Button("Tamam") { vm.lastDeleteResult = nil }
            } message: {
                Text(vm.lastDeleteResult?.message ?? "")
            }
            .alert("Hata", isPresented: .constant(vm.errorMessage != nil)) {
                Button("Tamam") { vm.errorMessage = nil }
            } message: {
                Text(vm.errorMessage ?? "")
            }
        }
    }
}
