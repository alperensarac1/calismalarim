// test/adisyon_service_test.dart
import 'package:flutter_test/flutter_test.dart';
import 'package:adisyon_flutter/service/AdisyonService.dart';

void main() {
  const baseUrl = 'https://alperensaracdeneme.com/adisyon/';

  late AdisyonService servis;

  setUpAll(() {
    servis = AdisyonService(baseUrl: baseUrl);
  });

  test('masa_listesi endpointi dönüyor mu?', () async {
    final masalar = await servis.getMasalar();

    // En azından 1 kayıt bekliyoruz
    expect(masalar.isNotEmpty, true, reason: 'Masa listesi boş geldi!');

    // Konsola yazdır (verbose çıktıyı test sonuçlarında görebilirsin)
    for (var m in masalar) {
      // ignore: avoid_print
      print('Masa ${m.id}: ${m.masaAdi} – ${m.toplamFiyat} ₺');
    }
  });
}
