import 'package:adisyon_flutter/Tests.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'anaekran/MainScreen.dart';
import 'viewmodel/UrunViewModel.dart';
import 'viewmodel/MasalarViewModel.dart';


void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => UrunViewModel()),
        ChangeNotifierProvider(create: (_) => MasalarViewModel()),
        // İhtiyaca göre diğer ViewModel'lar da buraya eklenebilir
      ],
      child: MaterialApp(
        title: 'Flutter Demo',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
          useMaterial3: true,
        ),
        home: MainScreen(),
      ),
    );
  }
}
