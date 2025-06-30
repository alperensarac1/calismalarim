import 'dart:async';
import 'dart:typed_data';
import 'package:barcode/barcode.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/numara_kayit/NumaraKayitSp.dart';
import '../services/Services.dart';
import '../usecases/DogrulamaKoduOlustur.dart';


class QRKodState {
  final String kalanSure;
  final Uint8List? qrImageBytes;
  final String dogrulamaKodu;

  QRKodState({
    this.kalanSure = '90 saniye',
    this.qrImageBytes,
    this.dogrulamaKodu = '',
  });

  QRKodState copyWith({
    String? kalanSure,
    Uint8List? qrImageBytes,
    String? dogrulamaKodu,
  }) {
    return QRKodState(
      kalanSure: kalanSure ?? this.kalanSure,
      qrImageBytes: qrImageBytes ?? this.qrImageBytes,
      dogrulamaKodu: dogrulamaKodu ?? this.dogrulamaKodu,
    );
  }
}

class QRKodVM extends StateNotifier<QRKodState> {
  final Services kahveServisDao;
  final NumaraKayitSp numaraKayitSp;
  final DogrulamaKoduOlustur dogrulamaKoduOlustur;

  Timer? _timer;

  QRKodVM({
    required this.kahveServisDao,
    required this.numaraKayitSp,
    required this.dogrulamaKoduOlustur,
  }) : super(QRKodState()) {
    yeniKodUret();
  }

  void yeniKodUret() async {
    final yeniKod = dogrulamaKoduOlustur.randomIdOlustur();
    state = state.copyWith(dogrulamaKodu: yeniKod);
    koduOlustur(yeniKod);
    qrKodOlustur(yeniKod);
  }

  void qrKodOlustur(String kod) {
    try {
      final bc = Barcode.qrCode();
      final svg = bc.toSvg(kod, width: 200, height: 200);
      final bytes = Uint8List.fromList(svg.codeUnits);
      state = state.copyWith(qrImageBytes: bytes);
    } catch (e) {
      debugPrint('QR kod oluşturulurken hata: $e');
    }
  }

  void koduOlustur(String kod) async {
    final numara = numaraKayitSp.numaraGetir();
    if (numara == "") {
      debugPrint('Numara boş!');
      return;
    }

    await kahveServisDao.kodUret(kod, await numara);

    _timer?.cancel();
    int remaining = 90;
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (remaining > 0) {
        state = state.copyWith(kalanSure: "$remaining saniye");
        remaining--;
      } else {
        timer.cancel();
        yeniKodUret();
      }
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}

