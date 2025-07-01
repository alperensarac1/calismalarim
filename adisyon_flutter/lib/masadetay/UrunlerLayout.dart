import 'package:flutter/material.dart';
import '../model/Urun.dart';
import '../model/Kategori.dart';

class UrunlerLayout extends StatefulWidget {
  final List<Urun> tumUrunler;
  final List<Kategori> kategoriler;
  final void Function(Urun urun) onUrunSec;

  const UrunlerLayout({
    super.key,
    required this.tumUrunler,
    required this.kategoriler,
    required this.onUrunSec,
  });

  @override
  State<UrunlerLayout> createState() => _UrunlerLayoutState();
}

class _UrunlerLayoutState extends State<UrunlerLayout> {
  late List<Urun> guncelUrunler;
  int seciliKategoriIndex = 0;

  @override
  void initState() {
    super.initState();
    print("📦 initState: ${widget.kategoriler.length} kategori yüklendi");
    kategoriFiltrele(0); // Başlangıçta tüm ürünleri göster
  }

  void kategoriFiltrele(int index) {
    print("🟥 kategoriFiltrele çağrıldı, index: $index");

    setState(() {
      seciliKategoriIndex = index;
      if (index == 0) {
        guncelUrunler = List.from(widget.tumUrunler);
        print("🟩 Tüm ürünler gösteriliyor: ${guncelUrunler.length} ürün");
      } else {
        final secilenKategori = widget.kategoriler[index - 1];
        print("🟨 Seçilen kategori: ${secilenKategori.kategoriAd} (id: ${secilenKategori.id})");
        guncelUrunler = widget.tumUrunler
            .where((urun) => urun.urunKategori.id == secilenKategori.id)
            .toList();
        print("🟦 Filtrelenmiş ürün sayısı: ${guncelUrunler.length}");
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final kategoriAdlari = ['Tümü', ...widget.kategoriler.map((e) => e.kategoriAd)];

    return Row(
      children: [
        // Kategori Listesi
        Container(
          width: 150,
          child: ListView.builder(
            itemCount: kategoriAdlari.length,
            itemBuilder: (context, index) {
              final secili = index == seciliKategoriIndex;
              return ListTile(
                title: Text(kategoriAdlari[index]),
                tileColor: secili ? Colors.grey[300] : null,
                onTap: () {
                  print("📌 Kategoriye tıklandı: $index");
                  kategoriFiltrele(index);
                },
              );
            },
          ),
        ),
        // Ürün Grid
        Expanded(
          child: guncelUrunler.isEmpty
              ? Center(child: Text("Seçilen kategoriye ait ürün bulunamadı."))
              : GridView.count(
            crossAxisCount: 3,
            childAspectRatio: 1,
            children: guncelUrunler.map((urun) {
              return Card(
                margin: const EdgeInsets.all(8),
                child: InkWell(
                  onTap: () => widget.onUrunSec(urun),
                  child: Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.fastfood),
                        const SizedBox(height: 8),
                        Text(urun.urunAd),
                        Text("${urun.urunFiyat.toStringAsFixed(2)} ₺"),
                      ],
                    ),
                  ),
                ),
              );
            }).toList(),
          ),
        ),
      ],
    );
  }
}
