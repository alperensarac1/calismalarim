import 'package:eticaret_flutter/viewmodel/auth_vm.dart';
import 'package:eticaret_flutter/viewmodel/home_vm.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'api/api_client.dart';
import 'api/token_store.dart';
import 'auth/login_page.dart';
import 'model/auth_service.dart';
import 'model/cart_service.dart';
import 'model/orders_service.dart';
import 'model/product_service.dart';


class App extends StatelessWidget {
  const App({super.key});

  @override
  Widget build(BuildContext context) {
    final tokenStore = TokenStore();
    final api = ApiClient(tokenStore: tokenStore);

    return MultiProvider(
      providers: [
        Provider.value(value: tokenStore),
        Provider.value(value: api),

        Provider(create: (_) => AuthService(api, tokenStore)),
        ChangeNotifierProvider(create: (c) => AuthVm(c.read<AuthService>())),

        Provider(create: (_) => ProductService(api)),
        ChangeNotifierProvider(create: (c) => HomeVm(c.read<ProductService>())),

        Provider(create: (_) => CartService(api)),
        Provider(create: (_) => OrdersService(api)),
      ],
      child: MaterialApp(
        debugShowCheckedModeBanner: false,
        title: "E-Ticaret",
        theme: ThemeData(useMaterial3: true),
        home: const LoginPage(), // login -> mainShell
      ),
    );
  }
}
