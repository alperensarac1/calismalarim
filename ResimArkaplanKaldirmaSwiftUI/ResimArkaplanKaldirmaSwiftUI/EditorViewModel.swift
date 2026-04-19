import SwiftUI
import PhotosUI

@MainActor
final class EditorViewModel: ObservableObject {
    
    @Published var originalImage: UIImage?
    @Published var workingImage: UIImage?
    
    @Published var tolerance: CGFloat = 60
    @Published var infoText: String = "Önce fotoğraf seçin. Sonra silmek istediğiniz bölgeye dokunun."
    @Published var isProcessing: Bool = false
    
    // Canlı preview state
    @Published var hasActivePreview: Bool = false
    private var previewBaseImage: UIImage?
    private var lastTappedPoint: CGPoint?
    
    // PNG export için
    @Published var exportedFileURL: URL?
    @Published var shouldShowShareSheet: Bool = false
    
    private var undoStack: [UIImage] = []
    private let maxUndoCount = 10
    
    func loadImage(from item: PhotosPickerItem?) async {
        guard let item else { return }
        
        isProcessing = true
        infoText = "Fotoğraf yükleniyor..."
        
        do {
            if let data = try await item.loadTransferable(type: Data.self),
               let image = UIImage(data: data) {
                
                let normalized = image.normalizedImage()
                
                originalImage = normalized
                workingImage = normalized
                previewBaseImage = nil
                lastTappedPoint = nil
                hasActivePreview = false
                exportedFileURL = nil
                shouldShowShareSheet = false
                undoStack.removeAll()
                
                infoText = "Fotoğraf yüklendi. Silmek istediğiniz bölgeye dokunun."
            } else {
                infoText = "Resim yüklenemedi."
            }
        } catch {
            infoText = "Resim yüklenemedi."
        }
        
        isProcessing = false
    }
    
    func onToleranceChanged(_ newValue: CGFloat) {
        tolerance = newValue
        
        if hasActivePreview {
            renderPreviewFromActiveState()
        }
    }
    
    func onImageTapped(x: Int, y: Int) {
        guard let current = workingImage else { return }
        guard let cgImage = current.cgImage else { return }
        guard x >= 0, x < cgImage.width, y >= 0, y < cgImage.height else { return }
        
        commitActivePreviewIfNeeded()
        saveStateForUndo(current)
        
        previewBaseImage = current
        lastTappedPoint = CGPoint(x: x, y: y)
        hasActivePreview = true
        infoText = "Canlı önizleme hazırlanıyor..."
        
        renderPreviewFromActiveState()
    }
    
    func undo() {
        guard !undoStack.isEmpty else { return }
        
        let previous = undoStack.removeLast()
        workingImage = previous
        hasActivePreview = false
        previewBaseImage = nil
        lastTappedPoint = nil
        infoText = "Son işlem geri alındı."
    }
    
    func reset() {
        guard let original = originalImage else { return }
        
        workingImage = original
        hasActivePreview = false
        previewBaseImage = nil
        lastTappedPoint = nil
        exportedFileURL = nil
        shouldShowShareSheet = false
        undoStack.removeAll()
        infoText = "Görsel sıfırlandı."
    }
    
    /// Gerçek PNG dosyası üretir ve paylaşım ekranını açmak için URL hazırlar.
    func exportAsPNG() {
        commitActivePreviewIfNeeded()
        
        guard let image = workingImage else { return }
        
        isProcessing = true
        infoText = "PNG hazırlanıyor..."
        
        Task.detached(priority: .userInitiated) {
            let fileURL = self.createPNGFile(from: image)
            
            await MainActor.run {
                self.isProcessing = false
                
                if let fileURL {
                    self.exportedFileURL = fileURL
                    self.shouldShowShareSheet = true
                    self.infoText = "PNG dosyası hazır."
                } else {
                    self.infoText = "PNG dışa aktarma başarısız oldu."
                }
            }
        }
    }
    
    var canUndo: Bool {
        !undoStack.isEmpty
    }
    
    private func renderPreviewFromActiveState() {
        guard let base = previewBaseImage,
              let point = lastTappedPoint else { return }
        
        isProcessing = true
        let currentTolerance = tolerance
        
        Task.detached(priority: .userInitiated) {
            let result = ImageProcessor.removeConnectedRegionByColor(
                source: base,
                startX: Int(point.x),
                startY: Int(point.y),
                tolerance: currentTolerance
            )
            
            await MainActor.run {
                self.workingImage = result
                self.isProcessing = false
                self.infoText = "Canlı önizleme aktif. Tolerans: \(Int(currentTolerance))"
            }
        }
    }
    
    private func commitActivePreviewIfNeeded() {
        guard hasActivePreview else { return }
        hasActivePreview = false
        previewBaseImage = nil
        lastTappedPoint = nil
    }
    
    private func saveStateForUndo(_ image: UIImage) {
        if undoStack.count >= maxUndoCount {
            undoStack.removeFirst()
        }
        undoStack.append(image)
    }
    
    /// UIImage -> PNG data -> temp klasöre gerçek .png dosyası
    nonisolated private func createPNGFile(from image: UIImage) -> URL? {
        guard let pngData = image.pngData() else { return nil }
        
        let fileName = "bg_removed_\(Int(Date().timeIntervalSince1970)).png"
        let tempURL = FileManager.default.temporaryDirectory.appendingPathComponent(fileName)
        
        do {
            if FileManager.default.fileExists(atPath: tempURL.path) {
                try FileManager.default.removeItem(at: tempURL)
            }
            
            try pngData.write(to: tempURL, options: .atomic)
            return tempURL
        } catch {
            print("PNG yazma hatası:", error)
            return nil
        }
    }
}

extension UIImage {
    func normalizedImage() -> UIImage {
        if imageOrientation == .up { return self }
        
        UIGraphicsBeginImageContextWithOptions(size, false, scale)
        draw(in: CGRect(origin: .zero, size: size))
        let normalized = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return normalized ?? self
    }
}
