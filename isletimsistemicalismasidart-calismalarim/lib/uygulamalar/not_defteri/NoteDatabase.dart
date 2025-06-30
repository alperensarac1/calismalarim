import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

import 'Not.dart';

class NoteDatabase {
  static final NoteDatabase instance = NoteDatabase._init();
  static Database? _database;

  NoteDatabase._init();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDB('NotlarDB.db');
    return _database!;
  }

  Future<Database> _initDB(String filePath) async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, filePath);
    return await openDatabase(path, version: 1, onCreate: _createDB);
  }

  Future<void> _createDB(Database db, int version) async {
    await db.execute('''
      CREATE TABLE Notlar (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        baslik TEXT,
        notMetin TEXT,
        listName TEXT,
        imageUrl TEXT,
        color TEXT,
        tarih TEXT
      )
    ''');
  }

  Future<int> yeniNot(Note not) async {
    final db = await database;
    return await db.insert('Notlar', not.toMap());
  }

  Future<List<Note>> getNotlarim() async {
    final db = await database;
    final result = await db.query('Notlar', orderBy: 'id DESC');
    return result.map((map) => Note.fromMap(map)).toList();
  }

  Future<int> notGuncelle(Note not) async {
    final db = await database;
    return await db.update(
      'Notlar',
      not.toMap(),
      where: 'id = ?',
      whereArgs: [not.id],
    );
  }

  Future<int> notSil(int id) async {
    final db = await database;
    return await db.delete(
      'Notlar',
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  Future<List<Note>> notAra(String aramaKelimesi) async {
    final db = await database;
    final result = await db.query(
      'Notlar',
      where: 'baslik LIKE ?',
      whereArgs: ['%$aramaKelimesi%'],
    );
    return result.map((map) => Note.fromMap(map)).toList();
  }

  Future<Note?> getNotById(int id) async {
    final db = await database;
    final result = await db.query(
      'Notlar',
      where: 'id = ?',
      whereArgs: [id],
    );
    if (result.isNotEmpty) {
      return Note.fromMap(result.first);
    }
    return null;
  }
}
