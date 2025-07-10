import Foundation
import SwiftUI
import PhotosUI

struct SingleChatScreen: View {
    let aliciId: Int
    let aliciAd: String
    
    @StateObject private var vm = MesajlarViewModel()
    @Environment(\.dismiss) private var dismiss
    
    private var benimId: Int { AppConfig.kullaniciId }
    
    var body: some View {
        VStack {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack {
                        ForEach(vm.mesajlar) { msg in
                            MessageBubble(mesaj: msg, benimId: benimId)
                                .id(msg.id)
                        }
                    }
                    .padding(.vertical, 6)
                }
                .onChange(of: vm.mesajlar.count) { _ in
                    if let last = vm.mesajlar.last {
                        withAnimation {
                            proxy.scrollTo(last.id, anchor: .bottom)
                        }
                    }
                }
            }
            
            ChatInputBar { metin, img64 in
                vm.mesajGonder(gonderenId: benimId,
                               aliciId: aliciId,
                               mesajText: metin,
                               base64Image: img64)
            }
        }
        .navigationTitle(aliciAd)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .navigationBarLeading) { Button("Geri") { dismiss() } } }
        .onAppear {
            vm.mesajlariYuklePeriyodik(gonderenId: benimId, aliciId: aliciId)
        }
    }
}


struct ChatInputBar: View {
    @State private var text = ""
    @State private var selectedItem: PhotosPickerItem? = nil
    @State private var previewImage: UIImage? = nil
    
    let onSend: (_ metin: String, _ imgBase64: String?) -> Void
    
    var body: some View {
        VStack(spacing: 4) {
            // Önizleme
            if let img = previewImage {
                Image(uiImage: img)
                    .resizable()
                    .scaledToFit()
                    .frame(maxHeight: 180)
                    .cornerRadius(8)
                    .onTapGesture { previewImage = nil }
            }
            
            HStack {
                PhotosPicker(selection: $selectedItem, matching: .images) {
                    Image(systemName: "photo.on.rectangle")
                        .font(.system(size: 22))
                }
                .onChange(of: selectedItem) { item in
                    Task {
                        if let data = try? await item?.loadTransferable(type: Data.self),
                           let uiImg = UIImage(data: data) {
                            previewImage = uiImg
                        }
                    }
                }
                
                TextField("Mesaj yaz…", text: $text)
                    .textFieldStyle(.roundedBorder)
                
                Button {
                    guard !text.trimmingCharacters(in: .whitespaces).isEmpty || previewImage != nil else { return }
                    let base64 = previewImage?.jpegData(compressionQuality: 0.8)?
                        .base64EncodedString()
                    onSend(text.trimmingCharacters(in: .whitespaces), base64)
                    text = ""
                    previewImage = nil
                } label: {
                    Image(systemName: "paperplane.fill")
                        .font(.system(size: 22))
                }
            }
        }
        .padding(.horizontal)
        .padding(.bottom, 8)
    }
}


struct MessageBubble: View {
    let mesaj: Mesaj
    let benimId: Int

    private var isMine: Bool { mesaj.gonderen_id == benimId }
    private var bubbleColor: Color { isMine ? Color(hex: 0x4F9BFF) : Color(hex: 0xE0E0E0) }
    private var textColor:  Color { isMine ? .white : .black }

    var body: some View {
        HStack {
            if !isMine { Spacer(minLength: 0) }

            VStack(alignment: .leading, spacing: 6) {
                // Metin
                if let t = mesaj.mesaj_text, !t.isEmpty {
                    Text(t)
                        .foregroundColor(textColor)
                }

                // Resim
                if let urlStr = mesaj.resim_url,
                   let url = URL(string: urlStr),
                   !urlStr.isEmpty {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .success(let img):
                            img.resizable()
                                .scaledToFill()
                                .frame(maxWidth: .infinity, maxHeight: 200)
                                .clipped()
                                .cornerRadius(8)
                        case .failure:
                            EmptyView()
                        default:
                            ProgressView()
                        }
                    }
                }

                Text(mesaj.tarih)
                    .font(.footnote)
                    .foregroundColor(textColor.opacity(0.7))
                    .frame(maxWidth: .infinity, alignment: .trailing)
            }
            .padding(8)
            .background(bubbleColor)
            .clipShape(RoundedRectangle(cornerRadius: 12))

            if isMine { Spacer(minLength: 0) }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
    }
}
