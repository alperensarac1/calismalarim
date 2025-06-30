//
//  Alarm.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//


import SwiftUI
import UserNotifications

struct Alarm: View {
    @State private var selectedDate = Date()
    @State private var showAlert = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""

    var body: some View {
        VStack(spacing: 20) {
            Text("Alarm Ayarla")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            DatePicker("Saat Seçin", selection: $selectedDate, displayedComponents: .hourAndMinute)
                .datePickerStyle(WheelDatePickerStyle())
                .labelsHidden()
                .padding()

            HStack {
                Button(action: setAlarm) {
                    Text("Alarmı Ayarla")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }//:Button

                Button(action: cancelAlarm) {
                    Text("Alarmı İptal Et")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.red)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }//:Button
            }//:HStack
            .padding()
        }//:VStack
        .padding()
        .alert(alertTitle, isPresented: $showAlert) {
            Button("Tamam", role: .cancel) {}
        } message: {
            Text(alertMessage)
        }
        .onAppear {
            requestNotificationPermission()
        }
    }

    func setAlarm() {
        scheduleNotification(at: selectedDate)

        let formatter = DateFormatter()
        formatter.timeStyle = .short
        let selectedTime = formatter.string(from: selectedDate)
        
        alertTitle = "Alarm Kuruldu"
        alertMessage = "Alarm \(selectedTime) saatine ayarlandı."
        showAlert = true
    }//:Function End

    func cancelAlarm() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: ["alarmNotification"])

        alertTitle = "Alarm İptal Edildi"
        alertMessage = "Kurulan alarm başarıyla iptal edildi."
        showAlert = true
    }//:Function End

   
    func requestNotificationPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound]) { granted, error in
            if let error = error {
                print("Bildirim izni alınırken hata oluştu: \(error.localizedDescription)")
            } else {
                print("Bildirim izni verildi mi? \(granted)")
            }
        }
    }//:Function End

    
    func scheduleNotification(at date: Date) {
        let content = UNMutableNotificationContent()
        content.title = "Alarm"
        content.body = "Zamanı Geldi!"
        content.sound = .default

        let calendar = Calendar.current
        var dateComponents = calendar.dateComponents([.hour, .minute], from: date)

        
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
    }//:Function End
    
}




struct Alarm_Previews: PreviewProvider {
    static var previews: some View {
        Alarm()
    }
}
