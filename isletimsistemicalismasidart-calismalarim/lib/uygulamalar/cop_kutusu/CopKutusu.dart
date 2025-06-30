import 'package:flutter/material.dart';
import 'package:isletimsistemicalismasidart/ana_ekran/model/UygulamalarModel.dart';
import 'package:isletimsistemicalismasidart/ana_ekran/repository/UygulamalarRepository.dart';

import '../../ana_ekran/entity/Const.dart';

class CopKutusuScreen extends StatefulWidget {
  @override
  _CopKutusuScreenState createState() => _CopKutusuScreenState();
}

class _CopKutusuScreenState extends State<CopKutusuScreen> {
  List<UygulamalarModel> copUygulamalar = Const.copUygulamaListesi;
  UygulamalarRepository repository = UygulamalarRepository();
  late UygulamalarModel selectedUygulama;
  Offset dropDownOffset = Offset.zero;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Çöp Kutusu")),
      body: Stack(
        children: [
          GridView.builder(
            padding: EdgeInsets.all(8.0),
            gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 3,
              crossAxisSpacing: 8,
              mainAxisSpacing: 8,
            ),
            itemCount: copUygulamalar.length,
            itemBuilder: (context, index) {
              return GestureDetector(
                onLongPress: () {
                  setState(() {
                    selectedUygulama = copUygulamalar[index];
                    dropDownOffset = Offset(100, 200); // Manuel konumlandırma
                  });
                },
                child: Card(
                  child: Center(child:
                      Column(children: [
                        Image.asset(copUygulamalar[index].uygulamaResmi),
                        Text(copUygulamalar[index].uygulamaAdi),
                      ],)

                  ),
                ),
              );
            },
          ),
          if (selectedUygulama != null)
            Positioned(
              left: dropDownOffset.dx,
              top: dropDownOffset.dy,
              child: PopupMenuButton(
                onSelected: (value) {
                  if (value == "restore") {
                    setState(() {
                      copUygulamalar.remove(selectedUygulama);
                      repository.copKutusundanCikar(selectedUygulama);
                    });
                  }
                },
                itemBuilder: (context) => [
                  PopupMenuItem(
                    value: "restore",
                    child: Text("Çöp Kutusundan Çıkar"),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }
}
