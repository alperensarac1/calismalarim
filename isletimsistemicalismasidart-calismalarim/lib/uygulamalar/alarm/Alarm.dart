import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:timezone/timezone.dart';
import 'package:timezone/timezone.dart' as tz;
import 'package:timezone/tzdata.dart';

class AlarmScreen extends StatefulWidget {
  @override
  _AlarmScreenState createState() => _AlarmScreenState();
}

class _AlarmScreenState extends State<AlarmScreen> {
  FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
  FlutterLocalNotificationsPlugin();

  TimeOfDay? selectedTime;

  @override
  void initState() {
    super.initState();
    _requestPermissions();
    _initializeNotifications();
  }

  Future<void> _requestPermissions() async {
    Map<Permission, PermissionStatus> statuses = await [
      Permission.notification,
      Permission.scheduleExactAlarm, // Android 13+ için
    ].request();

    if (statuses[Permission.notification]!.isDenied) {
      print("Bildirim izni reddedildi.");
    }

    if (statuses[Permission.scheduleExactAlarm]!.isDenied) {
      print("Alarm izni reddedildi.");
    }
  }

  void _initializeNotifications() {
    var initializationSettingsAndroid =
    AndroidInitializationSettings('@mipmap/ic_launcher');

    var initializationSettingsIOS = DarwinInitializationSettings();

    var initializationSettings = InitializationSettings(
      android: initializationSettingsAndroid,
      iOS: initializationSettingsIOS,
    );

    flutterLocalNotificationsPlugin.initialize(initializationSettings);
  }
  Future<void> _scheduleAlarm() async {
    if (selectedTime == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Lütfen bir saat seçin.")),
      );
      return;
    }

    DateTime now = DateTime.now();
    DateTime alarmTime = DateTime(
      now.year,
      now.month,
      now.day,
      selectedTime!.hour,
      selectedTime!.minute,
    );

    var androidDetails = AndroidNotificationDetails(
      'alarm_channel',
      'Alarm Bildirimi',
      importance: Importance.max,
      priority: Priority.high,
      playSound: true,
      sound: RawResourceAndroidNotificationSound('alarm'),
    );

    var iOSDetails = DarwinNotificationDetails();
    var platformDetails = NotificationDetails(
      android: androidDetails,
      iOS: iOSDetails,
    );

    tz.TZDateTime tzAlarmTime = tz.TZDateTime.from(alarmTime, tz.local);

    await flutterLocalNotificationsPlugin.zonedSchedule(
      0,
      'Alarm',
      'Alarm çalıyor!',
      tzAlarmTime, // ✅ Doğru format
      platformDetails,
      uiLocalNotificationDateInterpretation:
      UILocalNotificationDateInterpretation.absoluteTime,
      androidAllowWhileIdle: true,
    );

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text("Alarm Ayarlandı: ${selectedTime!.format(context)}")),
    );
  }


  Future<void> _cancelAlarm() async {
    await flutterLocalNotificationsPlugin.cancel(0);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text("Alarm iptal edildi.")),
    );
  }

  /// 📌 **Saat Seçici Aç**
  Future<void> _selectTime(BuildContext context) async {
    final TimeOfDay? pickedTime = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.now(),
    );

    if (pickedTime != null && pickedTime != selectedTime) {
      setState(() {
        selectedTime = pickedTime;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Alarm Kur")),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              selectedTime == null
                  ? "Saat Seç"
                  : "Seçilen Saat: ${selectedTime!.format(context)}",
              style: TextStyle(fontSize: 18),
            ),
            SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => _selectTime(context),
              child: Text("Saat Seç"),
            ),
            SizedBox(height: 16),
            ElevatedButton(
              onPressed: _scheduleAlarm,
              child: Text("Alarm Kur"),
            ),
            SizedBox(height: 16),
            ElevatedButton(
              onPressed: _cancelAlarm,
              child: Text("Alarmı İptal Et"),
            ),
          ],
        ),
      ),
    );
  }
}
