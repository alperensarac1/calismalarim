import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../util/constants.dart';


class StudentAttendanceScreen extends StatefulWidget {
  final String studentNo;
  const StudentAttendanceScreen({super.key, required this.studentNo});

  @override
  State<StudentAttendanceScreen> createState() => _StudentAttendanceScreenState();
}

class _StudentAttendanceScreenState extends State<StudentAttendanceScreen> {
  late final WebViewController ctrl;
  bool loading = true;

  @override
  void initState() {
    super.initState();

    final url = Uri.parse(kAttendanceUrl).replace(queryParameters: {
      "student_no": widget.studentNo,
    });

    ctrl = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setBackgroundColor(Colors.white)
      ..setNavigationDelegate(
        NavigationDelegate(
          onPageStarted: (_) => setState(() => loading = true),
          onPageFinished: (_) => setState(() => loading = false),
          onWebResourceError: (_) => setState(() => loading = false),
        ),
      )
      ..loadRequest(url);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Yoklama"),
        actions: [
          IconButton(
            onPressed: () => ctrl.reload(),
            icon: const Icon(Icons.refresh),
            tooltip: "Yenile",
          )
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
