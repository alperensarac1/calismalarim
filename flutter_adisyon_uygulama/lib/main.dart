import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:flutter_adisyon_uygulama/viewmodel/masalar_viewmodel.dart';
import 'package:flutter_adisyon_uygulama/viewmodel/urun_viewmodel.dart';
import 'package:flutter_adisyon_uygulama/viewmodel/masa_detay_viewmodel.dart';

import 'anaekran/main_screen.dart';
import 'masadetay/masa_detay_screen.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => MasalarViewModel()..masalariYukle()),
        ChangeNotifierProvider(create: (_) => UrunViewModel()..kategorileriYukle()),
        // MasaDetayViewModel masaId'ye göre route içinde kurulacak (ProxyProvider gerek yok)
      ],
      child: MaterialApp(
        title: 'Adisyon',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
          useMaterial3: true,
        ),

        // Ana ekran
        initialRoute: '/',
        routes: {
          '/': (context) => MainScreen(
            onNavigateToMasaDetay: (masaId) {
              Navigator.pushNamed(context, '/masaDetay', arguments: masaId);
            },
          ),
        },

        // masaId gibi argümanlı route’lar
        onGenerateRoute: (settings) {
          if (settings.name == '/masaDetay') {
            final masaId = (settings.arguments as int);

            return MaterialPageRoute(
            builder: (context) => ChangeNotifierProvider(
              create: (_) => MasaDetayViewModel(masaId: masaId)..yukleTumVeriler(),
              child: MasaDetayScreen(
                masaId: masaId,
                onNavigateBack: () => Navigator.pop(context),
              ),
            ),
            );

          }
          return null;
        },
      ),
    );
  }
}
