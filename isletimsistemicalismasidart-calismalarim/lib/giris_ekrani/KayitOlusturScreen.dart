import 'package:flutter/material.dart';
import 'package:isletimsistemicalismasidart/giris_ekrani/repository/KullaniciBilgileri.dart';

import 'components/EtSifre.dart';

class KayitOlusturScreen extends StatefulWidget {

  final Function navigateToGirisYap;

  const KayitOlusturScreen({Key? key, required this.navigateToGirisYap}) : super(key: key);



  @override
  _KayitOlusturScreenState createState() => _KayitOlusturScreenState();
}

class _KayitOlusturScreenState extends State<KayitOlusturScreen> {
  TextEditingController tf1 = TextEditingController();
  TextEditingController tf2 = TextEditingController();
  late KullaniciBilgileri kullaniciBilgileri;

  @override
  void initState() {
    kullaniciBilgileri = KullaniciBilgileri();
    super.initState();
  }

  void sifreKaydi(String sifre) {
    // Burada şifreyi kaydetme işlemi yapılabilir.
    kullaniciBilgileri.sifreKaydi(sifre);
    print("Şifre kaydedildi: $sifre");
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Image.asset('images/adios.png', scale: 4),
            SizedBox(height: 100),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20.0),
              child: EtSifre(
                controller: tf1,
                parolaGoster: false,
                hint: "Şifrenizi Tekrar Giriniz.",
              ),
            ),
            SizedBox(height: 50),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20.0),
              child: EtSifre(
                controller: tf2,
                parolaGoster: false,
                hint: "Şifrenizi Tekrar Giriniz.",
              ),
            ),
            SizedBox(height: 100),
            ElevatedButton(
              onPressed: () {
                if (tf1.text.isNotEmpty && tf2.text.isNotEmpty) {
                  if (tf1.text == tf2.text) {
                    sifreKaydi(tf1.text);
                    widget.navigateToGirisYap();
                  }
                }
              },
              child: Text("Kaydet"),
            ),
          ],
        ),
      ),
    );
  }
}