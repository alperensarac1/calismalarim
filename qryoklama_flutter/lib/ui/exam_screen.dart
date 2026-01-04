import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';
import '../service/api_client.dart';
import '../util/constants.dart';
import 'toast.dart';

class SinavWebViewScreen extends StatefulWidget {
  final String studentNo;
  const SinavWebViewScreen({super.key, required this.studentNo});

  @override
  State<SinavWebViewScreen> createState() => _SinavWebViewScreenState();
}

class _SinavWebViewScreenState extends State<SinavWebViewScreen> {
  late final WebViewController ctrl;
  bool loading = true;

  String? realUrl; // config sonrası

  @override
  void initState() {
    super.initState();

    ctrl = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setBackgroundColor(Colors.white)
      ..setNavigationDelegate(
        NavigationDelegate(
          onPageStarted: (_) => setState(() => loading = true),
          onPageFinished: (_) => setState(() => loading = false),
          onWebResourceError: (_) => setState(() => loading = false),
        ),
      );

    _loadConfigAndGo();
  }

  Future<void> _loadConfigAndGo() async {
    setState(() => loading = true);

    try {
      final api = ApiClient();
      final resp = await api.get(kExamConfigUrl);

      if (resp.statusCode < 200 || resp.statusCode >= 300) {
        AppToast.error("Config alınamadı (${resp.statusCode})");
        setState(() => loading = false);
        return;
      }

      final obj = jsonDecode(resp.body) as Map<String, dynamic>;
      final base = (obj["giris"] ?? "").toString();

      if (base.isEmpty) {
        AppToast.error("Config içinde 'giris' yok.");
        setState(() => loading = false);
        return;
      }

      final url = base + Uri.encodeComponent(widget.studentNo);
      realUrl = url;

      await ctrl.loadRequest(Uri.parse(url));
      setState(() => loading = false);
    } catch (e) {
      AppToast.error("Ayarlar alınamadı: $e");
      setState(() => loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Sınav Yeri"),
        actions: [
          IconButton(
            onPressed: _loadConfigAndGo,
            icon: const Icon(Icons.refresh),
            tooltip: "Yenile",
          ),
        ],
      ),
      body: Stack(
        children: [
          WebViewWidget(controller: ctrl),
          if (loading) const Center(child: CircularProgressIndicator()),
        ],
      ),
    );
  }
}
