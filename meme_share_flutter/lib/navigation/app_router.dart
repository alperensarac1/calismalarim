import 'package:flutter/material.dart';

import '../view/login_screen.dart';
import '../view/register_screen.dart';
import '../view/anasayfa_screen.dart';
import '../view/oda_screen.dart';
import 'routes.dart';

class AppRouter {
  static Route<dynamic> onGenerateRoute(RouteSettings settings) {
    final name = settings.name ?? Routes.login;
    final uri = Uri.parse(name);

    // /login
    if (uri.path == Routes.login) {
      return MaterialPageRoute(
        builder: (context) => LoginScreen(
          onNavigateRegister: () => Navigator.of(context).pushNamed(Routes.register),
          onLoginSuccess: (userId) {
            Navigator.of(context).pushNamedAndRemoveUntil(
              Routes.homePath(userId),
                  (route) => false,
            );
          },
        ),
      );
    }

    // /register
    if (uri.path == Routes.register) {
      return MaterialPageRoute(
        builder: (context) => RegisterScreen(
          onNavigateLogin: () => Navigator.of(context).pushNamedAndRemoveUntil(Routes.login, (r) => false),
          onRegisterSuccess: () => Navigator.of(context).pushNamedAndRemoveUntil(Routes.login, (r) => false),
        ),
      );
    }

    // /home/{userId}
    if (uri.pathSegments.isNotEmpty && uri.pathSegments.first == 'home') {
      final userId = uri.pathSegments.length >= 2 ? int.tryParse(uri.pathSegments[1]) ?? 0 : 0;

      return MaterialPageRoute(
        builder: (context) => AnasayfaScreen(
          userId: userId,
          onOpenRoom: (roomId, uid) => Navigator.of(context).pushNamed(Routes.odaPath(roomId, uid)),
        ),
      );
    }

    // /oda/{roomId}/{userId}
    if (uri.pathSegments.isNotEmpty && uri.pathSegments.first == 'oda') {
      final roomId = uri.pathSegments.length >= 2 ? int.tryParse(uri.pathSegments[1]) ?? 0 : 0;
      final userId = uri.pathSegments.length >= 3 ? int.tryParse(uri.pathSegments[2]) ?? 0 : 0;

      return MaterialPageRoute(
        builder: (_) => OdaScreen(roomId: roomId, userId: userId),
      );
    }

    // fallback
    return MaterialPageRoute(
      builder: (_) => const Scaffold(
        body: Center(child: Text('404 - Route bulunamadı')),
      ),
    );
  }
}
