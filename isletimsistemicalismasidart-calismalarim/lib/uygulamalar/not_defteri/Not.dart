import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';

class Note {
  int? id;
  String baslik;
  String notMetin;
  String listName;
  String imageUrl;
  String color;
  String tarih;

  Note({
    this.id,
    required this.baslik,
    required this.notMetin,
    required this.listName,
    required this.imageUrl,
    required this.color,
    required this.tarih,
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'baslik': baslik,
      'notMetin': notMetin,
      'listName': listName,
      'imageUrl': imageUrl,
      'color': color,
      'tarih': tarih,
    };
  }

  factory Note.fromMap(Map<String, dynamic> map) {
    return Note(
      id: map['id'],
      baslik: map['baslik'],
      notMetin: map['notMetin'],
      listName: map['listName'],
      imageUrl: map['imageUrl'],
      color: map['color'],
      tarih: map['tarih'],
    );
  }
}

