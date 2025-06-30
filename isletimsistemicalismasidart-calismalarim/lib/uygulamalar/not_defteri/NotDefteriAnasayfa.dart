import 'package:flutter/material.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/not_defteri/NotDefteriDetaysayfa.dart';
import 'package:provider/provider.dart';

import 'NotViewModel.dart';

class NotDefteriAnasayfa extends StatefulWidget {


  @override
  _NotDefteriAnasayfaState createState() => _NotDefteriAnasayfaState();
}

class _NotDefteriAnasayfaState extends State<NotDefteriAnasayfa> {
  String searchQuery = "";

  @override
  Widget build(BuildContext context) {
    final viewModel = Provider.of<NotDefteriViewModel>(context);
    final notListesi = viewModel.notlar;

    return Scaffold(
      floatingActionButton: FloatingActionButton(
        onPressed: (){
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => NotDefteriDetaySayfa(notId: null),
            ),
          );
        },
        backgroundColor: Theme.of(context).primaryColor,
        child: const Icon(Icons.add, color: Colors.white),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(
              onChanged: (value) {
                setState(() {
                  searchQuery = value;
                });
                viewModel.notAra(value);
              },
              decoration: const InputDecoration(
                hintText: "Ara...",
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 8.0),
            Expanded(
              child: ListView.builder(
                itemCount: notListesi.length,
                itemBuilder: (context, index) {
                  final not = notListesi[index];
                  return Card(
                    margin: const EdgeInsets.symmetric(vertical: 4.0),
                    elevation: 4.0,
                    child: ListTile(
                      title: Text(not.baslik),
                      subtitle: Text(not.notMetin),
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => NotDefteriDetaySayfa(notId: not.id),
                          ),
                        );
                      }
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}