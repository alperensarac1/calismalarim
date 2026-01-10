import 'package:flutter/material.dart';
import 'package:flutter_adisyon_uygulama/masadetay/urun_card.dart';
import 'package:provider/provider.dart';

import '../viewmodel/masa_detay_viewmodel.dart';

class UrunlerScreen extends StatefulWidget {
  final int masaId;
  const UrunlerScreen({super.key, required this.masaId});

  @override
  State<UrunlerScreen> createState() => _UrunlerScreenState();
}

class _UrunlerScreenState extends State<UrunlerScreen> {
  int seciliKategoriIndex = 0;

  @override
  void initState() {
    super.initState();
    Future.microtask(() => context.read<MasaDetayViewModel>().yukleTumVeriler());
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<MasaDetayViewModel>();

    final tumUrunler = vm.tumUrunler;
    final kategoriler = vm.kategoriler;

    final guncelListe = (seciliKategoriIndex == 0)
        ? tumUrunler
        : tumUrunler.where((u) {
      final kat = kategoriler.elementAt(seciliKategoriIndex - 1);
      return u.urunKategori.id == kat.id;
    }).toList();

    return Scaffold(
      body: SafeArea(
        child: Row(
          children: [
            // Sol kategori
            SizedBox(
              width: 150,
              child: ListView(
                padding: const EdgeInsets.all(8),
                children: [
                  _KatRow(
                    text: 'Tümü',
                    selected: seciliKategoriIndex == 0,
                    onTap: () => setState(() => seciliKategoriIndex = 0),
                  ),
                  for (int i = 0; i < kategoriler.length; i++)
                    _KatRow(
                      text: kategoriler[i].kategoriAd,
                      selected: seciliKategoriIndex == i + 1,
                      onTap: () => setState(() => seciliKategoriIndex = i + 1),
                    ),
                ],
              ),
            ),

            // Sağ grid
            Expanded(
              child: Padding(
                padding: const EdgeInsets.all(8),
                child: guncelListe.isEmpty
                    ? const Center(
                  child: Text(
                    'Seçilen kategoriye ait ürün bulunamadı.',
                    style: TextStyle(color: Colors.grey, fontSize: 18),
                  ),
                )
                    : GridView.builder(
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 3,
                    crossAxisSpacing: 8,
                    mainAxisSpacing: 8,
                    childAspectRatio: 0.75,
                  ),
                  itemCount: guncelListe.length,
                  itemBuilder: (_, i) {
                    final urun = guncelListe[i];
                    return UrunCard(
                      urun: urun,
                      onPlus: () => vm.urunEkle(urun.id),
                      onMinus: () {
                        if (urun.urunAdet > 0) vm.urunCikar(urun.id);
                      },
                    );
                  },
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _KatRow extends StatelessWidget {
  final String text;
  final bool selected;
  final VoidCallback onTap;

  const _KatRow({required this.text, required this.selected, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12),
        color: selected ? Colors.black12 : Colors.transparent,
        child: Text(text, textAlign: TextAlign.center),
      ),
    );
  }
}
