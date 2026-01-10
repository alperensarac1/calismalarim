// view/anasayfa_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../components/haber_card.dart';
import '../viewmodel/haberler_viewmodel.dart';
import 'haberdetay_screen.dart';
import 'kategori_screen.dart';


class AnasayfaScreen extends StatefulWidget {
  const AnasayfaScreen({super.key});

  @override
  State<AnasayfaScreen> createState() => _AnasayfaScreenState();
}

class _AnasayfaScreenState extends State<AnasayfaScreen> {
  bool _loaded = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (!_loaded) {
      _loaded = true;
      final vm = context.read<HaberlerViewModel>();
      vm.loadSonDakikaHaberler();
      vm.loadGundemHaberler();
      vm.loadKategoriler();
    }
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<HaberlerViewModel>();

    return Scaffold(
      appBar: AppBar(title: const Text('Haberler')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            const Text(
              "SON DAKİKA",
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFFC65555)),
            ),
            const SizedBox(height: 8),
            SizedBox(
              height: 260,
              child: vm.sonDakikaHaberler.isNotEmpty
                  ? ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: vm.sonDakikaHaberler.length,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                itemBuilder: (context, i) {
                  final haber = vm.sonDakikaHaberler[i];
                  return HaberCard(
                    haber: haber,
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => HaberDetayScreen(haber: haber),
                        ),
                      );
                    },
                  );
                },
              )
                  : const Center(child: Text("Son dakika haberleri yükleniyor...")),
            ),
            const SizedBox(height: 24),
            const Text(
              "GÜNDEM",
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFFC65555)),
            ),
            const SizedBox(height: 8),
            SizedBox(
              height: 260,
              child: vm.gundemHaberler.isNotEmpty
                  ? ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: vm.gundemHaberler.length,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                itemBuilder: (context, i) {
                  final haber = vm.gundemHaberler[i];
                  return HaberCard(
                    haber: haber,
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => HaberDetayScreen(haber: haber),
                        ),
                      );
                    },
                  );
                },
              )
                  : const Center(child: Text("Gündem haberleri yükleniyor...")),
            ),
            const SizedBox(height: 24),
            const Text(
              "KATEGORİLER",
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFFC65555)),
            ),
            const SizedBox(height: 8),
            vm.kategoriler.isNotEmpty
                ? ListView.separated(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: vm.kategoriler.length,
              separatorBuilder: (_, __) => const SizedBox(height: 4),
              itemBuilder: (context, i) {
                final kat = vm.kategoriler[i];
                return ListTile(
                  title: Text(kat.tur_adi, style: const TextStyle(fontSize: 18)),
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => KategoriScreen(kategoriId: kat.id),
                      ),
                    );
                  },
                );
              },
            )
                : const Text("Kategoriler yükleniyor..."),
          ],
        ),
      ),
    );
  }
}
