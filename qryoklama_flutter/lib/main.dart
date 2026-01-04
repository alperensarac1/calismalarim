import 'package:flutter/material.dart';
import 'package:qryoklama_flutter/ui/student_setup_page.dart';
import 'data/prefs.dart';
import 'ui/scan_page.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const QRYoklamaApp());
}

class QRYoklamaApp extends StatelessWidget {
  const QRYoklamaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: "QR Yoklama",
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.deepPurple,
          brightness: Brightness.light,
        ),
        appBarTheme: const AppBarTheme(
          centerTitle: true,
        ),
      ),
      home: const Bootstrapper(),
    );
  }
}

/// İlk açılış yöneticisi
/// - öğrenci no varsa → ScanPage
/// - yoksa → StudentSetupPage
class Bootstrapper extends StatefulWidget {
  const Bootstrapper({super.key});

  @override
  State<Bootstrapper> createState() => _BootstrapperState();
}

class _BootstrapperState extends State<Bootstrapper> {
  final Prefs prefs = Prefs();
  String? studentNo;
  bool loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final no = await prefs.getStudentNo();
    if (!mounted) return;
    setState(() {
      studentNo = no;
      loading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (loading) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (studentNo == null) {
      return const StudentSetupPage();
    }

    return ScanPage(studentNo: studentNo!);
  }
}
