import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:url_launcher/url_launcher.dart';


class Telefon extends StatefulWidget {
  @override
  _TelefonState createState() => _TelefonState();
}

class _TelefonState extends State<Telefon> {
  String numara = "";

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Telefon Araması'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            TextField(
              onChanged: (value) {
                setState(() {
                  numara = value;
                });
              },
              decoration: InputDecoration(
                labelText: 'Telefon Numarası',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.phone,
            ),
            SizedBox(height: 16),
            ElevatedButton(
              onPressed: () async {
                if (numara.isNotEmpty) {
                  PermissionStatus status = await Permission.phone.status;

                  if (status.isGranted) {
                    final Uri launchUri = Uri(scheme: 'tel', path: numara);
                    if (await canLaunch(launchUri.toString())) {
                      await launch(launchUri.toString());
                    } else {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(content: Text("Telefon araması başlatılamadı.")),
                      );
                    }
                  } else {
                    Permission.phone.request();
                  }
                }
              },
              child: Text('Ara'),
              style: ElevatedButton.styleFrom(
                minimumSize: Size(double.infinity, 50),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
