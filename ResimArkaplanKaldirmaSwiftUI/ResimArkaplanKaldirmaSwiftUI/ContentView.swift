import SwiftUI
import PhotosUI

struct ContentView: View {
    
    @StateObject private var viewModel = EditorViewModel()
    @State private var selectedItem: PhotosPickerItem?
    
    var body: some View {
        VStack(spacing: 12) {
            
            HStack(spacing: 8) {
                PhotosPicker(selection: $selectedItem, matching: .images) {
                    Text("Fotoğraf Seç")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
                
                Button {
                    viewModel.undo()
                } label: {
                    Text("Geri Al")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(viewModel.canUndo ? Color.orange : Color.gray)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
                .disabled(!viewModel.canUndo || viewModel.isProcessing)
            }
            
            HStack(spacing: 8) {
                Button {
                    viewModel.reset()
                } label: {
                    Text("Sıfırla")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.red)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
                .disabled(viewModel.workingImage == nil || viewModel.isProcessing)
                
                Button {
                    viewModel.exportAsPNG()
                } label: {
                    Text("PNG Kaydet")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.green)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
                .disabled(viewModel.workingImage == nil || viewModel.isProcessing)
            }
            
            Text(viewModel.infoText)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.top, 4)
            
            Text("Tolerans: \(Int(viewModel.tolerance))")
                .frame(maxWidth: .infinity, alignment: .leading)
            
            Slider(
                value: Binding(
                    get: { viewModel.tolerance },
                    set: { viewModel.onToleranceChanged($0) }
                ),
                in: 0...255
            )
            .disabled(viewModel.workingImage == nil)
            
            ZoomableImageEditor(
                image: viewModel.workingImage,
                onImageTap: { x, y in
                    viewModel.onImageTapped(x: x, y: y)
                }
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .cornerRadius(12)
        }
        .padding()
        .task(id: selectedItem) {
            if let selectedItem {
                await viewModel.loadImage(from: selectedItem)
            }
        }
        .sheet(isPresented: $viewModel.shouldShowShareSheet) {
            if let url = viewModel.exportedFileURL {
                ShareSheet(activityItems: [url])
            }
        }
    }
}
