import 'package:flutter/material.dart';
import 'package:flutter_chat_uygulama/view/registration_dialog.dart';
import 'package:flutter_chat_uygulama/view/single_chat_screen.dart';
import 'package:provider/provider.dart';

import '../util/app_config.dart';
import '../util/pref_manager.dart';
import '../viewmodel/mesajlar_viewmodel.dart';
import '../viewmodel/sohbet_listesi_viewmodel.dart';
import 'mesajlar_screen.dart';


class ChatAppContent extends StatefulWidget {
  const ChatAppContent({super.key});

  @override
  State<ChatAppContent> createState() => _ChatAppContentState();
}

class _ChatAppContentState extends State<ChatAppContent> {
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
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    // Registration dialog’ı ekran açılır açılmaz göstermek için
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

    // MultiProvider: ViewModel’lar app geneline
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => MesajlarViewModel()),
        ChangeNotifierProvider(create: (_) => SohbetListesiViewModel()),
      ],
      child: MaterialApp(
        debugShowCheckedModeBanner: false,
        initialRoute: '/mesajlar',
        routes: {
          '/mesajlar': (_) => const MesajlarScreen(),
        },
        onGenerateRoute: (settings) {
          if (settings.name == '/singleChat') {
            final args = settings.arguments as SingleChatArgs;
            return MaterialPageRoute(
              builder: (_) => SingleChatScreen(
                aliciId: args.aliciId,
                aliciAd: args.aliciAd,
              ),
            );
          }
          return null;
        },
      ),
    );
  }
}

class SingleChatArgs {
  final int aliciId;
  final String aliciAd;

  SingleChatArgs({required this.aliciId, required this.aliciAd});
}
