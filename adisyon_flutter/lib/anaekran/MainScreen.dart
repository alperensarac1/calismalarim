import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:provider/provider.dart';

import '../masadetay/MasaDetayScreen.dart';
import '../model/Masa.dart';
import '../viewmodel/MasaDetayViewModel.dart';
import '../viewmodel/MasalarViewModel.dart';
import '../viewmodel/UrunViewModel.dart';
import 'MasaOzetLayout.dart';
import 'MasalarLayout.dart';

class MainScreen extends StatelessWidget {
  const MainScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => MasalarViewModel()..masalariYukle()),
        ChangeNotifierProvider(create: (_) => UrunViewModel()..kategorileriYukle()),
      ],
      child: const _MainScreenBody(),
    );
  }
}

class _MainScreenBody extends StatelessWidget {
  const _MainScreenBody();

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<MasalarViewModel>();
    final masaList = vm.masalar;

    if (masaList.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    // MasaDetayViewModel oluştururken masaId 0 verildi; detay kısmında güncellenecek.
    final detayVM = MasaDetayViewModel(0);

    return Scaffold(
      backgroundColor: const Color(0xFFFFBABA),
      body: Padding(
        padding: const EdgeInsets.all(10),
        child: Row(
          children: [
            // Masa özeti
            Expanded(
              flex: 1,
              child: MasaOzetLayout(
                masaList: masaList,
                detayViewModel: detayVM,
                onMasaDetay: (masa) {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => ChangeNotifierProvider(
                        create: (_) => MasaDetayViewModel(masa.id)..yukleTumVeriler(),
                        child: MasaDetayScreen(masaId: masa.id),
                      ),
                    ),
                  );
                },
              ),
            ),
            const SizedBox(width: 10),
            // Masalar grid
            Expanded(
              flex: 2,
              child: MasalarLayout(
                masaList: masaList,
                onMasaTap: (masa) {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => ChangeNotifierProvider(
                        create: (_) => MasaDetayViewModel(masa.id)..yukleTumVeriler(),
                        child: MasaDetayScreen(masaId: masa.id),
                      ),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
      floatingActionButton: _ActionButtonsRow(masaList: masaList),
    );
  }
}

class _ActionButtonsRow extends StatelessWidget {
  const _ActionButtonsRow({required this.masaList});

  final List<Masa> masaList;

  @override
  Widget build(BuildContext context) {
    final masalarVM = context.read<MasalarViewModel>();
    final urunVM = context.read<UrunViewModel>();

    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        FloatingActionButton.extended(
          heroTag: 'masa',
          label: const Text('Masa İşl.'),
          onPressed: () => _showMasaDialog(context, masalarVM),
        ),
        const SizedBox(width: 12),
        FloatingActionButton.extended(
          heroTag: 'urun',
          label: const Text('Ürün İşl.'),
          onPressed: () => showUrunDialog(context, urunVM),
        ),
        const SizedBox(width: 12),
        FloatingActionButton.extended(
          heroTag: 'birleştir',
          label: const Text('Birleştir'),
          onPressed: () => _showMasaBirlestirDialog(context, masalarVM),
        ),
        const SizedBox(width: 12),
        FloatingActionButton.extended(
          heroTag: 'kategori',
          label: const Text('Kategori'),
          onPressed: () => _showKategoriDialog(context, urunVM),
        ),
      ],
    );
  }

  // ——— Dialog helper'ları (kısa tutuldu) ———
  void _showMasaDialog(BuildContext ctx, MasalarViewModel vm) {
    final idCtrl = TextEditingController();
    showDialog(
      context: ctx,
      builder: (_) => AlertDialog(
        title: const Text('Masa İşlemleri'),
        content: TextField(
          controller: idCtrl,
          keyboardType: TextInputType.number,
          decoration: const InputDecoration(hintText: 'Silinecek masa ID'),
        ),
        actions: [
          TextButton(
            onPressed: () async {
              await vm.masaEkle();
              Navigator.pop(ctx);
            },
            child: const Text('Ekle'),
          ),
          TextButton(
            onPressed: () async {
              final id = int.tryParse(idCtrl.text);
              if (id != null) await vm.masaSil(id);
              Navigator.pop(ctx);
            },
            child: const Text('Sil'),
          ),
        ],
      ),
    );
  }

  void showUrunDialog(BuildContext context, UrunViewModel urunVM) {
    final kategoriler = urunVM.kategoriler;
    if (kategoriler.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Kategori listesi boş')),
      );
      return;
    }

    final urunAdController = TextEditingController();
    final fiyatController = TextEditingController();
    final silAdController = TextEditingController();
    int selectedKategoriIndex = 0;
    String? base64Resim;

    Future<void> resimSec() async {
      // İzin iste (Android 13+ için READ_MEDIA_IMAGES, iOS için PHOTOS izni)
      final status = await Permission.photos.request();
      if (status.isDenied || status.isPermanentlyDenied) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Galeri izni verilmedi')),
        );
        return;
      }

      // Resim seç
      final picker = ImagePicker();
      final picked = await picker.pickImage(source: ImageSource.gallery);
      if (picked != null) {
        final bytes = await picked.readAsBytes();
        base64Resim = base64Encode(bytes);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Resim seçildi')),
        );
      }
    }

    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text("Ürün Yönetimi"),
        content: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text("Ürün Ekle", style: TextStyle(fontWeight: FontWeight.bold)),
              TextField(controller: urunAdController, decoration: const InputDecoration(hintText: "Ürün Adı")),
              TextField(
                controller: fiyatController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(hintText: "Fiyat"),
              ),
              DropdownButton<int>(
                isExpanded: true,
                value: selectedKategoriIndex,
                items: [
                  for (int i = 0; i < kategoriler.length; i++)
                    DropdownMenuItem(
                      value: i,
                      child: Text(kategoriler[i].kategoriAd),
                    )
                ],
                onChanged: (val) {
                  selectedKategoriIndex = val!;
                },
              ),
              const SizedBox(height: 8),
              ElevatedButton.icon(
                onPressed: resimSec,
                icon: const Icon(Icons.image),
                label: const Text("Resim Seç"),
              ),
              const Divider(),
              const Text("Ürün Sil", style: TextStyle(fontWeight: FontWeight.bold)),
              TextField(controller: silAdController, decoration: const InputDecoration(hintText: "Ürün Adı")),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () {
              final urunAd = urunAdController.text.trim();
              final fiyat = double.tryParse(fiyatController.text.trim()) ?? 0;
              final kategori = kategoriler[selectedKategoriIndex];

              if (urunAd.isEmpty || fiyat <= 0 || base64Resim == null) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Tüm alanları doldurun')),
                );
                return;
              }

              urunVM.urunEkle(
                urunAd,
                fiyat,
                base64Resim!,
                kategori.id,
              );

              urunVM.urunleriYukle();
              Navigator.pop(context);
            },
            child: const Text("Ekle"),
          ),
          TextButton(
            onPressed: () {
              final ad = silAdController.text.trim();
              if (ad.isNotEmpty) {
                urunVM.urunSil(ad);
                urunVM.urunleriYukle();
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('"$ad" silindi')),
                );
              } else {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Lütfen silinecek ürün adı girin')),
                );
              }
            },
            child: const Text("Sil"),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text("İptal"),
          ),
        ],
      ),
    );
  }

  void _showMasaBirlestirDialog(BuildContext ctx, MasalarViewModel vm) {
    final anaCtrl = TextEditingController();
    final digerCtrl = TextEditingController();
    showDialog(
      context: ctx,
      builder: (_) => AlertDialog(
        title: const Text('Masa Birleştir'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: anaCtrl,
              decoration: const InputDecoration(hintText: 'Ana Masa ID'),
              keyboardType: TextInputType.number,
            ),
            TextField(
              controller: digerCtrl,
              decoration: const InputDecoration(hintText: 'Birleşecek Masa ID'),
              keyboardType: TextInputType.number,
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () {
              final ana = int.tryParse(anaCtrl.text);
              final diger = int.tryParse(digerCtrl.text);
              if (ana != null && diger != null) {
                vm.masaBirlestir(ana, diger);
              }
              Navigator.pop(ctx);
            },
            child: const Text('Birleştir'),
          ),
        ],
      ),
    );
  }

  void _showKategoriDialog(BuildContext ctx, UrunViewModel vm) {
    final adCtrl = TextEditingController();
    int selectedKategoriIndex = 0;

    showDialog(
      context: ctx,
      builder: (_) => StatefulBuilder(
        builder: (context, setState) => AlertDialog(
          title: const Text('Kategori İşlemleri'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: adCtrl,
                decoration: const InputDecoration(hintText: 'Yeni kategori adı'),
              ),
              const SizedBox(height: 12),
              if (vm.kategoriler.isNotEmpty)
                DropdownButton<int>(
                  value: selectedKategoriIndex,
                  items: List.generate(
                    vm.kategoriler.length,
                        (index) => DropdownMenuItem(
                      value: index,
                      child: Text(vm.kategoriler[index].kategoriAd),
                    ),
                  ),
                  onChanged: (v) => setState(() => selectedKategoriIndex = v ?? 0),
                ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () {
                final ad = adCtrl.text.trim();
                if (ad.isNotEmpty) vm.kategoriEkle(ad);
                Navigator.pop(ctx);
              },
              child: const Text('Ekle'),
            ),
            TextButton(
              onPressed: () {
                if (vm.kategoriler.isNotEmpty) {
                  final id = vm.kategoriler[selectedKategoriIndex].id;
                  vm.kategoriSil(id);
                }
                Navigator.pop(ctx);
              },
              child: const Text('Sil'),
            ),
          ],
        ),
      ),
    );
  }
}
