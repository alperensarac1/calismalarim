import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:isletimsistemicalismasidart/ana_ekran/entity/Const.dart';
import 'package:isletimsistemicalismasidart/ana_ekran/repository/UygulamalarRepository.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'model/UygulamalarModel.dart';

class UygulamaEkraniScreen extends StatefulWidget {

  @override
  _UygulamaEkraniScreenState createState() => _UygulamaEkraniScreenState();
}

class _UygulamaEkraniScreenState extends State<UygulamaEkraniScreen> {
  List<UygulamalarModel> uygulamalar = [];
  UygulamalarRepository repository = UygulamalarRepository();
  @override
  void initState() {
    super.initState();
    _loadUygulamalar();
  }

  void _loadUygulamalar(){
    setState(() {
      uygulamalar = Const.getUygulamalarListesi();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Padding(
        padding: const EdgeInsets.all(8.0),
        child: GridView.builder(
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 3,
            crossAxisSpacing: 8,
            mainAxisSpacing: 8,
          ),
          itemCount: uygulamalar.length,
          itemBuilder: (context, index) {
            final uygulama = uygulamalar[index];
            return GestureDetector(
              onTap: (){
                context.go("/" + uygulama.uygulamaGecisId);
              },
              onLongPress: () => _showContextMenu(context, uygulama),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Image.asset(
                    'assets/${uygulama.uygulamaResmi}.png',
                    width: 80,
                    height: 80,
                  ),
                  SizedBox(height: 8),
                  Text(uygulama.uygulamaAdi),
                ],
              ),
            );
          },
        ),
      ),
    );
  }

  void _showContextMenu(BuildContext context, UygulamalarModel uygulama) {
    final RenderBox overlay = Overlay.of(context).context.findRenderObject() as RenderBox;
    showMenu(
      context: context,
      position: RelativeRect.fromLTRB(100, 200, overlay.size.width - 100, overlay.size.height - 200),
      items: [
        PopupMenuItem(
          child: Text("Çöp Kutusuna Taşı"),
          onTap: () {
            repository.copKutusunaTasi(uygulama);
            setState(() {
              uygulamalar.remove(uygulama);
            });
          },
        ),
      ],
    );
  }
}


