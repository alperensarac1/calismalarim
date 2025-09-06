import Foundation
import UIKit
import Combine

@MainActor
final class OdaViewModel: ObservableObject {

    @Published var uploadResult: String? = nil

    @Published var gonderiler: [GonderiModel]? = nil

    @Published var odaOlusturmaSonucu: SimpleResponse? = nil

    @Published var isLoading: Bool = false

    // MARK: - Görsel upload (Base64 JSON)
    /// Android: uploadImage(uri, roomId, userId, caption)
    /// iOS: UIImage ver; burada JPEG(0.8) -> Base64 -> APIService.uploadImageBase64
    func uploadImage(image: UIImage,
                     roomId: Int,
                     userId: Int,
                     caption: String) {
        guard let jpeg = image.jpegData(compressionQuality: 0.8) else {
            uploadResult = "Görsel sıkıştırılamadı."
            return
        }
        let base64 = jpeg.base64EncodedString()
        let req = ImageUploadRequest(roomId: roomId,
                                     userId: userId,
                                     base64Image: base64,
                                     caption: caption)

        isLoading = true
        Task {
            defer { isLoading = false }
            do {
                let res = try await APIService.shared.uploadImageBase64(req)
                if res.success {
                    uploadResult = "Görsel yüklendi"
                } else {
                    uploadResult = "Görsel yükleme hatası"
                }
            } catch {
                uploadResult = "Bağlantı hatası: \(error.localizedDescription)"
            }
        }
    }

    // MARK: - Odadaki medyaları çek
    /// Android: getAllMedia(roomId)
    func getAllMedia(roomId: Int) {
        isLoading = true
        Task {
            defer { isLoading = false }
            do {
                let items = try await APIService.shared.getAllMedia(roomId: roomId)
                gonderiler = items
            } catch {
                gonderiler = nil
            }
        }
    }

    // MARK: - Oda oluştur
    /// Android: createRoom(userId)
    func createRoom(userId: Int) {
        isLoading = true
        Task {
            defer { isLoading = false }
            do {
                let res = try await APIService.shared.createRoom(userId: userId)
                odaOlusturmaSonucu = res
            } catch {
                odaOlusturmaSonucu = SimpleResponse(success: false,
                                                    message: "Bağlantı hatası: \(error.localizedDescription)",
                                                    roomCode: nil,
                                                    roomId: nil)
            }
        }
    }
}
