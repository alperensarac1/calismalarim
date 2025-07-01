import 'package:flutter/material.dart';

import '../model/Masa.dart';

class MasaCard extends StatelessWidget {
  const MasaCard({
    super.key,
    required this.masa,
    required this.onTap,
  });

  final Masa masa;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      elevation: 8,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(24),
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Masa ${masa.id}', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 4),
              Text('Tutar: ${masa.toplamFiyat.toStringAsFixed(2)} ₺'),
              const SizedBox(height: 4),
              Text('Süre: ${masa.sure}')
            ],
          ),
        ),
      ),
    );
  }
}