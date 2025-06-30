import 'package:flutter/material.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/rehber/entity/RehberDao.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'dart:convert';
import 'package:url_launcher/url_launcher.dart';

import '../model/RehberKisiler.dart';

class RehberEkrani extends StatefulWidget {
  final RehberVeritabaniYardimcisi vt;
  final Function onRefresh;

  RehberEkrani({required this.vt, required this.onRefresh});

  @override
  _RehberEkraniState createState() => _RehberEkraniState();
}

class _RehberEkraniState extends State<RehberEkrani> {
  late Future<List<RehberKisiler>> kisilerFuture;

  @override
  void initState() {
    super.initState();
    kisilerFuture = widget.vt.queryAll() as Future<List<RehberKisiler>>; // Future türünde veri alıyoruz
  }

  void refreshData() {
    setState(() {
      kisilerFuture = widget.vt.queryAll() as Future<List<RehberKisiler>>;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          // Navigate to add contact screen
          Navigator.pushNamed(context, '/rehberEkle');
        },
        child: Text("+"),
      ),
      body: FutureBuilder<List<RehberKisiler>>(
        future: kisilerFuture, // Burada future verisi bekleniyor
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            // Veriler yükleniyor, loading widget'ı göster
            return Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            // Hata durumu
            return Center(child: Text('Bir hata oluştu'));
          } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
            // Veri yoksa mesaj göster
            return Center(child: Text('Rehberde kişi yok'));
          } else {
            // Veriler geldi, listeyi oluştur
            List<RehberKisiler> kisiler = snapshot.data!;

            return ListView.builder(
              itemCount: kisiler.length,
              itemBuilder: (context, index) {
                return KisiItem(
                  vt: widget.vt,
                  kisi: kisiler[index],
                  onRefresh: refreshData,
                );
              },
            );
          }
        },
      ),
    );
  }
}
class KisiItem extends StatelessWidget {
  final RehberVeritabaniYardimcisi vt;
  final RehberKisiler kisi;
  final Function onRefresh;

  KisiItem({required this.vt, required this.kisi, required this.onRefresh});

  Future<void> _makePhoneCall(String phoneNumber) async {
    final status = await Permission.phone.request();
    if (status.isGranted) {
      final phoneUrl = 'tel:$phoneNumber';
      if (await canLaunch(phoneUrl)) {
        await launch(phoneUrl);
      } else {
        Fluttertoast.showToast(msg: "Arama yapılamıyor.");
      }
    } else {
      Fluttertoast.showToast(msg: "Çağrı izni gerekli!");
    }
  }

  @override
  Widget build(BuildContext context) {
    bool showMenu = false;

    return ListTile(
      title: Text(kisi.kisi_isim),
      subtitle: Text(kisi.kisi_numara),
      trailing: IconButton(
        icon: Icon(Icons.more_vert),
        onPressed: () {
          showMenu = true;
          showMenuDialog(context);
        },
      ),
      onTap: () async {
        _makePhoneCall(kisi.kisi_numara);
      },
    );
  }

  void showMenuDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text("Seçenekler"),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ListTile(
                title: Text("Düzenle"),
                onTap: () {
                  Navigator.pushNamed(
                    context,
                    '/rehberDetay',
                    arguments: jsonEncode(kisi.toJson()),
                  );
                },
              ),
              ListTile(
                title: Text("Sil"),
                onTap: () {
                  vt.delete(kisi.kisi_id);
                  onRefresh();
                  Navigator.pop(context);
                },
              ),
            ],
          ),
        );
      },
    );
  }
}
