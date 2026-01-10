import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../model/urun.dart';
import '../utils/extension.dart';
import '../viewmodel/masa_detay_viewmodel.dart';

class MasaDetayScreen extends StatefulWidget {
  final int masaId;
  final VoidCallback onNavigateBack;

  const MasaDetayScreen({
    super.key,
    required this.masaId,
    required this.onNavigateBack,
  });

  @override
  State<MasaDetayScreen> createState() => _MasaDetayScreenState();
}

class _MasaDetayScreenState extends State<MasaDetayScreen> {
  int seciliKategoriIndex = 0; // 0 = Tümü

  @override
  void initState() {
    super.initState();
    Future.microtask(() async {
      await context.read<MasaDetayViewModel>().yukleTumVeriler();
      // Eğer ayrıca UrunViewModel kullanıyorsan:
      // await context.read<UrunViewModel>().urunleriYukle();
      // await context.read<UrunViewModel>().kategorileriYukle();
    });
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<MasaDetayViewModel>();

    final kategoriler = vm.kategoriler;
    final tumUrunler = vm.tumUrunler;

    final filtreliUrunler = (seciliKategoriIndex == 0)
        ? tumUrunler
        : tumUrunler.where((u) {
      final secKat = kategoriler.elementAt(seciliKategoriIndex - 1);
      return u.urunKategori.id == secKat.id;
    }).toList();

    return Scaffold(
      backgroundColor: const Color(0xFFFFBABA),
      body: SafeArea(
        child: Column(
          children: [
            // HEADER 60px
            Container(
              height: 60,
              color: Colors.white,
              padding: const EdgeInsets.symmetric(horizontal: 12),
              child: Row(
                children: [
                  InkWell(
                    onTap: widget.onNavigateBack,
                    child: const Padding(
                      padding: EdgeInsets.only(right: 16),
                      child: Text('←', style: TextStyle(fontSize: 24)),
                    ),
                  ),
                  Text(
                    vm.masa != null ? 'Masa ${vm.masa!.id}' : 'Masa',
                    style: const TextStyle(fontSize: 20),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 4),

            // BODY
            Expanded(
              child: Row(
                children: [
                  // SOL PANEL (200)
                  Container(
                    width: 200,
                    color: Colors.white,
                    padding: const EdgeInsets.all(8),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Masa Ürünleri', style: TextStyle(fontSize: 18)),
                        const SizedBox(height: 8),

                        Expanded(
                          child: vm.urunler.isEmpty
                              ? const Text(
                            'Henüz ürün eklenmemiş.',
                            style: TextStyle(color: Colors.grey),
                          )
                              : ListView.separated(
                            itemCount: vm.urunler.length,
                            separatorBuilder: (_, __) => const SizedBox(height: 6),
                            itemBuilder: (_, i) {
                              final mu = vm.urunler[i];
                              return Text(
                                '${mu.urunAd} (adet: ${mu.adet})',
                                style: const TextStyle(fontSize: 16, color: Colors.black54),
                              );
                            },
                          ),
                        ),

                        const SizedBox(height: 8),
                        const Divider(height: 1),
                        const SizedBox(height: 8),

                        Text(
                          'Toplam: ${vm.toplamFiyat.fiyatYaz()}',
                          style: const TextStyle(fontSize: 18),
                        ),
                        const SizedBox(height: 8),

                        SizedBox(
                          width: double.infinity,
                          child: ElevatedButton(
                            onPressed: () async {
                              await vm.odemeAl(onSuccess: () {});
                              if (!context.mounted) return;
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(content: Text('Ödeme tamamlandı')),
                              );
                              await vm.yukleTumVeriler();
                              // Compose'daki gibi ödeme sonrası dönmek istersen:
                              // widget.onNavigateBack();
                            },
                            child: const Text('Ödeme Al'),
                          ),
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(width: 8),

                  // SAĞ PANEL
                  Expanded(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 8),
                      child: Column(
                        children: [
                          // KATEGORİ SATIRI (56px) - Compose'ta LazyColumn ile yapılmıştı
                          SizedBox(
                            height: 56,
                            child: ListView(
                              scrollDirection: Axis.horizontal,
                              children: [
                                _KategoriChip(
                                  text: 'Tümü',
                                  selected: seciliKategoriIndex == 0,
                                  onTap: () => setState(() => seciliKategoriIndex = 0),
                                ),
                                for (int i = 0; i < kategoriler.length; i++)
                                  _KategoriChip(
                                    text: kategoriler[i].kategoriAd,
                                    selected: seciliKategoriIndex == i + 1,
                                    onTap: () => setState(() => seciliKategoriIndex = i + 1),
                                  ),
                              ],
                            ),
                          ),

                          const SizedBox(height: 4),

                          Expanded(
                            child: filtreliUrunler.isEmpty
                                ? const Center(
                              child: Text(
                                'Bu kategoride ürün bulunamadı.',
                                style: TextStyle(color: Colors.grey, fontSize: 16),
                              ),
                            )
                                : GridView.builder(
                              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                                crossAxisCount: 3,
                                crossAxisSpacing: 8,
                                mainAxisSpacing: 8,
                                childAspectRatio: 0.75,
                              ),
                              itemCount: filtreliUrunler.length,
                              itemBuilder: (_, i) {
                                final urun = filtreliUrunler[i];
                                return _UrunCard(
                                  urun: urun,
                                  onPlus: () async {
                                    await vm.urunEkle(urun.id);
                                  },
                                  onMinus: () async {
                                    if (urun.urunAdet > 0) {
                                      await vm.urunCikar(urun.id);
                                    }
                                  },
                                );
                              },
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _KategoriChip extends StatelessWidget {
  final String text;
  final bool selected;
  final VoidCallback onTap;

  const _KategoriChip({
    required this.text,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 10),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(20),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
          decoration: BoxDecoration(
            color: selected ? Colors.red.withOpacity(0.15) : Colors.white,
            borderRadius: BorderRadius.circular(20),
            border: Border.all(color: selected ? Colors.red : Colors.black12),
          ),
          child: Text(
            text,
            style: TextStyle(
              color: selected ? Colors.red : Colors.black,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
      ),
    );
  }
}

class _UrunCard extends StatelessWidget {
  final Urun urun;
  final VoidCallback onPlus;
  final VoidCallback onMinus;

  const _UrunCard({
    required this.urun,
    required this.onPlus,
    required this.onMinus,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 4,
      color: Colors.white,
      child: Padding(
        padding: const EdgeInsets.all(8),
        child: Column(
          children: [
            Expanded(
              child: ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: Image.network(
                  urun.urunResim,
                  width: double.infinity,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => const Center(child: Icon(Icons.broken_image)),
                ),
              ),
            ),
            const SizedBox(height: 6),
            Text(
              urun.urunAd,
              textAlign: TextAlign.center,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(fontSize: 16),
            ),
            const SizedBox(height: 4),
            Text(
              '${urun.urunFiyat.toStringAsFixed(2)} TL',
              style: const TextStyle(color: Colors.black54),
            ),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                SizedBox(
                  width: 32,
                  height: 32,
                  child: ElevatedButton(
                    onPressed: onPlus,
                    style: ElevatedButton.styleFrom(padding: EdgeInsets.zero),
                    child: const Text('+'),
                  ),
                ),
                const SizedBox(width: 8),
                SizedBox(
                  width: 24,
                  child: Text(
                    '${urun.urunAdet}',
                    textAlign: TextAlign.center,
                    style: const TextStyle(fontSize: 16),
                  ),
                ),
                const SizedBox(width: 8),
                SizedBox(
                  width: 32,
                  height: 32,
                  child: ElevatedButton(
                    onPressed: onMinus,
                    style: ElevatedButton.styleFrom(padding: EdgeInsets.zero),
                    child: const Text('−'),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
