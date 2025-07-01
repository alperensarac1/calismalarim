import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../model/Masa.dart';
import '../viewmodel/MasaDetayViewModel.dart';

class MasaAdisyonLayout extends StatelessWidget {
  final Masa masa;

  const MasaAdisyonLayout({
    super.key,
    required this.masa,
  });

  @override
  Widget build(BuildContext context) {
    final viewModel = Provider.of<MasaDetayViewModel>(context);

    return Column(
      children: [
        // Ürün listesi
        Expanded(
          child: ListView.builder(
            itemCount: viewModel.masaUrunler.length,
            itemBuilder: (context, index) {
              final urun = viewModel.masaUrunler[index];
              return ListTile(
                title: Text('${urun.urunAd} (${urun.adet})'),
              );
            },
          ),
        ),

        // Fiyat ve buton
        Container(
          padding: const EdgeInsets.all(16),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded( // BU EKLENDİ
                child: Text(
                  '${viewModel.toplamFiyat.toStringAsFixed(2)} TL',
                  style: const TextStyle(fontSize: 20, color: Colors.grey),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
              const SizedBox(width: 8), // Butonla yazı arasında boşluk
              ElevatedButton(
                onPressed: () async {
                  await viewModel.odemeAl(() {});
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Ödeme alındı')),
                  );
                  await viewModel.yukleTumVeriler();
                },
                style: ElevatedButton.styleFrom(backgroundColor: Colors.blue),
                child: const Text("Ödeme Al"),
              ),
            ],
          )

        ),
      ],
    );
  }
}
