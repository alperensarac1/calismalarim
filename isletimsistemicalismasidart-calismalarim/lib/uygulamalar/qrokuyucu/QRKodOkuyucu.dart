import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:qr_code_scanner/qr_code_scanner.dart';
import 'package:webview_flutter/webview_flutter.dart';

class QrOkuyucuScreen extends StatefulWidget {
  @override
  _QrOkuyucuScreenState createState() => _QrOkuyucuScreenState();
}

class _QrOkuyucuScreenState extends State<QrOkuyucuScreen> {
  final GlobalKey qrKey = GlobalKey(debugLabel: 'QR');
  QRViewController? controller;
  String scanResult = "";
  bool isScanning = false;
  String? urlToOpen;

  void _onQRViewCreated(QRViewController controller) {
    this.controller = controller;
    controller.scannedDataStream.listen((scanData) {
      setState(() {
        scanResult = scanData.code ?? "";
        isScanning = false;
        controller.pauseCamera(); // Tarama sonrası kamerayı durdur
        _showResultDialog(scanResult);
      });
    });
  }

  void _showResultDialog(String result) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text("Sonuç"),
          content: Text(result),
          actions: [
            TextButton(
              onPressed: () {
                Clipboard.setData(ClipboardData(text: result));
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text("Panoya kopyalandı")),
                );
                Navigator.pop(context);
              },
              child: Text("Kopyala"),
            ),
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: Text("Kapat"),
            ),
          ],
        );
      },
    );
  }

  @override
  void dispose() {
    controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("QR Okuyucu")),
      body: Column(
        children: [
          Expanded(
            flex: 2,
            child: isScanning
                ? QRView(
              key: qrKey,
              onQRViewCreated: _onQRViewCreated,
            )
                : Center(child: Text("QR kod taramak için butona basın.")),
          ),
          SizedBox(height: 20),
          ElevatedButton(
            onPressed: () {
              setState(() {
                isScanning = true;
              });
              controller?.resumeCamera();
            },
            child: Text("QR Tara"),
          ),
          if (scanResult.isNotEmpty) ...[
            SizedBox(height: 10),
            ElevatedButton(
              onPressed: () {
                setState(() {
                  urlToOpen = scanResult;
                });
              },
              child: Text("Siteyi Aç"),
            ),
          ],
          if (urlToOpen != null) ...[
            Expanded(
              flex: 3,
              child: WebViewWidget(
                controller: WebViewController()
                  ..setJavaScriptMode(JavaScriptMode.unrestricted)
                  ..loadRequest(Uri.parse(urlToOpen!)),
              ),
            ),
          ],
        ],
      ),
    );
  }
}
