import 'package:flutter/material.dart';

class Takvim extends StatefulWidget {
  @override
  _TakvimState createState() => _TakvimState();
}

class _TakvimState extends State<Takvim> {
  String selectedDate = getCurrentDateTime();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Takvim Uygulaması'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              'Bugünün Tarihi: $selectedDate',
              style: Theme.of(context).textTheme.bodySmall,
            ),
            SizedBox(height: 16),
            SizedBox(
              height: 250,
              child: CalendarDatePicker(
                initialDate: DateTime.now(),
                firstDate: DateTime(2000),
                lastDate: DateTime(2100),
                onDateChanged: (DateTime newDate) {
                  setState(() {
                    selectedDate = "${newDate.day}-${newDate.month}-${newDate.year}";
                  });
                },
              ),
            ),
            SizedBox(height: 16),
            Text(
              'Seçilen Tarih: $selectedDate',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ],
        ),
      ),
    );
  }
}

String getCurrentDateTime() {
  final DateTime now = DateTime.now();
  return "${now.day}-${now.month}-${now.year}";
}
