import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import 'entity/session_manager.dart';
import 'navigation/app_router.dart';


void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final SessionManager session = SessionManager();
  late final GoRouter _router = AppRouter.build(session);

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      debugShowCheckedModeBanner: false,
      routerConfig: _router,
    );
  }
}
