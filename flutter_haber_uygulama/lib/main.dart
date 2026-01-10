import 'package:flutter/material.dart';
import 'package:flutter_haber_uygulama/viewmodel/haberdetay_viewmodel.dart';
import 'package:provider/provider.dart';

import 'dao/haber_dao.dart';
import 'navigation/app_router.dart';
import 'viewmodel/haberler_viewmodel.dart';

void main() {
  runApp(
    MultiProvider(
      providers: [
        Provider(create: (_) => HaberDao()),
        ChangeNotifierProvider(create: (ctx) => HaberlerViewModel(dao: ctx.read<HaberDao>())),
        ChangeNotifierProvider(create: (ctx) => HaberDetayViewModel(dao: ctx.read<HaberDao>())),
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
      initialRoute: AppRoutes.anasayfa,
      onGenerateRoute: AppRouter.onGenerateRoute,
    );
  }
}
