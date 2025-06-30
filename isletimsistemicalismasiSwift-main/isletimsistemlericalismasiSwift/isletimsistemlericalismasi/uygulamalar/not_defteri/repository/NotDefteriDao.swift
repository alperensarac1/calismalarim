//
//  NotDefteriDao.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 26.01.2025.
//

import Foundation
import CoreData
import UIKit

class NotDefteriDAO {
    // Core Data context'i alma
    private var context: NSManagedObjectContext {
        guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else {
            fatalError("AppDelegate'e erişilemedi.")
        }
        return appDelegate.persistentContainer.viewContext
    }

    // Yeni bir not oluşturma
    func notEkle(baslik: String, icerik: String) {
        let yeniNot = Not(context: context)
        yeniNot.not_baslik = baslik
        yeniNot.not_icerik = icerik

        kaydet()
    }

    // Tüm notları getir
    func notlariGetir() -> [Not] {
        let fetchRequest: NSFetchRequest<Not> = Not.fetchRequest()

        do {
            return try context.fetch(fetchRequest)
        } catch {
            print("Notlar getirilemedi: \(error.localizedDescription)")
            return []
        }
    }

    // Belirli bir notu sil
    func notSil(not: Not) {
        context.delete(not)
        kaydet()
    }

    // Not güncelle
    func notGuncelle(not: Not, yeniBaslik: String, yeniIcerik: String) {
        not.not_baslik = yeniBaslik
        not.not_icerik = yeniIcerik

        kaydet()
    }

    // Core Data değişikliklerini kaydet
    private func kaydet() {
        do {
            try context.save()
        } catch {
            print("Veriler kaydedilirken hata oluştu: \(error.localizedDescription)")
        }
    }
}
