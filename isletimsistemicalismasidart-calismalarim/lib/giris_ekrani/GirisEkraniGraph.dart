import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart';
import 'GirisEkraniScreen.dart';
import 'KayitOlusturScreen.dart';
import 'GuvenlikSorusuSecScreen.dart';
import 'SifremiUnuttumScreen.dart';

class GirisEkraniGraph extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      routerConfig: _router,
    );
  }

  final GoRouter _router = GoRouter(
    initialLocation: '/kayitOlustur', // Ana ekranda KayitOlusturScreen açılacak,
    routes: [
      GoRoute(
        path: '/girisYap',
        builder: (context, state) => GirisYapScreen(
          navigateToKayitOlustur: () => context.go('/kayitOlustur'),
          navigateToSifremiUnuttum: () => context.go('/sifremiUnuttum'),
          navigateToUygulamaEkrani: () => context.go('/uygulamaekrani'),
        ),
      ),
      GoRoute(
        path: '/kayitOlustur',
        builder: (context, state) => KayitOlusturScreen(navigateToGirisYap: () => context.go('/girisYap')),
      ),
      GoRoute(
        path: '/guvenlikSorusuSec',
        builder: (context, state) => GuvenlikSorusuSecScreen(navigateToKayitOlustur: () => context.go('/kayitOlustur')),
      ),
      GoRoute(
        path: '/sifremiUnuttum',
        builder: (context, state) => SifremiUnuttumScreen(navigateToKayitOlustur: () => context.go('/kayitOlustur')),
      ),
    ],
  );
}
