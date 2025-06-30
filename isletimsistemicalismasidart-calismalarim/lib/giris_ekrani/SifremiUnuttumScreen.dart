import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:isletimsistemicalismasidart/giris_ekrani/repository/KullaniciBilgileri.dart';

class SifremiUnuttumScreen extends StatefulWidget {
  final Function navigateToKayitOlustur;

  SifremiUnuttumScreen({required this.navigateToKayitOlustur});

  @override
  _SifremiUnuttumScreenState createState() => _SifremiUnuttumScreenState();
}

class _SifremiUnuttumScreenState extends State<SifremiUnuttumScreen> {
  TextEditingController tfGuvenlikSorusuCevap = TextEditingController();
  late KullaniciBilgileri kullaniciBilgileri;

  @override
  void initState() {
    kullaniciBilgileri = KullaniciBilgileri();
    super.initState();
  }

  String guvenlikSorusuGetir() {
    // Gerçek senaryoda burada güvenlik sorusu getirilecek
    return kullaniciBilgileri.guvenlikSorusuGetir();
  }



  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(guvenlikSorusuGetir(), style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            SizedBox(height: 20),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20.0),
              child: TextField(
                controller: tfGuvenlikSorusuCevap,
                decoration: InputDecoration(labelText: "Cevabınızı girin"),
              ),
            ),
            SizedBox(height: 20),
            ElevatedButton(
              onPressed: () {
                if (kullaniciBilgileri.guvenlikSorusuDogrula(tfGuvenlikSorusuCevap.text)) {
                  widget.navigateToKayitOlustur();
                } else {
                  Fluttertoast.showToast(
                    msg: "Güvenlik sorusu cevabı eşleşmiyor!",
                    toastLength: Toast.LENGTH_SHORT,
                    gravity: ToastGravity.BOTTOM,
                  );
                }
              },
              child: Text("Onayla"),
            ),
          ],
        ),
      ),
    );
  }
}