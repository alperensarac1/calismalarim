import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

class FullscreenWebOverlay extends StatefulWidget {
  final String title;
  final String initialUrl;

  const FullscreenWebOverlay({
    super.key,
    required this.title,
    required this.initialUrl,
  });

  @override
  State<FullscreenWebOverlay> createState() => _FullscreenWebOverlayState();
}

class _FullscreenWebOverlayState extends State<FullscreenWebOverlay> {
  late final WebViewController ctrl;
  bool loading = true;

  @override
  void initState() {
    super.initState();

    ctrl = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setBackgroundColor(Colors.white)
      ..setNavigationDelegate(
        NavigationDelegate(
          onPageStarted: (_) {
            if (mounted) setState(() => loading = true);
          },
          onPageFinished: (_) {
            if (mounted) setState(() => loading = false);
          },
          onWebResourceError: (err) {
            // sayfa hatası olursa loading kapat
            if (mounted) setState(() => loading = false);
          },
        ),
      )
      ..loadRequest(Uri.parse(widget.initialUrl));
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      insetPadding: const EdgeInsets.all(10),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(16),
        child: Column(
          children: [
            // Üst bar (close + title + refresh)
            Material(
              color: Colors.white,
              elevation: 1,
              child: SizedBox(
                height: 52,
                child: Row(
                  children: [
                    IconButton(
                      onPressed: () => Navigator.pop(context),
                      icon: const Icon(Icons.close),
                      tooltip: "Kapat",
                    ),
                    Expanded(
                      child: Text(
                        widget.title,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(fontWeight: FontWeight.w600),
                      ),
                    ),
                    IconButton(
                      onPressed: () => ctrl.reload(),
                      icon: const Icon(Icons.refresh),
                      tooltip: "Yenile",
                    ),
                  ],
                ),
              ),
            ),

            // WebView
            Expanded(
              child: Stack(
                children: [
                  WebViewWidget(controller: ctrl),
                  if (loading)
                    const Center(
                      child: CircularProgressIndicator(),
                    ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
