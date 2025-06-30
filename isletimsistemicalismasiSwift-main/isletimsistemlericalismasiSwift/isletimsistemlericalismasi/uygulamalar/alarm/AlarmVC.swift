//
//  AlarmVC.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 31.01.2025.
//

import UIKit
import UserNotifications

class AlarmVC: UIViewController {

    @IBOutlet weak var datePicker: UIDatePicker!
    override func viewDidLoad() {
        super.viewDidLoad()

        
    }
    

    @IBAction func btnAyarla(_ sender: Any) {
        scheduleNotification(at: datePicker.date)
                let formatter = DateFormatter()
                formatter.timeStyle = .short
                let selectedTime = formatter.string(from: datePicker.date)
                showAlert(title: "Alarm Kuruldu", message: "Alarm \(selectedTime) saatine ayarlandı.")
    }
    
    @IBAction func btniptalEt(_ sender: Any) {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: ["alarmNotification"])
                showAlert(title: "Alarm İptal Edildi", message: "Kurulan alarm başarıyla iptal edildi.")
    }
    func scheduleNotification(at date: Date) {
           let content = UNMutableNotificationContent()
           content.title = "Alarm"
           content.body = "Zamanı Geldi!"
           content.sound = .default
           
           let calendar = Calendar.current
           var dateComponents = calendar.dateComponents([.hour, .minute], from: date)
           
           // Eğer saat geçmişse, ertesi güne al
           let now = Date()
           if let alarmDate = calendar.date(from: dateComponents), alarmDate < now {
               dateComponents.day = (calendar.dateComponents([.day], from: now).day ?? 0) + 1
           }
           
           let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: false)
           let request = UNNotificationRequest(identifier: "alarmNotification", content: content, trigger: trigger)
           
           UNUserNotificationCenter.current().add(request) { error in
               if let error = error {
                   print("Alarm programlanırken hata oluştu: \(error.localizedDescription)")
               }
           }
       }
       
       // Kullanıcıya Onay Mesajı Gösterme
       func showAlert(title: String, message: String) {
           let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
           alert.addAction(UIAlertAction(title: "Tamam", style: .default))
           present(alert, animated: true)
       }
    
}


