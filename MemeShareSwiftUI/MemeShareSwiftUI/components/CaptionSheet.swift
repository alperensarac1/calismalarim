import Foundation
import SwiftUI

struct CaptionSheet: View {
    @State private var caption: String = ""
    let onCancel: () -> Void
    let onSend: (String) -> Void

    var body: some View {
        NavigationStack {
            Form {
                Section("Açıklama") {
                    TextField("Açıklama…", text: $caption)
                }
            }
            .navigationTitle("Paylaş")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("İptal", action: onCancel)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Gönder") { onSend(caption.trimmingCharacters(in: .whitespacesAndNewlines)) }
                }
            }
        }
    }
}
