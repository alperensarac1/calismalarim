import 'package:flutter/material.dart';
import 'package:online_taksi_flutter/screens/splash_screen.dart';

void main() {
  runApp(const OnlineTaksiApp());
}

class OnlineTaksiApp extends StatelessWidget {
  const OnlineTaksiApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'onlinetaksi',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorSchemeSeed: Colors.yellow,
      ),
      home: const SplashScreen(),
    );
  }
}
