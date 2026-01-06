import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:image_gallery_saver/image_gallery_saver.dart';
import 'package:permission_handler/permission_handler.dart';

class KameraScreen extends StatefulWidget {
  const KameraScreen({super.key});

  @override
  State<KameraScreen> createState() => _KameraScreenState();
}

class _KameraScreenState extends State<KameraScreen> {
  final ImagePicker _picker = ImagePicker();

  File? _imageFile;
  String _userText = "";

  Uint8List? _previewBytes; // üzerine yazı basılmış önizleme
  bool _isSaving = false;

  Future<bool> _ensureCameraPermission() async {
    final status = await Permission.camera.request();
    return status.isGranted;
  }

  Future<void> _takePhoto() async {
    final ok = await _ensureCameraPermission();
    if (!ok) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text("Kamera izni gereklidir")),
        );
      }
      return;
    }

    final XFile? picked = await _picker.pickImage(
      source: ImageSource.camera,
      imageQuality: 100, // JPEG kalite
    );

    if (picked == null) return;

    setState(() {
      _imageFile = File(picked.path);
      _previewBytes = null;
    });
  }

  Future<Uint8List> _drawTextOnImageBytes(Uint8List imageBytes, String text) async {
    // Decode image to ui.Image
    final ui.Codec codec = await ui.instantiateImageCodec(imageBytes);
    final ui.FrameInfo frame = await codec.getNextFrame();
    final ui.Image image = frame.image;

    // Prepare canvas
    final ui.PictureRecorder recorder = ui.PictureRecorder();
    final Canvas canvas = Canvas(recorder);

    final paint = Paint();
    canvas.drawImage(image, Offset.zero, paint);

    // Text painter
    final textSpan = TextSpan(
      text: text,
      style: const TextStyle(
        fontSize: 40,
        color: Colors.white,
        shadows: [
          Shadow(
            blurRadius: 4,
            offset: Offset(1, 1),
            color: Colors.black,
          )
        ],
        fontWeight: FontWeight.w600,
      ),
    );

    final tp = TextPainter(
      text: textSpan,
      textDirection: TextDirection.ltr,
      maxLines: 3,
      ellipsis: "…",
    );

    // Foto genişliğine göre kırpma/yerleştirme
    final double padding = 20;
    tp.layout(maxWidth: image.width.toDouble() - padding * 2);

    final x = padding;
    final y = image.height.toDouble() - tp.height - padding;

    tp.paint(canvas, Offset(x, y));

    // Export to bytes
    final ui.Picture picture = recorder.endRecording();
    final ui.Image result = await picture.toImage(image.width, image.height);
    final ByteData? pngBytes = await result.toByteData(format: ui.ImageByteFormat.png);

    if (pngBytes == null) {
      throw Exception("Görsel işlenemedi");
    }

    return pngBytes.buffer.asUint8List();
  }

  Future<void> _openPreviewDialogAndSave() async {
    if (_imageFile == null) return;

    setState(() => _isSaving = true);

    try {
      final bytes = await _imageFile!.readAsBytes();
      final withText = await _drawTextOnImageBytes(bytes, _userText.trim());

      setState(() {
        _previewBytes = withText;
        _isSaving = false;
      });

      if (!mounted) return;

      // Önizleme dialog’u
      final bool? confirm = await showDialog<bool>(
        context: context,
        builder: (ctx) {
          return AlertDialog(
            title: const Text("Kaydetmeden Önce Önizleme"),
            content: SizedBox(
              width: double.maxFinite,
              child: AspectRatio(
                aspectRatio: 1,
                child: Container(
                  decoration: BoxDecoration(
                    border: Border.all(color: Colors.grey),
                  ),
                  child: _previewBytes == null
                      ? const Center(child: CircularProgressIndicator())
                      : Image.memory(_previewBytes!, fit: BoxFit.cover),
                ),
              ),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(ctx, false),
                child: const Text("İptal"),
              ),
              ElevatedButton(
                onPressed: () => Navigator.pop(ctx, true),
                child: const Text("Evet, Kaydet"),
              ),
            ],
          );
        },
      );

      if (confirm == true) {
        await _saveToGallery(_previewBytes!);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text("Fotoğraf kaydedildi")),
          );
        }
      }
    } catch (e) {
      setState(() => _isSaving = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text("Hata: $e")),
        );
      }
    }
  }

  Future<void> _saveToGallery(Uint8List bytes) async {
    // Android 13+ genelde ekstra izin istemeden kaydedebilir; bazı cihazlarda Photos izni gerekir.
    // iOS için de Photos ekleme izni istenebilir.
    await Permission.photosAddOnly.request(); // iOS
    await Permission.photos.request(); // bazı Android cihazlar / galeriler

    final name = "photo_${DateTime.now().millisecondsSinceEpoch}";
    final result = await ImageGallerySaver.saveImage(
      bytes,
      quality: 100,
      name: name,
    );

    if (result["isSuccess"] != true) {
      throw Exception("Galeriye kaydedilemedi: $result");
    }
  }

  @override
  Widget build(BuildContext context) {
    final img = _imageFile;

    return Scaffold(
      appBar: AppBar(title: const Text("Kamera Düzenleme")),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 300,
              height: 300,
              decoration: BoxDecoration(
                border: Border.all(color: Colors.grey, width: 2),
              ),
              alignment: Alignment.center,
              child: img == null
                  ? const Text("Fotoğraf Yok", style: TextStyle(color: Colors.grey))
                  : Image.file(img, fit: BoxFit.cover),
            ),
            const SizedBox(height: 16),
            TextField(
              decoration: const InputDecoration(
                labelText: "Fotoğrafa yazılacak metin",
                border: OutlineInputBorder(),
              ),
              onChanged: (v) => setState(() => _userText = v),
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                ElevatedButton(
                  onPressed: _isSaving ? null : _takePhoto,
                  child: const Text("Fotoğraf Çek"),
                ),
                const SizedBox(width: 16),
                ElevatedButton(
                  onPressed: (img == null || _isSaving) ? null : _openPreviewDialogAndSave,
                  child: _isSaving
                      ? const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                      : const Text("Kaydet"),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
