import 'package:flutter/material.dart';

import '../model/Masa.dart';
import '../viewmodel/MasaDetayViewModel.dart';

class MasaOzetLayout extends StatelessWidget {
  const MasaOzetLayout({
    super.key,
    required this.masaList,
    required this.detayViewModel,
    required this.onMasaDetay,
  });

  final List<Masa> masaList;
  final MasaDetayViewModel detayViewModel;
  final void Function(Masa) onMasaDetay;

  @override
  Widget build(BuildContext context) {
    final acikMasalar = masaList.where((m) => m.acikMi == 1).toList();

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 16),
          child: Text(
            '${acikMasalar.length} adet masa açık.',
            style: Theme.of(context).textTheme.titleMedium!.copyWith(color: Colors.red),
            textAlign: TextAlign.center,
          ),
        ),
        ListView.builder(
          shrinkWrap: true,
          itemCount: acikMasalar.length,
          itemBuilder: (context, index) {
            final masa = acikMasalar[index];
            return ListTile(
              title: Text('Masa ${masa.id}'),
              subtitle: Text('${masa.toplamFiyat.toStringAsFixed(2)} ₺'),
              onTap: () async {
                final secenek = await showDialog<int>(
                  context: context,
                  builder: (_) => SimpleDialog(
                    title: Text('Masa ${masa.id}'),
                    children: [
                      SimpleDialogOption(
                        onPressed: () => Navigator.pop(context, 0),
                        child: const Text('Ürün Ekle'),
                      ),
                      SimpleDialogOption(
                        onPressed: () => Navigator.pop(context, 1),
                        child: const Text('Ödeme Al'),
                      ),
                    ],
                  ),
                );
                if (secenek == 0) {
                  onMasaDetay(masa);
                } else if (secenek == 1) {
                  await detayViewModel.odemeAl(() {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Ödeme alındı')),
                    );
                  });
                }
              },
            );
          },
        )
      ],
    );
  }
}