import 'package:flutter/material.dart';

import 'core/app_colors.dart';
import 'core/session_manager.dart';
import 'screens/auth/login_screen.dart';
import 'screens/home/home_screen.dart';

void main() {
  runApp(const EBiletApp());
}

/// Flutter uygulamasının başlangıç noktası.
class EBiletApp extends StatelessWidget {
  const EBiletApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'E-Bilet Flutter',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        scaffoldBackgroundColor: AppColors.background,
        colorScheme: ColorScheme.fromSeed(
          seedColor: AppColors.blue,
        ),
        useMaterial3: true,
      ),
      home: const SplashRouter(),
    );
  }
}

/// Uygulama açılırken kullanıcının daha önce giriş yapıp yapmadığını kontrol eder.
///
/// Giriş varsa:
/// HomeScreen
///
/// Giriş yoksa:
/// LoginScreen
class SplashRouter extends StatefulWidget {
  const SplashRouter({super.key});

  @override
  State<SplashRouter> createState() => _SplashRouterState();
}

class _SplashRouterState extends State<SplashRouter> {
  bool _isLoading = true;
  bool _isLoggedIn = false;

  @override
  void initState() {
    super.initState();
    _checkSession();
  }

  Future<void> _checkSession() async {
    final loggedIn = await SessionManager.isLoggedIn();

    if (!mounted) return;

    setState(() {
      _isLoggedIn = loggedIn;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      );
    }

    if (_isLoggedIn) {
      return const HomeScreen();
    }

    return const LoginScreen();
  }
}