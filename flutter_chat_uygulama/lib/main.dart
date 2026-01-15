import 'package:flutter/material.dart';
import 'package:flutter_chat_uygulama/viewmodel/mesajlar_viewmodel.dart';
import 'package:flutter_chat_uygulama/viewmodel/sohbet_listesi_viewmodel.dart';
import 'package:provider/provider.dart';

import 'util/app_config.dart';
import 'util/pref_manager.dart';


import 'view/mesajlar_screen.dart';
import 'view/single_chat_screen.dart';
import 'view/registration_dialog.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Chat',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const BootGate(),
      onGenerateRoute: (settings) {
        if (settings.name == SingleChatScreen.routeName) {
          final args = settings.arguments as SingleChatArgs;
          return MaterialPageRoute(
            builder: (_) => SingleChatScreen(aliciId: args.aliciId, aliciAd: args.aliciAd),
          );
        }
        return null;
      },
    );
  }
}

/// Uygulama açılışında kullanıcı var mı kontrol eder.
/// Varsa ana app'e geçer, yoksa kayıt dialogu gösterir.
class BootGate extends StatefulWidget {
  const BootGate({super.key});

  @override
  State<BootGate> createState() => _BootGateState();
}

class _BootGateState extends State<BootGate> {
  bool _loading = true;
  bool _showRegister = false;

  @override
  void initState() {
    super.initState();
    _init();
  }

  Future<void> _init() async {
    final pref = PrefManager();
    final hasUser = await pref.kullaniciVarMi();
    final id = await pref.getirKullaniciId();

    if (hasUser && id != -1) {
      AppConfig.kullaniciId = id;
      _showRegister = false;
    } else {
      _showRegister = true;
    }

    setState(() => _loading = false);
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (_showRegister) {
      return Scaffold(
        body: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 520),
            child: RegistrationDialog(
              onKayitBasarili: (id) async {
                AppConfig.kullaniciId = id;
                await PrefManager().kaydetKullaniciId(id);
                setState(() => _showRegister = false);
              },
            ),
          ),
        ),
      );
    }

    // Kullanıcı varsa -> Provider'lı app
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => MesajlarViewModel()),
        ChangeNotifierProvider(create: (_) => SohbetListesiViewModel()),
      ],
      child: const MesajlarScreen(),
    );
  }
}

/// Navigator pushNamed için argüman
class SingleChatArgs {
  final int aliciId;
  final String aliciAd;

  SingleChatArgs({required this.aliciId, required this.aliciAd});
}
