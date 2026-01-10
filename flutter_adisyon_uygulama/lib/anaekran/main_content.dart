import 'package:flutter/material.dart';

import '../model/masa.dart';
import '../viewmodel/masa_detay_viewmodel.dart';
import 'masa_ozet_layout.dart';
import 'masalar_layout.dart';

class MainContent extends StatelessWidget {
  final List<Masa> masaListesi;
  final MasaDetayViewModel masaDetayVm; // ödeme gibi aksiyonlar için
  final void Function(Masa) onMasaClick;
  final void Function(Masa) onMasaDetayClick;

  const MainContent({
    super.key,
    required this.masaListesi,
    required this.masaDetayVm,
    required this.onMasaClick,
    required this.onMasaDetayClick,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      color: const Color(0xFFFFBABA),
      padding: const EdgeInsets.all(10),
      child: Row(
        children: [
          Expanded(
            flex: 1,
            child: MasaOzetLayout(
              masaListesi: masaListesi,
              masaDetayVm: masaDetayVm,
              onMasaDetayTikla: onMasaDetayClick,
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            flex: 2,
            child: MasalarLayout(
              masalar: masaListesi,
              onMasaClick: onMasaClick,
            ),
          ),
        ],
      ),
    );
  }
}
