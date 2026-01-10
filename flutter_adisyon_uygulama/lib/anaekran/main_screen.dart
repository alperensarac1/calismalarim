import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../model/kategori.dart';
import '../viewmodel/masa_detay_viewmodel.dart';
import '../viewmodel/masalar_viewmodel.dart';
import '../viewmodel/urun_viewmodel.dart';
import 'masa_ozet_layout.dart';
import 'masalar_layout.dart';

class MainScreen extends StatefulWidget {
  final void Function(int masaId) onNavigateToMasaDetay;

  const MainScreen({super.key, required this.onNavigateToMasaDetay});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() async {
      final masalarVm = context.read<MasalarViewModel>();
      final urunVm = context.read<UrunViewModel>();
      await masalarVm.masalariYukle();
      await urunVm.kategorileriYukle();
    });
  }

  @override
  Widget build(BuildContext context) {
    final masalarVm = context.watch<MasalarViewModel>();
    final urunVm = context.watch<UrunViewModel>();

    final masaList = masalarVm.masalar;
    final kategoriList = urunVm.kategoriler;

    return Scaffold(
      backgroundColor: const Color(0xFFFFBABA),
      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: Row(
                children: [
                  Expanded(
                    flex: 1,
                    child: Padding(
                      padding: const EdgeInsets.all(8),
                      child: MasaOzetLayout(
                        masaListesi: masaList,
                        // ödeme için: burada "anlık masaId" ile VM üretmek daha doğru
                        // (Compose'da MasaDetayViewModel(masa.id) yapıyordun)
                        masaDetayVmFactory: (masaId) => MasaDetayViewModel(masaId: masaId),
                        onMasaDetayTikla: (masa) => widget.onNavigateToMasaDetay(masa.id),
                        onOdemeAlindi: () => masalarVm.masalariYukle(),
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    flex: 2,
                    child: Padding(
                      padding: const EdgeInsets.all(8),
                      child: MasalarLayout(
                        masalar: masaList,
                        onMasaClick: (masa) => widget.onNavigateToMasaDetay(masa.id),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 8),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Row(
                children: [
                  Expanded(
                    child: ElevatedButton(
                      onPressed: () => _showMasaEkleCikarDialog(context, masalarVm),
                      child: const Text('Masa İşlemleri'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: () => _showUrunEkleSilDialog(context, urunVm, kategoriList),
                      child: const Text('Ürün İşlemleri'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: () => _showMasaBirlestirDialog(context, masalarVm),
                      child: const Text('Masa Birleştir'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: () => _showKategoriEkleSilDialog(context, urunVm, kategoriList),
                      child: const Text('Kategori İşlemleri'),
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

  // --- Dialoglar ---

  Future<void> _showMasaEkleCikarDialog(BuildContext context, MasalarViewModel vm) async {
    final controller = TextEditingController();

    await showDialog(
      context: context,
      builder: (_) {
        return AlertDialog(
          title: const Text('Masa İşlemleri'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text('Yeni masa ekleyebilir veya masa ID girerek masa silebilirsiniz.'),
              const SizedBox(height: 8),
              TextField(
                controller: controller,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Silinecek Masa ID (sayı)',
                  border: OutlineInputBorder(),
                ),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () async {
                await vm.masaEkle(onSuccess: () {
                  ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Masa eklendi')));
                });
                if (context.mounted) Navigator.pop(context);
              },
              child: const Text('Masa Ekle'),
            ),
            TextButton(
              onPressed: () async {
                final id = int.tryParse(controller.text.trim());
                if (id != null) {
                  await vm.masaSil(id);
                  if (context.mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Masa $id silme isteği gönderildi')));
                  }
                }
                if (context.mounted) Navigator.pop(context);
              },
              child: const Text('Masa Sil'),
            ),
          ],
        );
      },
    );
  }

  Future<void> _showMasaBirlestirDialog(BuildContext context, MasalarViewModel vm) async {
    final aCtrl = TextEditingController();
    final bCtrl = TextEditingController();

    await showDialog(
      context: context,
      builder: (_) {
        return AlertDialog(
          title: const Text('Masa Birleştir'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: aCtrl,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(labelText: 'Ana Masa ID', border: OutlineInputBorder()),
              ),
              const SizedBox(height: 8),
              TextField(
                controller: bCtrl,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(labelText: 'Birleştirilecek Masa ID', border: OutlineInputBorder()),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () async {
                final a = int.tryParse(aCtrl.text.trim());
                final b = int.tryParse(bCtrl.text.trim());
                if (a != null && b != null) {
                  await vm.masaBirlestir(a, b);
                  if (context.mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Masa $a ve Masa $b birleştiriliyor')));
                    Navigator.pop(context);
                  }
                }
              },
              child: const Text('Birleştir'),
            ),
          ],
        );
      },
    );
  }

  Future<void> _showKategoriEkleSilDialog(BuildContext context, UrunViewModel vm, List<Kategori> kategoriList) async {
    final yeniCtrl = TextEditingController();
    int selectedIndex = 0;

    await showDialog(
      context: context,
      builder: (ctx) {
        return StatefulBuilder(
          builder: (ctx, setState) {
            return AlertDialog(
              title: const Text('Kategori İşlemleri'),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  TextField(
                    controller: yeniCtrl,
                    decoration: const InputDecoration(labelText: 'Yeni Kategori Adı', border: OutlineInputBorder()),
                  ),
                  const SizedBox(height: 12),
                  DropdownButtonFormField<int>(
                    value: (kategoriList.isNotEmpty) ? selectedIndex : null,
                    decoration: const InputDecoration(labelText: 'Silinecek Kategori', border: OutlineInputBorder()),
                    items: [
                      for (int i = 0; i < kategoriList.length; i++)
                        DropdownMenuItem(value: i, child: Text(kategoriList[i].kategoriAd)),
                    ],
                    onChanged: (v) => setState(() => selectedIndex = v ?? 0),
                  ),
                ],
              ),
              actions: [
                TextButton(
                  onPressed: () async {
                    final ad = yeniCtrl.text.trim();
                    if (ad.isNotEmpty) {
                      await vm.kategoriEkle(ad);
                      await vm.kategorileriYukle();
                      if (context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Kategori '$ad' eklendi")));
                        Navigator.pop(context);
                      }
                    }
                  },
                  child: const Text('Kaydet'),
                ),
                TextButton(
                  onPressed: () async {
                    if (kategoriList.isNotEmpty) {
                      final id = kategoriList[selectedIndex].id;
                      await vm.kategoriSil(id);
                      await vm.kategorileriYukle();
                      if (context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Kategori $id silme isteği gönderildi')));
                        Navigator.pop(context);
                      }
                    }
                  },
                  child: const Text('Sil'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  Future<void> _showUrunEkleSilDialog(BuildContext context, UrunViewModel vm, List<Kategori> kategoriList) async {
    final adCtrl = TextEditingController();
    final fiyatCtrl = TextEditingController();
    final silCtrl = TextEditingController();

    int selectedIndex = 0;
    String? base64Image;

    await showDialog(
      context: context,
      builder: (ctx) {
        return StatefulBuilder(
          builder: (ctx, setState) {
            return AlertDialog(
              title: const Text('Ürün İşlemleri'),
              content: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Align(
                      alignment: Alignment.centerLeft,
                      child: Text('— ÜRÜN EKLEME —', style: TextStyle(fontWeight: FontWeight.bold)),
                    ),
                    const SizedBox(height: 6),
                    TextField(
                      controller: adCtrl,
                      decoration: const InputDecoration(labelText: 'Ürün Adı', border: OutlineInputBorder()),
                    ),
                    const SizedBox(height: 6),
                    TextField(
                      controller: fiyatCtrl,
                      keyboardType: const TextInputType.numberWithOptions(decimal: true),
                      decoration: const InputDecoration(labelText: 'Ürün Fiyatı', border: OutlineInputBorder()),
                    ),
                    const SizedBox(height: 6),
                    DropdownButtonFormField<int>(
                      value: (kategoriList.isNotEmpty) ? selectedIndex : null,
                      decoration: const InputDecoration(labelText: 'Kategori Seç', border: OutlineInputBorder()),
                      items: [
                        for (int i = 0; i < kategoriList.length; i++)
                          DropdownMenuItem(value: i, child: Text(kategoriList[i].kategoriAd)),
                      ],
                      onChanged: (v) => setState(() => selectedIndex = v ?? 0),
                    ),
                    const SizedBox(height: 6),
                    ElevatedButton(
                      onPressed: () async {
                        // Compose'da image picker + base64 vardı.
                        // Burada "dışarıdan base64 ver" mantığına bağlamak için placeholder bıraktım.
                        // Sen image_picker kullanıyorsan, buraya entegre ederiz.
                        // Şimdilik test amaçlı boş değilse “seçildi” sayalım:
                        setState(() => base64Image = base64Image ?? 'BASE64_BURAYA');
                        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Resim seçildi (placeholder)')));
                      },
                      child: const Text('Resim Seç'),
                    ),
                    const SizedBox(height: 14),
                    const Divider(),
                    const SizedBox(height: 14),
                    const Align(
                      alignment: Alignment.centerLeft,
                      child: Text('— ÜRÜN SİLME —', style: TextStyle(fontWeight: FontWeight.bold)),
                    ),
                    const SizedBox(height: 6),
                    TextField(
                      controller: silCtrl,
                      decoration: const InputDecoration(labelText: 'Silinecek Ürün Adı', border: OutlineInputBorder()),
                    ),
                  ],
                ),
              ),
              actions: [
                TextButton(
                  onPressed: () async {
                    final ad = adCtrl.text.trim();
                    final fiyat = double.tryParse(fiyatCtrl.text.trim()) ?? 0.0;
                    final katId = (kategoriList.isNotEmpty) ? kategoriList[selectedIndex].id : 0;

                    if (ad.isNotEmpty && base64Image != null && katId != 0) {
                      await vm.urunEkle(ad: ad, fiyat: fiyat, kategoriId: katId, base64: base64Image!);
                      await vm.urunleriYukle();
                      if (context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Ürün ekleme isteği gönderildi')));
                        Navigator.pop(context);
                      }
                    }
                  },
                  child: const Text('Ekle'),
                ),
                TextButton(
                  onPressed: () async {
                    final silAd = silCtrl.text.trim();
                    if (silAd.isNotEmpty) {
                      await vm.urunSil(silAd);
                      await vm.urunleriYukle();
                      if (context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('"$silAd" silme isteği gönderildi')));
                        Navigator.pop(context);
                      }
                    }
                  },
                  child: const Text('Sil'),
                ),
              ],
            );
          },
        );
      },
    );
  }
}
