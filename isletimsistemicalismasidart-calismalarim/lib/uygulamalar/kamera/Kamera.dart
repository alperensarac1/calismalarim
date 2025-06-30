import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:permission_handler/permission_handler.dart';
import 'dart:io';

class KameraEkrani extends StatefulWidget {
  @override
  _KameraEkraniState createState() => _KameraEkraniState();
}

class _KameraEkraniState extends State<KameraEkrani> {
  File? _image;
  final ImagePicker _picker = ImagePicker();

  Future<void> _checkCameraPermission() async {
    var status = await Permission.camera.status;
    if (!status.isGranted) {
      status = await Permission.camera.request();
      if (!status.isGranted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text("Kamera izni gereklidir!")),
        );
        return;
      }
    }
    _openCamera();
  }

  Future<void> _openCamera() async {
    final pickedFile = await _picker.pickImage(source: ImageSource.camera);
    if (pickedFile != null) {
      setState(() {
        _image = File(pickedFile.path);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Kamera")),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            _image != null
                ? Image.file(_image!, width: 300, height: 300, fit: BoxFit.cover)
                : Container(
              width: 300,
              height: 300,
              decoration: BoxDecoration(
                border: Border.all(color: Colors.grey),
              ),
              child: Center(
                child: Text("Fotoğraf Yok", style: TextStyle(color: Colors.grey)),
              ),
            ),
            SizedBox(height: 16),
            ElevatedButton(
              onPressed: _checkCameraPermission,
              child: Text("Fotoğraf Çek"),
            ),
          ],
        ),
      ),
    );
  }
}
