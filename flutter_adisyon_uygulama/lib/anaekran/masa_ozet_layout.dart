import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import '../model/masa.dart';
import '../utils/extension.dart';
import '../viewmodel/masa_detay_viewmodel.dart';

class MasaOzetLayout extends StatefulWidget {
  final List<Masa> masaListesi;

  final MasaDetayViewModel? masaDetayVm;

  final MasaDetayViewModel Function(int masaId)? masaDetayVmFactory;

  final void Function(Masa) onMasaDetayTikla;
  final VoidCallback? onOdemeAlindi;

  const MasaOzetLayout({
    super.key,
    required this.masaListesi,
    required this.onMasaDetayTikla,
    this.masaDetayVm,
    this.masaDetayVmFactory,
    this.onOdemeAlindi,
  });

  @override
  State<MasaOzetLayout> createState() => _MasaOzetLayoutState();
}

class _MasaOzetLayoutState extends State<MasaOzetLayout> {
  Masa? seciliMasa;

  @override
  Widget build(BuildContext context) {
    final acikMasalar = widget.masaListesi.where((m) => m.acikMi == 1).toList();
    final acikMasaSayisi = acikMasalar.length;

    return Column(
      children: [
        Text(
          '$acikMasaSayisi adet masa açık.',
          textAlign: TextAlign.center,
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        Expanded(
          child: ListView.separated(
            itemCount: acikMasalar.length,
            separatorBuilder: (_, __) => const SizedBox(height: 8),
            itemBuilder: (ctx, i) {
              final masa = acikMasalar[i];
              return Card(
                color: Colors.cyan,
                elevation: 4,
                child: InkWell(
                  onTap: () => setState(() => seciliMasa = masa),
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Masa ${masa.id}', style: Theme.of(context).textTheme.bodyLarge),
                        const SizedBox(height: 4),
                        Text('Tutar: ${masa.toplamFiyat.fiyatYaz()}'),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
        ),
        if (seciliMasa != null) _buildDialog(context, seciliMasa!),
      ],
    );
  }

  Widget _buildDialog(BuildContext context, Masa masa) {
    return AlertDialog(
      title: Text('Masa ${masa.id}'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextButton(
            onPressed: () {
              widget.onMasaDetayTikla(masa);
              setState(() => seciliMasa = null);
            },
            child: const Text('Ürün Ekle'),
          ),
          const SizedBox(height: 8),
          TextButton(
            onPressed: () async {
              final vm = widget.masaDetayVm ??
                  widget.masaDetayVmFactory?.call(masa.id);

              if (vm != null) {
                await vm.odemeAl(onSuccess: () {});
                if (context.mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Ödeme alındı')));
                }
                widget.onOdemeAlindi?.call();
              }
              if (mounted) setState(() => seciliMasa = null);
            },
            child: const Text('Ödeme Al'),
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => setState(() => seciliMasa = null),
          child: const Text('Kapat'),
        )
      ],
    );
  }
}
