import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'main_screen.dart';
import 'prestige_shop_screen.dart';

void main() {
  runApp(const ProviderScope(child: CookieApp()));
}

class CookieApp extends StatelessWidget {
  const CookieApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFFCE9B62)),
      ),
      home: const MainScreen(),
      routes: {
        '/shop': (_) => PrestigeShopScreen(onBack: () {}),
      },
    );
  }
}
