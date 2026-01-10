import 'dart:ui';

import 'package:flutter/material.dart';

import '../model/urun.dart';

class UrunCard extends StatelessWidget {
  final Urun urun;
  final VoidCallback onPlus;
  final VoidCallback onMinus;

  const UrunCard({
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
                  errorBuilder: (_, __, ___) =>
                  const Center(child: Icon(Icons.broken_image)),
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
