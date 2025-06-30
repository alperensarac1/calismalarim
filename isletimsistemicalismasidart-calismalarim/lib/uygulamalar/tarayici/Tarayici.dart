import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

class TarayiciEkrani extends StatefulWidget {
  @override
  _TarayiciEkraniState createState() => _TarayiciEkraniState();
}

class _TarayiciEkraniState extends State<TarayiciEkrani> {
  final TextEditingController _aramaController = TextEditingController();
  late final WebViewController _webViewController;

  @override
  void initState() {
    super.initState();
    _webViewController = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..loadRequest(Uri.parse("https://www.google.com")); // Varsayılan Google
  }

  void _aramaYap() {
    String aramaKelimesi = _aramaController.text.trim();
    if (aramaKelimesi.isNotEmpty) {
      String url = "https://www.google.com/search?q=${Uri.encodeComponent(aramaKelimesi)}";
      _webViewController.loadRequest(Uri.parse(url));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Tarayıcı")),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: TextField(
              controller: _aramaController,
              decoration: InputDecoration(
                labelText: "Arama Yap",
                suffixIcon: IconButton(
                  icon: Icon(Icons.search),
                  onPressed: _aramaYap,
                ),
                border: OutlineInputBorder(),
              ),
              onSubmitted: (_) => _aramaYap(),
            ),
          ),
          Expanded(
            child: WebViewWidget(controller: _webViewController),
          ),
        ],
      ),
    );
  }
}
