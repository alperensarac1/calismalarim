import 'package:flutter/material.dart';
import 'package:meme_share_flutter/service/meme_service.dart';
import 'package:provider/provider.dart';

import 'navigation/app_router.dart';
import 'navigation/routes.dart';
import 'service/api_client.dart';
import 'viewmodel/login_vm.dart';
import 'viewmodel/register_vm.dart';
import 'viewmodel/oda_vm.dart';

void main() {
  final dio = ApiClient.createDio();
  final api = MemeApiService(dio);

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => LoginVM(api: api)),
        ChangeNotifierProvider(create: (_) => RegisterVM(api: api)),
        ChangeNotifierProvider(create: (_) => OdaVM(api: api)),
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
      title: 'Meme Share',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      initialRoute: Routes.login,
      onGenerateRoute: AppRouter.onGenerateRoute,
    );
  }
}
