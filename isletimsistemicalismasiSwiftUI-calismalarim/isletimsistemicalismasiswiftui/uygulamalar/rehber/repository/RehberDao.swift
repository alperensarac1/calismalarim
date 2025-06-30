//
//  RehberDao.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//

import CoreData
import Foundation
import UIKit

class RehberDao{
    let context = persistentContainer.viewContext

    func kisiEkle(kisi_ad: String, kisi_tel: String) {
        let yeniKisi = Kisi(context: context)
        yeniKisi.kisi_ad = kisi_ad
        yeniKisi.kisi_tel = kisi_tel
        kaydet()
    }

    func kisileriGetir() -> [Kisi] {
        let fetchRequest: NSFetchRequest<Kisi> = Kisi.fetchRequest()

        do {
            return try context.fetch(fetchRequest)
        } catch {
            print("Kişiler: \(error.localizedDescription)")
            return []
        }
    }

    func kisiSil(kisi: Kisi) {
        context.delete(kisi)
        kaydet()
    }

    func kisiGetir(kisi_ad:String)->[Kisi]{
        let fetchRequest: NSFetchRequest<Kisi> = Kisi.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "kisi_ad CONTAINS[cd] %@", kisi_ad)
        do{
            return try context.fetch(fetchRequest)
        }catch{
            print("Kişiler \(error.localizedDescription)")
            return []
        }
    }
    
    func kisiGuncelle(kisi: Kisi, kisi_ad: String, kisi_tel: String) {
        kisi.kisi_ad = kisi_ad
        kisi.kisi_tel = kisi_tel
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
