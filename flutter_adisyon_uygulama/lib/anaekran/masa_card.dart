import 'package:flutter/material.dart';

import '../model/masa.dart';
import '../utils/extension.dart';

class MasaCard extends StatelessWidget {
  final Masa masa;
  final void Function(Masa) onClick;

  const MasaCard({
    super.key,
    required this.masa,
    required this.onClick,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 8,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: InkWell(
        borderRadius: BorderRadius.circular(24),
        onTap: () => onClick(masa),
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Masa ${masa.id}',
                style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              Text(
                'Tutar: ${masa.toplamFiyat.fiyatYaz()}',
                style: const TextStyle(fontSize: 18, color: Colors.black54),
              ),
              const SizedBox(height: 4),
              Text(
                'Süre: ${masa.sure}',
                style: const TextStyle(fontSize: 16, color: Colors.grey),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
