//
//  isletimsistemicalismasiswiftuiApp.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 22.02.2025.
//

import SwiftUI
import CoreData
@main
struct isletimsistemicalismasiswiftuiApp: App {
    let persistenceController = PersistenceController.shared

    var body: some Scene {
        WindowGroup {
            SifreOlusturScreen()
                .environment(\.managedObjectContext, persistenceController.container.viewContext)
        }
    }
}
var persistentContainer: NSPersistentContainer = {
    let container = NSPersistentContainer(name: "isletimsistemicalismasiswiftui")
    container.loadPersistentStores(completionHandler: {(storedescription,error) in
        if let error = error as NSError? {
            fatalError("Unresolved Error \(error) , \(error.userInfo)")
        }
    })
    return container
}()

func saveContext(){
    let context = persistentContainer.viewContext
    if context.hasChanges{
        do{
            try context.save()
        }catch{
            let nserror = error as NSError
            fatalError("Unresolved error \(nserror) , \(nserror.userInfo)")
        }
    }
}
