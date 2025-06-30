import 'package:flutter/material.dart';
import 'dart:core';

import 'package:isletimsistemicalismasidart/giris_ekrani/repository/KullaniciBilgileri.dart';

import 'components/EtSifre.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class GirisYapScreen extends StatefulWidget {
  final Function navigateToKayitOlustur;
  final Function navigateToSifremiUnuttum;
  final Function navigateToUygulamaEkrani;



  GirisYapScreen({
    required this.navigateToKayitOlustur,
    required this.navigateToSifremiUnuttum,
    required this.navigateToUygulamaEkrani,
  });

  @override
  _GirisYapScreenState createState() => _GirisYapScreenState();
}

class _GirisYapScreenState extends State<GirisYapScreen> {

  late KullaniciBilgileri kullaniciBilgileri;

  TextEditingController etSifre = TextEditingController();


  @override
  void initState() {
    super.initState();
    kullaniciBilgileri = KullaniciBilgileri();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!kullaniciBilgileri.sifreKaydiOlusturulmusMu()) {
        widget.navigateToKayitOlustur();
      }
    });
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
                controller: etSifre,
                parolaGoster: false,
                hint: "Şifre",
              ),
            ),
            SizedBox(height: 100),
            ElevatedButton(
              onPressed: () {
                if (kullaniciBilgileri.sifreSorgulama(etSifre.text)) {
                  widget.navigateToUygulamaEkrani();
                }
              },
              child: Text("Giriş Yap"),
            ),
            SizedBox(height: 100),
            GestureDetector(
              onTap: () {
                widget.navigateToSifremiUnuttum();
              },
              child: Text(
                "Şifremi Unuttum!",
                style: TextStyle(color: Colors.red),
              ),
            ),
          ],
        ),
      ),
    );
  }
}