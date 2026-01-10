

import 'package:flutter/material.dart';

import '../model/masa.dart';
import 'masa_card.dart';

class MasalarLayout extends StatelessWidget {
  final List<Masa> masalar;
  final void Function(Masa) onMasaClick;

  const MasalarLayout({
    super.key,
    required this.masalar,
    required this.onMasaClick,
  });

  @override
  Widget build(BuildContext context) {
    return GridView.builder(
      padding: const EdgeInsets.all(8),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        crossAxisSpacing: 8,
        mainAxisSpacing: 8,
        childAspectRatio: 1.2,
      ),
      itemCount: masalar.length,
      itemBuilder: (ctx, i) {
        return MasaCard(
          masa: masalar[i],
          onClick: onMasaClick,
        );
      },
    );
  }
}
