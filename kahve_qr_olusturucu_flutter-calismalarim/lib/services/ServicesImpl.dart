

import 'Services.dart';
import 'http_service/KahveHttpServisDao.dart';


class ServicesImpl {
  static final ServicesImpl _instance = ServicesImpl._internal();
  final Services _service = KahveHTTPServisDao();

  ServicesImpl._internal();

  static ServicesImpl getInstance() => _instance;

  Services get service => _service;
}
