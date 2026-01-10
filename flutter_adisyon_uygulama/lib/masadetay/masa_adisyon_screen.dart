import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../utils/extension.dart';
import '../viewmodel/masa_detay_viewmodel.dart';

class MasaAdisyonScreen extends StatefulWidget {
  final int masaId;
  const MasaAdisyonScreen({super.key, required this.masaId});

  @override
  State<MasaAdisyonScreen> createState() => _MasaAdisyonScreenState();
}

class _MasaAdisyonScreenState extends State<MasaAdisyonScreen> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() => context.read<MasaDetayViewModel>().yukleTumVeriler());
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<MasaDetayViewModel>();

    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              Expanded(
                child: ListView.builder(
                  itemCount: vm.urunler.length,
                  itemBuilder: (_, i) {
                    final u = vm.urunler[i];
                    return Padding(
                      padding: const EdgeInsets.symmetric(vertical: 4),
                      child: Text(
                        '${u.urunAd} (Adet: ${u.adet})',
                        style: const TextStyle(fontSize: 16),
                      ),
                    );
                  },
                ),
              ),
              const SizedBox(height: 16),
              Column(
                children: [
                  Text(
                    'Toplam: ${vm.toplamFiyat.fiyatYaz()}',
                    textAlign: TextAlign.center,
                    style: const TextStyle(fontSize: 20),
                  ),
                  const SizedBox(height: 12),
                  ElevatedButton(
                    onPressed: () async {
                      await vm.odemeAl(onSuccess: () {});
                      if (!context.mounted) return;
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Ödeme alındı')),
                      );
                      await vm.yukleTumVeriler();
                    },
                    child: const Text('Ödeme Al'),
                  ),
                ],
              )
            ],
          ),
        ),
      ),
    );
  }
}
