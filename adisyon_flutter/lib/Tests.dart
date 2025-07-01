import 'package:flutter/material.dart';
import '../model/Urun.dart';
import '../model/Kategori.dart';
import '../service/AdisyonService.dart';

class AdisyonServiceTestScreen extends StatefulWidget {
  const AdisyonServiceTestScreen({super.key});

  @override
  State<AdisyonServiceTestScreen> createState() => _AdisyonServiceTestScreenState();
}

class _AdisyonServiceTestScreenState extends State<AdisyonServiceTestScreen> {
  final AdisyonService _service = AdisyonService();
  List<Urun> _urunler = [];
  List<Kategori> _kategoriler = [];
  String _log = "";

  void _logla(String mesaj) {
    setState(() {
      _log += "\n$mesaj";
    });
  }

  Future<void> _urunleriTestEt() async {
    try {
      final urunler = await _service.getUrunler();
      setState(() => _urunler = urunler);
      _logla("✅ Ürünler çekildi: ${urunler.length} adet");
    } catch (e) {
      _logla("❌ Ürün çekme hatası: $e");
    }
  }

  Future<void> _kategorileriTestEt() async {
    try {
      final kategoriler = await _service.getKategoriler();
      setState(() => _kategoriler = kategoriler);
      _logla("✅ Kategoriler çekildi: ${kategoriler.length} adet");
    } catch (e) {
      _logla("❌ Kategori çekme hatası: $e");
    }
  }

  Future<void> _kategoriEkleTestEt() async {
    try {
      await _service.kategoriEkle("TestKategori_${DateTime.now().millisecondsSinceEpoch}");
      _logla("✅ Test kategorisi eklendi");
    } catch (e) {
      _logla("❌ Kategori ekleme hatası: $e");
    }
  }

  Future<void> _urunEkleTestEt() async {
    try {
      if (_kategoriler.isEmpty) {
        _logla("⚠️ Önce kategori çekmelisiniz");
        return;
      }

      await _service.urunEkle(
        "TestÜrün_${DateTime.now().millisecondsSinceEpoch}",
        99.99,
        _kategoriler.first.id,
        1,
        "", // base64 resim yok
      );
      _logla("✅ Test ürünü eklendi");
    } catch (e) {
      _logla("❌ Ürün ekleme hatası: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Adisyon Test Ekranı")),
      body: Padding(
        padding: const EdgeInsets.all(12.0),
        child: ListView(
          children: [
            ElevatedButton(
              onPressed: _kategorileriTestEt,
              child: const Text("📁 Kategorileri Getir"),
            ),
            ElevatedButton(
              onPressed: _kategoriEkleTestEt,
              child: const Text("➕ Test Kategori Ekle"),
            ),
            ElevatedButton(
              onPressed: _urunleriTestEt,
              child: const Text("📦 Ürünleri Getir"),
            ),
            ElevatedButton(
              onPressed: _urunEkleTestEt,
              child: const Text("➕ Test Ürün Ekle (ilk kategoride)"),
            ),
            const SizedBox(height: 20),
            const Text("📝 LOG", style: TextStyle(fontWeight: FontWeight.bold)),
            Text(_log),
          ],
        ),
      ),
    );
  }
}
