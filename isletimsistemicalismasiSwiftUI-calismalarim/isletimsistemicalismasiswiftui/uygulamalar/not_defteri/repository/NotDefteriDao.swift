
import Foundation
import CoreData
import UIKit

class NotDefteriDAO {
    
    let context = persistentContainer.viewContext

    
    func notEkle(baslik: String, icerik: String) {
        let yeniNot = Not(context: context)
        yeniNot.not_baslik = baslik
        yeniNot.not_icerik = icerik

        kaydet()
    }

   
    func notlariGetir() -> [Not] {
        let fetchRequest: NSFetchRequest<Not> = Not.fetchRequest()

        do {
            return try context.fetch(fetchRequest)
        } catch {
            print("Notlar getirilemedi: \(error.localizedDescription)")
            return []
        }
    }

    
    func notSil(not: Not) {
        context.delete(not)
        kaydet()
    }

   
    func notGuncelle(not: Not, yeniBaslik: String, yeniIcerik: String) {
        not.not_baslik = yeniBaslik
        not.not_icerik = yeniIcerik

        kaydet()
    }

    
    private func kaydet() {
        do {
            try context.save()
        } catch {
            print("Veriler kaydedilirken hata oluştu: \(error.localizedDescription)")
        }
    }
}
