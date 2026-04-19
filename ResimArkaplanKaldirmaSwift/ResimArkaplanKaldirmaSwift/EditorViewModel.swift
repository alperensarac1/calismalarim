import UIKit

final class EditorViewModel {

    var onStateChanged: (() -> Void)?

    private(set) var originalImage: UIImage?
    private(set) var workingImage: UIImage?

    private(set) var tolerance: CGFloat = 60
    private(set) var infoText: String = "Önce fotoğraf seçin. Sonra silmek istediğiniz bölgeye dokunun."
    private(set) var isProcessing: Bool = false

    // Son aktif canlı önizleme state'i
    private var hasActivePreview = false
    private var previewBaseImage: UIImage?
    private var lastTappedPoint: CGPoint?

    private var undoStack: [UIImage] = []
    private let maxUndoCount = 10

    // Eski async sonuçların yeni sonucu ezmesini önler
    private var previewRequestID: Int = 0

    var canUndo: Bool {
        !undoStack.isEmpty
    }

    func setLoadingState() {
        isProcessing = true
        infoText = "Fotoğraf yükleniyor..."
        notify()
    }

    func setErrorState(_ text: String) {
        isProcessing = false
        infoText = text
        notify()
    }

    func loadImage(_ image: UIImage) {
        previewRequestID += 1

        originalImage = image
        workingImage = image
        previewBaseImage = nil
        lastTappedPoint = nil
        hasActivePreview = false
        undoStack.removeAll()
        isProcessing = false
        infoText = "Fotoğraf yüklendi. Silmek istediğiniz bölgeye dokunun."
        notify()
    }

    func onToleranceChanged(_ newValue: CGFloat) {
        tolerance = newValue
        notify()

        if hasActivePreview {
            renderPreviewFromActiveState()
        }
    }

    func onImageTapped(x: Int, y: Int) {
        guard let current = workingImage,
              let cgImage = current.cgImage else { return }

        guard x >= 0, x < cgImage.width, y >= 0, y < cgImage.height else { return }

        // Önce varsa önceki canlı sonucu mevcut workingImage üstünde kalıcı kabul ediyoruz
        commitActivePreviewIfNeeded()

        saveStateForUndo(current)

        previewBaseImage = current
        lastTappedPoint = CGPoint(x: x, y: y)
        hasActivePreview = true
        infoText = "Canlı önizleme hazırlanıyor..."
        notify()

        renderPreviewFromActiveState()
    }

    func undo() {
        previewRequestID += 1

        guard !undoStack.isEmpty else { return }

        let previous = undoStack.removeLast()
        workingImage = previous
        hasActivePreview = false
        previewBaseImage = nil
        lastTappedPoint = nil
        isProcessing = false
        infoText = "Son işlem geri alındı."
        notify()
    }

    func reset() {
        previewRequestID += 1

        guard let original = originalImage else { return }

        workingImage = original
        hasActivePreview = false
        previewBaseImage = nil
        lastTappedPoint = nil
        undoStack.removeAll()
        isProcessing = false
        infoText = "Görsel sıfırlandı."
        notify()
    }

    func exportAsPNG(completion: @escaping (URL?) -> Void) {
        commitActivePreviewIfNeeded()

        guard let image = workingImage else {
            completion(nil)
            return
        }

        isProcessing = true
        infoText = "PNG hazırlanıyor..."
        notify()

        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            let exportedURL = Self.createPNGFile(from: image)

            DispatchQueue.main.async {
                guard let self else { return }

                self.isProcessing = false
                self.infoText = exportedURL != nil ? "PNG dosyası hazır." : "PNG dışa aktarma başarısız oldu."
                self.notify()
                completion(exportedURL)
            }
        }
    }

    private func renderPreviewFromActiveState() {
        guard let base = previewBaseImage,
              let point = lastTappedPoint else { return }

        previewRequestID += 1
        let requestID = previewRequestID

        let currentTolerance = tolerance
        isProcessing = true
        infoText = "Canlı önizleme güncelleniyor..."
        notify()

        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            let result = ImageProcessor.removeConnectedRegionByColor(
                source: base,
                startX: Int(point.x),
                startY: Int(point.y),
                tolerance: currentTolerance
            )

            DispatchQueue.main.async {
                guard let self else { return }

                // Bu sonuç güncel isteğe ait değilse çöpe at
                guard requestID == self.previewRequestID else { return }

                self.workingImage = result
                self.isProcessing = false
                self.infoText = "Canlı önizleme aktif. Tolerans: \(Int(currentTolerance))"
                self.notify()
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

    private func notify() {
        onStateChanged?()
    }

    private static func createPNGFile(from image: UIImage) -> URL? {
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
