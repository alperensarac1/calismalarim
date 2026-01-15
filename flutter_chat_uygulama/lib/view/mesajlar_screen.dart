import 'package:flutter/material.dart';
import 'package:flutter_chat_uygulama/view/single_chat_screen.dart';
import 'package:provider/provider.dart';

import '../service/api_service.dart';
import '../util/app_config.dart';

import '../viewmodel/sohbet_listesi_viewmodel.dart';
import 'chat_app_content.dart';

class MesajlarScreen extends StatefulWidget {
  const MesajlarScreen({super.key});

  @override
  State<MesajlarScreen> createState() => _MesajlarScreenState();
}

class _MesajlarScreenState extends State<MesajlarScreen> {
  final _api = ApiService();
  String? _secilenNumara;

  @override
  void initState() {
    super.initState();
    // LaunchedEffect(Unit) karşılığı
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<SohbetListesiViewModel>()
          .sohbetListesiniBaslat(kullaniciId: AppConfig.kullaniciId);
    });
  }

  Future<void> _numaradanChatAc(String numara) async {
    try {
      final resp = await _api.kullanicilariGetir();
      if (resp.success) {
        final kisi = resp.kullanicilar
            .where((k) => k.numara == numara)
            .cast()
            .toList();

        if (kisi.isNotEmpty) {
          final found = kisi.first;
          if (!mounted) return;
          Navigator.pushNamed(
            context,
            SingleChatScreen.routeName,
            arguments: SingleChatArgs(aliciId: found.id, aliciAd: found.ad),
          );

        } else {
          if (!mounted) return;
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Bu numara kayıtlı değil')),
          );
        }
      }
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Sunucu hatası: $e')),
      );
    } finally {
      _secilenNumara = null;
    }
  }

  Future<void> _showYeniKisiDialog() async {
    final numara = await showDialog<String>(
      context: context,
      builder: (_) => const YeniKisiDialog(),
    );

    if (numara != null && numara.trim().isNotEmpty) {
      _secilenNumara = numara.trim();
      await _numaradanChatAc(_secilenNumara!);
    }
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<SohbetListesiViewModel>();

    // Compose snackbarHostState.showSnackbar(hata)
    if (vm.hataMesaji != null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        final msg = vm.hataMesaji!;
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
      });
    }

    return Scaffold(
      appBar: AppBar(title: const Text('Mesajlar')),
      floatingActionButton: FloatingActionButton(
        onPressed: _showYeniKisiDialog,
        child: const Icon(Icons.add),
      ),
      body: ListView.separated(
        itemCount: vm.konusulanKisiler.length,
        separatorBuilder: (_, __) => const Divider(height: 1),
        itemBuilder: (context, index) {
          final kisi = vm.konusulanKisiler[index];
          return ListTile(
            title: Text(kisi.ad),
            subtitle: Text(kisi.sonMesaj),
            trailing: Text(kisi.tarih, maxLines: 1, overflow: TextOverflow.ellipsis),
            onTap: () async {
              // Compose: secilenNumara = it.numara -> LaunchedEffect tetikleniyor
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(content: Text('${kisi.ad} seçildi')),
              );
              await _numaradanChatAc(kisi.numara);
            },
          );
        },
      ),
    );
  }
}

class YeniKisiDialog extends StatefulWidget {
  const YeniKisiDialog({super.key});

  @override
  State<YeniKisiDialog> createState() => _YeniKisiDialogState();
}

class _YeniKisiDialogState extends State<YeniKisiDialog> {
  final _controller = TextEditingController();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Yeni Mesaj'),
      content: TextField(
        controller: _controller,
        keyboardType: TextInputType.phone,
        decoration: const InputDecoration(labelText: 'Alıcı numarası'),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('İptal'),
        ),
        TextButton(
          onPressed: () {
            final numara = _controller.text.trim();
            if (numara.isEmpty) {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Numara girilmedi')),
              );
              return;
            }
            Navigator.pop(context, numara);
          },
          child: const Text('Gönder'),
        ),
      ],
    );
  }
}
