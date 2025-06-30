import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';

import 'Not.dart';
import 'NotViewModel.dart';

class NotDefteriDetaySayfa extends StatefulWidget {
  final int? notId;



  const NotDefteriDetaySayfa({Key? key, this.notId}) : super(key: key);

  @override
  _NotDefteriDetaySayfaState createState() => _NotDefteriDetaySayfaState();
}

class _NotDefteriDetaySayfaState extends State<NotDefteriDetaySayfa> {
  final TextEditingController _baslikController = TextEditingController();
  final TextEditingController _notMetinController = TextEditingController();
  late String tarih;

  @override
  void initState() {
    super.initState();
    NotDefteriViewModel viewModel = Provider.of<NotDefteriViewModel>(context);
    tarih = DateFormat("EEEE, dd MMMM yyyy HH:mm", "tr_TR").format(DateTime.now());

    // Eğer güncellenecek bir not varsa yükle
    if (widget.notId != null) {
      final not = viewModel.getNotById(widget.notId!) as Note;
      if (not != null) {
        _baslikController.text = not.baslik;
        _notMetinController.text = not.notMetin;
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = Provider.of<NotDefteriViewModel>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text("Not Detayı"),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          if (_baslikController.text.isEmpty) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text("Başlık giriniz!")),
            );
          } else if (_notMetinController.text.isEmpty) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text("Not giriniz!")),
            );
          } else {
            final not = Note(
              id: widget.notId ?? 0,
              baslik: _baslikController.text,
              notMetin: _notMetinController.text,
              listName: "null",
              color: "null",
              tarih: tarih,
              imageUrl: "",
            );

              viewModel.yeniNot(not);


            Navigator.pop(context);
          }
        },
        backgroundColor: Theme.of(context).primaryColor,
        child: const Icon(Icons.save),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            TextField(
              controller: _baslikController,
              decoration: const InputDecoration(
                labelText: "Başlık",
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _notMetinController,
              decoration: const InputDecoration(
                labelText: "Not",
                border: OutlineInputBorder(),
              ),
              maxLines: 6,
            ),
            const SizedBox(height: 16),
            Text(
              tarih,
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                IconButton(
                  onPressed: () {
                    if (widget.notId != null) {
                      viewModel.notSil(widget.notId!);
                      Navigator.pop(context);
                    }
                  },
                  icon: const Icon(Icons.delete, color: Colors.red),
                ),
                IconButton(
                  onPressed: () => Navigator.pop(context),
                  icon: const Icon(Icons.arrow_back),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
