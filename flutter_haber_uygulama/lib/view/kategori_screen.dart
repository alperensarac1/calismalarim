// view/kategori_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../dao/haber_dao.dart';
import '../viewmodel/kategoriler_viewmodel.dart';
import 'haberdetay_screen.dart';

class KategoriScreen extends StatefulWidget {
  final int kategoriId;

  const KategoriScreen({super.key, required this.kategoriId});

  @override
  State<KategoriScreen> createState() => _KategoriScreenState();
}

class _KategoriScreenState extends State<KategoriScreen> {
  bool _loaded = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (!_loaded) {
      _loaded = true;
      // Bu VM constructor'da required dao istiyordu; burada provider üzerinden alıp başlatacağız.
      final dao = context.read<HaberDao>();
      final vm = KategorilerViewModel(haberDao: dao);
      // local provider gibi kullanmak için route içinde ChangeNotifierProvider ile sarmalayacağız
    }
  }

  @override
  Widget build(BuildContext context) {
    final dao = context.read<HaberDao>();

    return ChangeNotifierProvider(
      create: (_) => KategorilerViewModel(haberDao: dao)..loadKategoriHaberleri(widget.kategoriId),
      child: Consumer<KategorilerViewModel>(
        builder: (context, vm, _) {
          return Scaffold(
            appBar: AppBar(title: Text("Kategori: ${widget.kategoriId}")),
            body: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
              child: vm.loading
                  ? const Center(child: CircularProgressIndicator())
                  : ListView.separated(
                itemCount: vm.kategoriHaberleri.length,
                separatorBuilder: (_, __) => const SizedBox(height: 8),
                itemBuilder: (context, i) {
                  final haber = vm.kategoriHaberleri[i];
                  return Card(
                    child: InkWell(
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(builder: (_) => HaberDetayScreen(haber: haber)),
                        );
                      },
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(haber.baslik, style: const TextStyle(fontWeight: FontWeight.bold)),
                            const SizedBox(height: 4),
                            Text(
                              (haber.icerik.length > 100)
                                  ? "${haber.icerik.substring(0, 100)}..."
                                  : haber.icerik,
                              style: const TextStyle(color: Colors.grey),
                            ),
                          ],
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),
          );
        },
      ),
    );
  }
}
