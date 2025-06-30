import 'package:flutter/material.dart';

class HesapMakinesiScreen extends StatefulWidget {
  @override
  _HesapMakinesiScreenState createState() => _HesapMakinesiScreenState();
}

class _HesapMakinesiScreenState extends State<HesapMakinesiScreen> {
  double? val1;
  double? val2;
  String action = "";
  String display = "";
  String result = "";

  // İşlem yapma fonksiyonu
  void operate(String op) {
    setState(() {
      if (val1 != null) {
        val2 = double.tryParse(display) ?? 0.0;
        if (val2 != null) {
          switch (action) {
            case "+":
              val1 = val1! + val2!;
              break;
            case "-":
              val1 = val1! - val2!;
              break;
            case "*":
              val1 = val1! * val2!;
              break;
            case "/":
              val1 = val2 != 0.0 ? val1! / val2! : double.nan;
              break;
          }
        }
      } else {
        val1 = double.tryParse(display) ?? 0.0;
      }
      action = op;
      result = "$val1 $op";
      display = "";
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Hesap Makinesi")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Sonuç ve işlem ekranı
            Align(
              alignment: Alignment.centerRight,
              child: Text(
                result,
                style: TextStyle(fontSize: 24, color: Colors.grey),
              ),
            ),
            Align(
              alignment: Alignment.centerRight,
              child: Text(
                display,
                style: TextStyle(fontSize: 36, fontWeight: FontWeight.bold),
              ),
            ),
            SizedBox(height: 16),

            // Sayı Butonları
            for (var row in [
              ["7", "8", "9"],
              ["4", "5", "6"],
              ["1", "2", "3"],
              ["0"]
            ])
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: row.map((number) {
                  return ElevatedButton(
                    onPressed: () {
                      setState(() {
                        display += number;
                      });
                    },
                    style: ElevatedButton.styleFrom(
                      fixedSize: Size(70, 70),
                    ),
                    child: Text(number, style: TextStyle(fontSize: 24)),
                  );
                }).toList(),
              ),

            SizedBox(height: 8),

            // İşlem Butonları
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: ["+", "-", "*", "/"].map((op) {
                return ElevatedButton(
                  onPressed: () => operate(op),
                  style: ElevatedButton.styleFrom(
                    fixedSize: Size(70, 70),
                  ),
                  child: Text(op, style: TextStyle(fontSize: 24)),
                );
              }).toList(),
            ),

            SizedBox(height: 8),

            // Sonuç ve Temizleme Butonları
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton(
                  onPressed: () {
                    setState(() {
                      if (val1 != null) {
                        val2 = double.tryParse(display) ?? 0.0;
                        switch (action) {
                          case "+":
                            val1 = val1! + val2!;
                            break;
                          case "-":
                            val1 = val1! - val2!;
                            break;
                          case "*":
                            val1 = val1! * val2!;
                            break;
                          case "/":
                            val1 = val2 != 0.0 ? val1! / val2! : double.nan;
                            break;
                        }
                      } else {
                        val1 = double.tryParse(display) ?? 0.0;
                      }
                      action = "";
                      result = "";
                      display = val1.toString();
                    });
                  },
                  style: ElevatedButton.styleFrom(fixedSize: Size(150, 70)),
                  child: Text("=", style: TextStyle(fontSize: 24)),
                ),
                ElevatedButton(
                  onPressed: () {
                    setState(() {
                      val1 = null;
                      val2 = null;
                      display = "";
                      result = "";
                      action = "";
                    });
                  },
                  style: ElevatedButton.styleFrom(fixedSize: Size(70, 70)),
                  child: Text("C", style: TextStyle(fontSize: 24)),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
