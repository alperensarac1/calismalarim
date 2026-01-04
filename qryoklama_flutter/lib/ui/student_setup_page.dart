import 'package:flutter/material.dart';
import '../data/prefs.dart';
import 'scan_page.dart';
import 'toast.dart';

class StudentSetupPage extends StatefulWidget {
  const StudentSetupPage({super.key});

  @override
  State<StudentSetupPage> createState() => _StudentSetupPageState();
}

class _StudentSetupPageState extends State<StudentSetupPage> {
  final Prefs prefs = Prefs();
  final TextEditingController ctrl = TextEditingController();
  bool saving = false;

  Future<void> _save() async {
    final no = ctrl.text.trim();

    if (no.isEmpty) {
      AppToast.error("Öğrenci numarası gerekli");
      return;
    }

    setState(() => saving = true);
    try {
      await prefs.setStudentNo(no);

      if (!mounted) return;
      AppToast.success("Kaydedildi ✅");

      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => ScanPage(studentNo: no)),
      );
    } catch (e) {
      AppToast.error("Kaydedilemedi: $e");
    } finally {
      if (mounted) setState(() => saving = false);
    }
  }

  @override
  void dispose() {
    ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Öğrenci Girişi")),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            const SizedBox(height: 120),

            TextField(
              controller: ctrl,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                border: OutlineInputBorder(),
                hintText: "Öğrenci Numaranızı giriniz",
              ),
              onSubmitted: (_) => _save(),
            ),

            const SizedBox(height: 16),

            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: saving ? null : _save,
                child: saving
                    ? const SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
                    : const Text("KAYDET"),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
