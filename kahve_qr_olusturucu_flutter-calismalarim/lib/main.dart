import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:kahve_qr_olusturucu/view/MainScreen.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'data/numara_kayit/NumaraKayitSp.dart';
import 'di/Providers.dart';


void main() async {
  WidgetsFlutterBinding.ensureInitialized(); // ✅ SharedPreferences kullanımı için gerekli
  final prefs = await SharedPreferences.getInstance();

  runApp(
    ProviderScope(
      overrides: [
        numaraKayitSpProvider.overrideWithValue(NumaraKayitSp(prefs)),
      ],
      child: const MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Kahve QR Oluşturucu',
      theme: ThemeData(
        primarySwatch: Colors.brown,
      ),
      home: MainScreen(),
    );
  }
}
