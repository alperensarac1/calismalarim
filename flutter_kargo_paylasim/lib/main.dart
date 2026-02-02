import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'service/api_client.dart';
import 'storage/token_store.dart';
import 'viewmodel/home_vm.dart';
import 'view/home_screen.dart';
import 'view/login_screen.dart';

void main() {
  runApp(const AppRoot());
}

class AppRoot extends StatefulWidget {
  const AppRoot({super.key});
  @override
  State<AppRoot> createState() => _AppRootState();
}

class _AppRootState extends State<AppRoot> {
  final tokenStore = TokenStore();
  late final api = ApiClient(tokenStore);

  String? token;
  bool loading = true;

  @override
  void initState() {
    super.initState();
    tokenStore.getToken().then((t) {
      setState(() {
        token = t;
        loading = false;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    if (loading) {
      return const MaterialApp(home: Scaffold(body: Center(child: CircularProgressIndicator())));
    }

    return MultiProvider(
      providers: [
        Provider.value(value: api),
        ChangeNotifierProvider(create: (_) => HomeVM(api)),
      ],
      child: MaterialApp(
        debugShowCheckedModeBanner: false,
        home: (token == null || token!.isEmpty)
            ? LoginScreen(onLoggedIn: () async {
          final t = await tokenStore.getToken();
          setState(() => token = t);
        })
            : const HomeScreen(),
      ),
    );
  }
}
