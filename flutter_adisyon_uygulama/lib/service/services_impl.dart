import 'package:flutter_adisyon_uygulama/service/services.dart';

import 'adisyon_api.dart';
import 'adisyon_servis_dao.dart';
import 'api_client.dart';

class ServicesImpl {
  ServicesImpl._();

  static Services? _instance;

  static Services getInstance() {
    _instance ??= AdisyonServisDao(
      AdisyonApi(ApiClient.create().dio),
    );
    return _instance!;
  }
}
