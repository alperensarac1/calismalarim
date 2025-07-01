//
//  MasalarViewModel.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 6.06.2025.
//

import Foundation
import Combine

@MainActor
class MasalarViewModel: ObservableObject {
    // MARK: - Yayınlanan (Published) Özellikler
    @Published var masalar: [Masa] = []
    @Published var birlesmeSonucu: Bool = false

    // MARK: - DAO (Servis) Örneği
    private let dao = AdisyonServisDao.shared

    // MARK: - Masaları Yükle
    /// `masalariGetir()` servisini çağırarak `masalar` dizisini günceller.
    func masalariYukle() {
      Task {
        do {
          let liste = try await dao.masalariGetir()
          self.masalar = liste
        }
        catch let DecodingError.dataCorrupted(context) {
          print("🛑 Data corrupted:", context)
        }
        catch let DecodingError.keyNotFound(key, context) {
          print("🛑 Eksik anahtar '\(key.stringValue)':", context.debugDescription)
        }
        catch let DecodingError.typeMismatch(type, context) {
          print("🛑 Tür uyumsuzluğu '\(type)':", context.debugDescription)
        }
        catch let DecodingError.valueNotFound(value, context) {
          print("🛑 Değer bulunamadı '\(value)':", context.debugDescription)
        }
        catch {
          print("🛑 Diğer hata:", error)
        }
      }
    }


    // MARK: - Masa Güncelle
    /// Var olan `masalar` dizisinden, `id` eşleşen masayı günceller.
    /// - Parametre:
    ///   - masa: Değişmiş haliyle verilecek `Masa` nesnesi.
    func guncelleMasa(_ masa: Masa) {
        guard
            let index = masalar.firstIndex(where: { $0.id == masa.id })
        else { return }
        masalar[index] = masa
    }

    // MARK: - Masa Ekle
    /// Servise yeni bir masa eklemesi yaptırır. Başarı durumunda `onSuccess` kapanışı çağrılır.
    /// - Parametre:
    ///   - onSuccess: Masa eklendikten sonra çalışacak kapanış bloğu.
    func masaEkle(onSuccess: @escaping () -> Void) {
        Task {
            do {
                _ = try await dao.masaEkle()
                // Başarılı ekleme durumunda UI güncellemesi yapması için main thread’de onSuccess'i çağırıyoruz:
                onSuccess()
            } catch {
                print("MasalarViewModel – Masa ekleme hatası: \(error.localizedDescription)")
            }
        }
    }

    // MARK: - Masa Sil
    /// ID’si verilen masayı silme servisini çağırır.
    /// - Parametre:
    ///   - masaId: Silinmesi istenen masanın `id` değeri.
    func masaSil(masaId: Int) {
        Task {
            do {
                _ = try await dao.masaSil(masaId: masaId)
                // İsteğe bağlı: Silindikten sonra masalar listesini yeniden yükleyebilirsin.
            } catch {
                print("MasalarViewModel – Masa silme hatası: \(error.localizedDescription)")
            }
        }
    }

    // MARK: - Masaları Birleştir
    /// İki masayı birleştirme servisini çağırır ve başarılı olursa `birlesmeSonucu = true` yapar.
    /// - Parametreler:
    ///   - anaId: Ana masa id’si.
    ///   - bId: Birleştirilecek masa id’si.
    func masaBirlestir(anaId: Int, bId: Int) {
        Task {
            do {
                _ = try await dao.masaBirlestir(anaMasaId: anaId, birlestirilecekMasaId: bId)
                self.birlesmeSonucu = true
            } catch {
                print("MasalarViewModel – Birleştirme hatası: \(error.localizedDescription)")
                self.birlesmeSonucu = false
            }
        }
    }
}
