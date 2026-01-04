import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import 'package:qryoklama_flutter/ui/toast.dart';
import 'package:qryoklama_flutter/ui/web_overlay.dart';

import '../data/prefs.dart';
import '../service/api_client.dart';
import '../service/attendance_service.dart';
import '../service/device_service.dart';
import '../service/location_service.dart';
import '../service/qr_parser.dart';
import '../util/constants.dart';

class ScanPage extends StatefulWidget {
  final String studentNo;
  const ScanPage({super.key, required this.studentNo});

  @override
  State<ScanPage> createState() => _ScanPageState();
}

class _ScanPageState extends State<ScanPage> {
  final MobileScannerController scanner = MobileScannerController(
    detectionSpeed: DetectionSpeed.normal,
    facing: CameraFacing.back,
  );

  late final AttendanceService attendance;

  bool isSending = false;

  // debounce
  String? lastText;
  DateTime? lastTs;
  static const Duration debounce = Duration(milliseconds: 1200);

  @override
  void initState() {
    super.initState();

    final prefs = Prefs();
    attendance = AttendanceService(
      api: ApiClient(),
      device: DeviceService(prefs),
      location: LocationService(),
      qr: QRParser(),
    );
  }

  Future<void> _sendQR(String raw) async {
    if (isSending) return;

    setState(() => isSending = true);
    await scanner.stop(); // ✅ QR okundu -> kamera durdur (Android pause)

    try {
      final res = await attendance.sendByQR(
        studentNo: widget.studentNo,
        qrRaw: raw,
      );

      if (!mounted) return;

      if (res.isOk) {
        AppToast.success("Yoklama alındı ✅");
      } else {
        AppToast.error(res.error!);
      }
    } finally {
      if (mounted) setState(() => isSending = false);
      await scanner.start(); // ✅ her durumda devam et
    }
  }

  Future<void> _sendCode(String code) async {
    final c = code.trim();
    if (c.isEmpty) {
      AppToast.error("Kod boş olamaz");
      return;
    }
    if (isSending) return;

    setState(() => isSending = true);
    await scanner.stop(); // ✅ gönderimde kamera dursun

    try {
      final res = await attendance.sendByCode(
        studentNo: widget.studentNo,
        joinCode: c,
      );

      if (!mounted) return;

      if (res.isOk) {
        AppToast.success("Yoklama alındı ✅");
      } else {
        AppToast.error(res.error!);
      }
    } finally {
      if (mounted) setState(() => isSending = false);
      await scanner.start();
    }
  }

  Future<void> _openAttendance() async {
    await scanner.stop();

    final url = Uri.parse(kAttendanceUrl).replace(queryParameters: {
      "student_no": widget.studentNo,
    }).toString();

    await showDialog(
      context: context,
      barrierDismissible: false,
      builder: (_) => FullscreenWebOverlay(
        title: "Yoklama",
        initialUrl: url,
      ),
    );

    await scanner.start(); // ✅ resume
  }

  Future<void> _openExam() async {
    await scanner.stop(); // ✅ pause
    setState(() => isSending = true);

    try {
      final resp = await ApiClient().get(kExamConfigUrl);
      if (resp.statusCode < 200 || resp.statusCode >= 300) {
        _showDialog("Hata (${resp.statusCode})", resp.body);
        return;
      }

      final obj = jsonDecode(resp.body) as Map<String, dynamic>;
      final base = (obj["giris"] ?? "").toString();
      if (base.isEmpty) {
        _showDialog("Hata", "Config içinde 'giris' yok.");
        return;
      }

      final url = base + Uri.encodeComponent(widget.studentNo);

      await showDialog(
        context: context,
        barrierDismissible: false,
        builder: (_) => FullscreenWebOverlay(
          title: "Sınav Yeri",
          initialUrl: url,
        ),
      );
    } catch (e) {
      _showDialog("Hata", "Ayarlar alınamadı: $e");
    } finally {
      if (mounted) setState(() => isSending = false);
      await scanner.start();
    }
  }

  Future<void> _codeDialog() async {
    await scanner.stop();
    final ctrl = TextEditingController();

    final code = await showDialog<String>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text("Kod Gönder"),
        content: TextField(
          controller: ctrl,
          decoration: const InputDecoration(hintText: "6 haneli kod"),
          keyboardType: TextInputType.number,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text("İptal"),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, ctrl.text.trim()),
            child: const Text("Gönder"),
          ),
        ],
      ),
    );

    await scanner.start();

    if (code != null) {
      await _sendCode(code);
    }
  }

  void _showDialog(String title, String msg) {
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        title: Text(title),
        content: SingleChildScrollView(child: Text(msg)),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text("Tamam"),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          MobileScanner(
            controller: scanner,
            onDetect: (capture) {
              if (isSending) return;

              final bcs = capture.barcodes;
              if (bcs.isEmpty) return;
              final txt = bcs.first.rawValue;
              if (txt == null) return;

              final now = DateTime.now();
              if (lastText == txt &&
                  lastTs != null &&
                  now.difference(lastTs!) < debounce) {
                return;
              }

              lastText = txt;
              lastTs = now;

              _sendQR(txt);
            },
          ),

          Positioned(
            top: 50,
            left: 0,
            right: 0,
            child: Center(
              child: FilledButton(
                onPressed: isSending ? null : _openExam,
                child: const Text("Sınav Yeri Sorgula"),
              ),
            ),
          ),

          Positioned(
            left: 24,
            bottom: 24,
            child: FloatingActionButton(
              heroTag: "fab_att",
              onPressed: isSending ? null : _openAttendance,
              child: const Icon(Icons.table_chart),
            ),
          ),

          // Bottom-right FAB
          Positioned(
            right: 24,
            bottom: 24,
            child: FloatingActionButton(
              heroTag: "fab_code",
              onPressed: isSending ? null : _codeDialog,
              child: const Icon(Icons.message),
            ),
          ),

          if (isSending)
            Positioned.fill(
              child: Container(
                color: Colors.black26,
                child: const Center(child: CircularProgressIndicator()),
              ),
            ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    scanner.dispose();
    super.dispose();
  }
}
