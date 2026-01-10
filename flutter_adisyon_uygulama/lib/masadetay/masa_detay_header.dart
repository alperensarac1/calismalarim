import 'package:flutter/material.dart';

import '../model/masa.dart';

class MasaDetayHeader extends StatelessWidget {
  final Masa masa;
  final VoidCallback onBackPressed;

  const MasaDetayHeader({
    super.key,
    required this.masa,
    required this.onBackPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 16),
      child: Row(
        children: [
          IconButton(
            onPressed: onBackPressed,
            icon: const Icon(Icons.arrow_back),
            iconSize: 24,
          ),
          const SizedBox(width: 8),
          Text(
            'Masa ${masa.id}',
            style: const TextStyle(fontSize: 20),
          ),
        ],
      ),
    );
  }
}
