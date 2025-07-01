import 'package:flutter/material.dart';

import '../model/Masa.dart';
import 'MasaCard.dart';


class MasalarLayout extends StatelessWidget {
  const MasalarLayout({
    super.key,
    required this.masaList,
    required this.onMasaTap,
  });

  final List<Masa> masaList;
  final void Function(Masa) onMasaTap;

  @override
  Widget build(BuildContext context) {
    return GridView.builder(
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        mainAxisSpacing: 16,
        crossAxisSpacing: 16,
        childAspectRatio: 0.9,
      ),
      itemCount: masaList.length,
      shrinkWrap: true,
      itemBuilder: (context, index) {
        final masa = masaList[index];
        return MasaCard(
          masa: masa,
          onTap: () => onMasaTap(masa),
        );
      },
    );
  }
}