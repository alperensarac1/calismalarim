import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';

class RehberVeritabaniYardimcisi {
  static final _databaseName = "rehberveritabani.db";
  static final _databaseVersion = 1;

  // Rehber tablosu
  static final table = 'rehber';
  static final columnId = 'kisi_id';
  static final columnIsim = 'kisi_isim';
  static final columnNumara = 'kisi_numara';

  // Singleton deseni ile veritabanı erişimi
  RehberVeritabaniYardimcisi._privateConstructor();
  static final RehberVeritabaniYardimcisi instance =
  RehberVeritabaniYardimcisi._privateConstructor();


  RehberVeritabaniYardimcisi();

  static Database? _database;

  Future<Database> get database async {
    if (_database != null) return _database!;
    // Veritabanını yaratıyoruz
    _database = await _initDatabase();
    return _database!;
  }

  // Veritabanını başlatma işlemi
  _initDatabase() async {
    var databasesPath = await getDatabasesPath();
    String path = join(databasesPath, _databaseName);

    return await openDatabase(path, version: _databaseVersion,
        onCreate: (db, version) async {
          // Tabloyu yaratma SQL komutu
          await db.execute('''
        CREATE TABLE $table (
          $columnId INTEGER PRIMARY KEY AUTOINCREMENT,
          $columnIsim TEXT,
          $columnNumara TEXT
        )
      ''');
        });
  }

  // Veritabanını güncelleme
  Future<void> upgradeDatabase(Database db, int oldVersion, int newVersion) async {
    if (oldVersion < newVersion) {
      // Tabloyu sil ve tekrar oluştur
      await db.execute("DROP TABLE IF EXISTS $table");
      await _initDatabase();
    }
  }

  // Kişi ekleme
  Future<int> insert(Map<String, dynamic> row) async {
    Database db = await instance.database;
    return await db.insert(table, row);
  }

  // Kişi silme
  Future<int> delete(int id) async {
    Database db = await instance.database;
    return await db.delete(table, where: '$columnId = ?', whereArgs: [id]);
  }

  // Kişi güncelleme
  Future<int> update(Map<String, dynamic> row) async {
    Database db = await instance.database;
    int id = row[columnId];
    return await db.update(table, row, where: '$columnId = ?', whereArgs: [id]);
  }

  // Tüm kişileri getirme
  Future<List<Map<String, dynamic>>> queryAll() async {
    Database db = await instance.database;
    return await db.query(table);
  }
}
