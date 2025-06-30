import 'package:flutter/material.dart';
import 'package:isletimsistemicalismasidart/giris_ekrani/repository/KullaniciBilgileri.dart';

class GuvenlikSorusuSecScreen extends StatefulWidget {
  final Function navigateToKayitOlustur;

  GuvenlikSorusuSecScreen({required this.navigateToKayitOlustur});

  @override
  _GuvenlikSorusuSecScreenState createState() => _GuvenlikSorusuSecScreenState();
}

class _GuvenlikSorusuSecScreenState extends State<GuvenlikSorusuSecScreen> {
  int selectedRadio = -1;
  TextEditingController textController = TextEditingController();
  late KullaniciBilgileri kullaniciBilgileri;
  List<String> questions = [
    "En sevdiğiniz müzik grubu nedir?",
    "En sevdiğiniz renk nedir?",
    "En sevdiğiniz hayvan nedir?"
  ];

  void saveSecurityQuestion() {
    if (selectedRadio != -1 && textController.text.isNotEmpty) {
      print("Seçilen Soru: ${questions[selectedRadio]}");
      print("Cevap: ${textController.text}");
      kullaniciBilgileri.guvenlikSorusuKaydet(questions[selectedRadio]);
      kullaniciBilgileri.guvenlikSorusuCevapKaydet(textController.text);
      widget.navigateToKayitOlustur();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              "Lütfen güvenlik sorularından birisini seçerek cevaplayınız",
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
            SizedBox(height: 20),
            Column(
              children: List.generate(questions.length, (index) {
                return Column(
                  children: [
                    RadioListTile(
                      title: Text(questions[index]),
                      value: index,
                      groupValue: selectedRadio,
                      onChanged: (value) {
                        setState(() {
                          selectedRadio = value as int;
                          textController.clear();
                        });
                      },
                    ),
                    if (selectedRadio == index)
                      Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 20.0),
                        child: Column(
                          children: [
                            TextField(
                              controller: textController,
                              decoration: InputDecoration(labelText: "Cevabınızı girin"),
                            ),
                            SizedBox(height: 10),
                            ElevatedButton(
                              onPressed: saveSecurityQuestion,
                              child: Text("Onayla"),
                            )
                          ],
                        ),
                      ),
                  ],
                );
              }),
            ),
          ],
        ),
      ),
    );
  }
}
