import 'dart:ui' as ui;

import 'package:barcode/barcode.dart';
import 'package:flutter/material.dart';
import 'dart:typed_data';

import 'package:flutter/services.dart';
import 'package:qr_flutter/qr_flutter.dart';

import '../utils/Constants.dart';

class QRKodOlusturScreen extends StatefulWidget {
  const QRKodOlusturScreen({super.key});

  @override
  State<QRKodOlusturScreen> createState() => _QRKodOlusturScreenState();
}

class _QRKodOlusturScreenState extends State<QRKodOlusturScreen> {
  Uint8List? qrImage;
  String kalanSure = "90 saniye";
  String dogrulamaKodu = "";

  @override
  void initState() {
    super.initState();
    generateAndStart();
  }

  void generateAndStart() async {
    dogrulamaKodu = generateRandomCode();
    final image = await generateQrCode(dogrulamaKodu); // QR image byte array
    setState(() {
      qrImage = image;
    });
    startTimer();
  }

  void startTimer() {
    int seconds = 90;
    Future.doWhile(() async {
      await Future.delayed(const Duration(seconds: 1));
      if (seconds > 0) {
        seconds--;
        setState(() {
          kalanSure = "$seconds saniye";
        });
        return true;
      } else {
        generateAndStart();
        return false;
      }
    });
  }

  String generateRandomCode() {
    return DateTime.now().millisecondsSinceEpoch.toString().substring(5, 11);
  }

  Future<Uint8List> generateQrCode(String code) async {
    final qrValidationResult = QrValidator.validate(
      data: code,
      version: QrVersions.auto,
      errorCorrectionLevel: QrErrorCorrectLevel.H,
    );

    if (qrValidationResult.status == QrValidationStatus.valid) {
      final qrCode = qrValidationResult.qrCode!;
      final painter = QrPainter.withQr(
        qr: qrCode,
        color: const Color(0xFF000000),
        emptyColor: const Color(0xFFFFFFFF),
        gapless: true,
      );

      final image = await painter.toImage(250); // 300x300 boyutunda
      final byteData = await image.toByteData(format: ui.ImageByteFormat.png);

      return byteData!.buffer.asUint8List();
    } else {
      throw Exception("Geçersiz QR kod verisi.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: kbackgroundColor, // R.color.background
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            const SizedBox(height: 20),
            qrImage != null
                ? ClipRRect(
              borderRadius: BorderRadius.circular(30),
              child: Image.memory(qrImage!, width: 250, height: 250),
            )
                : const CircularProgressIndicator(),
            const SizedBox(height: 16),
            Text("Yenileme için kalan süre: $kalanSure"),
            const SizedBox(height: 10),
            Text(
              "Her 5 kahve alımınızda 1 kahve bizden !!",
              style: TextStyle(fontSize: 20, color: kTextColor),
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                beyazButton("Şubelerimiz"),
                const SizedBox(width: 20),
                beyazButton("Bizi Puanlayın"),
              ],
            ),
            const SizedBox(height: 20),
            beyazButton("Rastgele Ürün Getir"),
          ],
        ),
      ),
    );
  }

  Widget beyazButton(String text) {
    return ElevatedButton(
      onPressed: () {},
      style: ElevatedButton.styleFrom(
        foregroundColor: kTextColor,
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        side: BorderSide(color: kTextColor),
      ),
      child: Text(text),
    );
  }
}
