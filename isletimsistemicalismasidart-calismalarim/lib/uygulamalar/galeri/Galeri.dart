import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

class GaleriScreen extends StatefulWidget {
  @override
  _GaleriScreenState createState() => _GaleriScreenState();
}

class _GaleriScreenState extends State<GaleriScreen> {
  File? _selectedImage;
  final ImagePicker _picker = ImagePicker();

  Future<void> _pickImage() async {
    final pickedFile = await _picker.pickImage(source: ImageSource.gallery);
    if (pickedFile != null) {
      setState(() {
        _selectedImage = File(pickedFile.path);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Galeri")),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            _selectedImage != null
                ? ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: Image.file(
                _selectedImage!,
                width: 250,
                height: 250,
                fit: BoxFit.cover,
              ),
            )
                : Text("Henüz bir resim seçilmedi"),
            SizedBox(height: 16),
            ElevatedButton(
              onPressed: _pickImage,
              child: Text("Galeriden Resim Seç"),
            ),
          ],
        ),
      ),
    );
  }
}
