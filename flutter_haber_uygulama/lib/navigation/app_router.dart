// navigation/app_router.dart
import 'package:flutter/material.dart';

import '../model/haber_model.dart';
import '../view/anasayfa_screen.dart';
import '../view/haberdetay_screen.dart';
import '../view/kategori_screen.dart';

class AppRoutes {
  static const anasayfa = '/';
  static const detay = '/detay';
  static const kategori = '/kategori';
}

class AppRouter {
  static Route<dynamic> onGenerateRoute(RouteSettings settings) {
    switch (settings.name) {
      case AppRoutes.anasayfa:
        return MaterialPageRoute(builder: (_) => const AnasayfaScreen());

      case AppRoutes.detay:
        final haber = settings.arguments as HaberModel?;
        if (haber == null) {
          return MaterialPageRoute(
            builder: (_) => const Scaffold(body: Center(child: Text("Haber bulunamadı!"))),
          );
        }
        return MaterialPageRoute(builder: (_) => HaberDetayScreen(haber: haber));

      case AppRoutes.kategori:
        final kategoriId = settings.arguments as int?;
        if (kategoriId == null) {
          return MaterialPageRoute(
            builder: (_) => const Scaffold(body: Center(child: Text("Kategori bulunamadı!"))),
          );
        }
        return MaterialPageRoute(builder: (_) => KategoriScreen(kategoriId: kategoriId));

      default:
        return MaterialPageRoute(
          builder: (_) => const Scaffold(body: Center(child: Text("Sayfa bulunamadı"))),
        );
    }
  }
}
