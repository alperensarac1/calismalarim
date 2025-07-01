import 'package:flutter_test/flutter_test.dart';
import 'package:flutter/material.dart';
import 'package:adisyon_flutter/model/Masa.dart';

void main() {
  testWidgets('Masa kartları görünüyor', (WidgetTester tester) async {
    // Sahte veri
    final masalar = [
      Masa(id: 1, masaAdi: 'Masa 1', acikMi: 1, sure: 10, toplamFiyat: 50.0),
      Masa(id: 2, masaAdi: 'Masa 2', acikMi: 0, sure: 0, toplamFiyat: 0.0),
    ];

    // Basit widget
    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: Column(
          children: masalar.map((m) => Text('${m.masaAdi}')).toList(),
        ),
      ),
    ));

    // Beklentiler
    expect(find.text('Masa 1'), findsOneWidget);
    expect(find.text('Masa 2'), findsOneWidget);
  });
}
