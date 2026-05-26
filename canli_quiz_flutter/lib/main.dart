import 'package:flutter/material.dart';
import 'screens/home_screen.dart';

void main() {
  runApp(const CanliQuizApp());
}

class CanliQuizApp extends StatelessWidget {
  const CanliQuizApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: "Canlı Quiz",
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorSchemeSeed: Colors.deepPurple,
        scaffoldBackgroundColor: const Color(0xFFF8FAFC),
      ),
      home: const HomeScreen(),
    );
  }
}