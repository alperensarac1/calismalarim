import 'package:flutter/material.dart';
import 'package:flutter_sms/flutter_sms.dart';
import 'package:permission_handler/permission_handler.dart';

class MesajlasmaEkrani extends StatefulWidget {
  @override
  _MesajlasmaEkraniState createState() => _MesajlasmaEkraniState();
}

class _MesajlasmaEkraniState extends State<MesajlasmaEkrani> {
  TextEditingController _numaraController = TextEditingController();
  TextEditingController _mesajController = TextEditingController();

  Future<void> _checkSmsPermission() async {
    var status = await Permission.sms.status;
    if (!status.isGranted) {
      status = await Permission.sms.request();
      if (!status.isGranted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text("SMS izni gereklidir!")),
        );
        return;
      }
    }
    _sendSms();
  }

  Future<void> _sendSms() async {
    String numara = _numaraController.text.trim();
    String mesaj = _mesajController.text.trim();

    if (numara.isNotEmpty && mesaj.isNotEmpty) {
      try {
        String result = await sendSMS(message: mesaj, recipients: [numara], sendDirect: true);
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Mesaj Gönderildi: $result")));
        _numaraController.clear();
        _mesajController.clear();
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Mesaj gönderilirken hata oluştu")));
      }
    } else {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Numara ve mesaj boş olamaz")));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Mesaj Gönder")),
      body: Padding(
        padding: EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(
              controller: _numaraController,
              keyboardType: TextInputType.phone,
              decoration: InputDecoration(labelText: "Telefon Numarası"),
            ),
            SizedBox(height: 8),
            TextField(
              controller: _mesajController,
              maxLines: 5,
              decoration: InputDecoration(labelText: "Mesaj"),
            ),
            SizedBox(height: 16),
            ElevatedButton(
              onPressed: _checkSmsPermission,
              child: Text("Mesaj Gönder"),
            ),
          ],
        ),
      ),
    );
  }
}
