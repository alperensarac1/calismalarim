//
//  PatientVM.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 3.03.2026.
//

import Foundation

import Foundation

final class PatientVM {
    func createHelp(patientId: Int, servis: String, oda: String, lat: Double, lng: Double) async -> Result<String, AppError> {
           if servis.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ||
               oda.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
               return .failure(.message("Servis ve oda zorunlu"))
           }

           do {
               let res: ApiOk<EmptyDTO> = try await ApiClient.shared.post(
                   "help_create.php",
                   body: HelpCreateBody(
                       patient_id: patientId,
                       servis_adi: servis,
                       oda_no: oda,
                       lat: lat,
                       lng: lng
                   )
               )

               if res.ok == true {
                   if let req = res.request {
                       return .success("İstek oluşturuldu. No: \(req.id), Durum: \(req.status)")
                   } else {
                       return .success("Durum: OPEN (yardımcı bekleniyor)")
                   }
               } else {
                   return .failure(.message(res.error ?? "İstek gönderilemedi"))
               }
           } catch ApiError.badResponse {
               return .failure(.message("Sunucu uygun yanıt vermedi"))
           } catch ApiError.decodeError {
               return .failure(.message("Sunucu verisi çözümlenemedi"))
           } catch {
               return .failure(.message("İstek hatası: \(error.localizedDescription)"))
           }
       }

    func myActive(patientId: Int) async -> HelpActive? {
        do {
            let res: ApiOk<HelpActive> = try await ApiClient.shared.get(
                "help_my_active.php",
                query: ["patient_id": "\(patientId)"]
            )
            return (res.ok == true) ? res.active : nil
        } catch {
            return nil
        }
    }

    func confirm(requestId: Int, patientId: Int) async -> Result<String, AppError> {
        do {
            let res: ApiOk<EmptyDTO> = try await ApiClient.shared.post(
                "help_confirm.php",
                body: HelpConfirmBody(request_id: requestId, patient_id: patientId)
            )

            if res.ok == true {
                return .success("Durum: CONFIRMED")
            } else {
                return .failure(.message(res.error ?? "Onaylanamadı"))
            }
        } catch {
            return .failure(.message("Bağlantı hatası"))
        }
    }

    func cancel(requestId: Int, patientId: Int) async -> Result<String, AppError> {
        do {
            let res: ApiOk<EmptyDTO> = try await ApiClient.shared.post(
                "help_cancel.php",
                body: HelpCancelBody(request_id: requestId, patient_id: patientId)
            )

            if res.ok == true {
                return .success("İstek iptal edildi")
            } else {
                return .failure(.message(res.error ?? "İptal edilemedi"))
            }
        } catch {
            return .failure(.message("Bağlantı hatası"))
        }
    }
}
